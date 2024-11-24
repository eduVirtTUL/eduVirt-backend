package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.ovirt.engine.sdk4.Connection;
import org.ovirt.engine.sdk4.types.VnicProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.entity.eduvirt.network.VnicProfilePoolMember;
import pl.lodz.p.it.eduvirt.exceptions.EntityAlreadyException;
import pl.lodz.p.it.eduvirt.exceptions.vnic_profile.VnicProfileEduvirtNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.vnic_profile.VnicProfileOvirtNotFoundException;
import pl.lodz.p.it.eduvirt.repository.eduvirt.VlansRangeRepository;
import pl.lodz.p.it.eduvirt.repository.eduvirt.VnicProfileRepository;
import pl.lodz.p.it.eduvirt.service.VnicProfilePoolService;
import pl.lodz.p.it.eduvirt.util.connection.ConnectionFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@LoggerInterceptor
@RequiredArgsConstructor
public class VnicProfilePoolServiceImpl implements VnicProfilePoolService {

    //TODO consider managing transaction timeouts rather than excluding API operations from transactions

    private final ConnectionFactory connectionFactory;
    private final VnicProfileRepository vnicProfileRepository;

    private final VlansRangeRepository vlansRangeRepository;

    @Override
    @Transactional
    public Map<Boolean, List<VnicProfile>> getSynchronizedVnicProfiles() {
        return getSynchronizedVnicProfiles(vnicProfileRepository.findAll());
    }

    //TODO test Transactional????
    //TODO change map to class with lists /maps??
    @Transactional
    Map<Boolean, List<VnicProfile>> getSynchronizedVnicProfiles(List<VnicProfilePoolMember> poolSource) {
        List<VnicProfile> vnicProfilesInPool = new ArrayList<>();
        List<VnicProfile> vnicProfilesOutOfPool = new ArrayList<>();

        fetchOVirtVnicProfiles().forEach(oVirtVnicProfile -> {
            int vlanId = Optional.ofNullable(oVirtVnicProfile.network().vlan())
                    .map(v -> v.id().intValue())
                    .orElse(-1);

            if (vlanId > -1 && isInRanges(vlanId)) {
                if (poolSource.stream()
                        .anyMatch(poolMember -> poolMember.getId().toString().equals(oVirtVnicProfile.id()))) {
                    vnicProfilesInPool.add(oVirtVnicProfile);
                } else {
                    vnicProfilesOutOfPool.add(oVirtVnicProfile);
                }
            }
        });

        return Map.ofEntries(
                Map.entry(Boolean.TRUE, vnicProfilesInPool),
                Map.entry(Boolean.FALSE, vnicProfilesOutOfPool)
        );
    }

    private boolean isInRanges(int vlanId) {
        return vlansRangeRepository.findAll().stream()
                .anyMatch(vlansRange -> vlansRange.getFrom() <= vlanId && vlansRange.getTo() >= vlanId);
    }

    @Override
    @Transactional(propagation = Propagation.NEVER)
    public List<VnicProfile> fetchOVirtVnicProfiles() {
        // TODO maybe change it to fetching data from oVirt database rather then via API
        try (Connection connection = connectionFactory.getConnection()) {
            return connection.systemService()
                    .vnicProfilesService()
                    .list()
                    .follow("network")
                    .send()
                    .profiles();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional
    public List<VnicProfilePoolMember> getVnicProfilesPool() {
        // TODO optimization
        List<VnicProfilePoolMember> vnicProfilePoolMembers = vnicProfileRepository.findAll();
        Set<String> vnicProfilesInPoolIds = getSynchronizedVnicProfiles(vnicProfilePoolMembers).get(Boolean.TRUE)
                .stream()
                .map(VnicProfile::id)
                .collect(Collectors.toUnmodifiableSet());

        return vnicProfilePoolMembers.stream()
                .filter(vnicProfilePoolMember -> vnicProfilesInPoolIds.contains(vnicProfilePoolMember.getId().toString()))
                .toList();
    }

    @Override
    @Transactional
    public VnicProfilePoolMember addVnicProfileToPool(UUID vnicProfileId) {
        if (vnicProfileRepository.findById(vnicProfileId).isPresent()) {
            throw new EntityAlreadyException(vnicProfileId.toString());
        }

        List<VnicProfile> ovirtVnicProfiles = getSynchronizedVnicProfiles().get(Boolean.FALSE);

        Optional<VnicProfile> relatedVnicProfile = ovirtVnicProfiles.stream()
                .filter(vnicProfile -> vnicProfile.id().equals(vnicProfileId.toString()))
                .findFirst();

        if (relatedVnicProfile.isPresent()) {
            return vnicProfileRepository.saveAndFlush(
                    new VnicProfilePoolMember(vnicProfileId, relatedVnicProfile.get().network().vlan().idAsInteger())
            );
        } else if (false) {
            // TODO implement handling vlan not in ranges
            throw new RuntimeException("Vlan id not in range");
        } else {
            throw new VnicProfileOvirtNotFoundException(vnicProfileId.toString());
        }
    }

    @Override
    @Transactional
    public void removeVnicProfileFromPool(UUID vnicProfileId) {
        if (vnicProfileRepository.findById(vnicProfileId).isEmpty()) {
            throw new VnicProfileEduvirtNotFoundException(vnicProfileId.toString());
        }

        vnicProfileRepository.deleteById(vnicProfileId);
    }

    @Override
    @Transactional
    public void markVnicProfileAsOccupied(UUID vnicProfileId) {
        changeVnicProfilePoolMemberStatus(vnicProfileId, true);
    }

    @Override
    @Transactional
    public void markVnicProfileAsFree(UUID vnicProfileId) {
        changeVnicProfilePoolMemberStatus(vnicProfileId, false);
    }

    private void changeVnicProfilePoolMemberStatus(UUID vnicProfileId, boolean setInUser) {
        Optional<VnicProfilePoolMember> vnicProfileOpt = vnicProfileRepository.findById(vnicProfileId);
        if (vnicProfileOpt.isEmpty()) throw new VnicProfileEduvirtNotFoundException(vnicProfileId.toString());

        VnicProfilePoolMember vnicProfile = vnicProfileOpt.get();

        vnicProfile.setInUse(setInUser);

        vnicProfileRepository.saveAndFlush(vnicProfile);
    }
}
