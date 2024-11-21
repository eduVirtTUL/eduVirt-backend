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
import pl.lodz.p.it.eduvirt.service.OVirtVnicProfileService;
import pl.lodz.p.it.eduvirt.util.connection.ConnectionFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@LoggerInterceptor
@RequiredArgsConstructor
public class OVirtVnicProfileServiceImpl implements OVirtVnicProfileService {

    private final ConnectionFactory connectionFactory;
    private final VnicProfileRepository vnicProfileRepository;

    private final VlansRangeRepository vlansRangeRepository;

    @Override
    @Transactional(propagation = Propagation.NEVER)
    public Map<Boolean, List<VnicProfile>> getSynchronizedVnicProfiles() {
        List<VnicProfile> vnicProfilesInPool = new ArrayList<>();
        List<VnicProfile> vnicProfilesOutOfPool = new ArrayList<>();

        List<VnicProfilePoolMember> vnicProfilePoolMembersInPool = getVnicProfilesPool();

        fetchOVirtVnicProfiles().forEach(oVirtVnicProfile -> {
            int vlanId = Optional.ofNullable(oVirtVnicProfile.network().vlan())
                    .map(v -> v.id().intValue())
                    .orElse(-1);

            if (vlanId > -1 && vlansRangeRepository.findAll().stream().anyMatch(vlansRange ->
                    vlansRange.getFrom() <= vlanId && vlansRange.getTo() >= vlanId)) {
                if (vnicProfilePoolMembersInPool.stream().anyMatch(poolMember ->
                        poolMember.getId().toString().equals(oVirtVnicProfile.id()))) {
                    vnicProfilesInPool.add(oVirtVnicProfile);
                } else {
                    vnicProfilesOutOfPool.add(oVirtVnicProfile);
                }
            }
        });

        return Map.ofEntries(
                Map.entry(true, vnicProfilesInPool),
                Map.entry(false, vnicProfilesOutOfPool)
        );
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
//                    .stream()
//                    .map(vnicProfileMapper::ovirtVnicProfileToDto)
//                    .toList();
        } catch (Exception e) {
            //todo: error handling
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<VnicProfilePoolMember> getVnicProfilesPool() {
        return vnicProfileRepository.findAll();
    }

    @Override
    @Transactional
    public VnicProfilePoolMember addVnicProfileToPool(UUID vnicProfileId) {
        if (vnicProfileRepository.findById(vnicProfileId).isPresent())
            throw new EntityAlreadyException(vnicProfileId.toString());

        //TODO to refactor to synchronizing with oVirt
        if (fetchOVirtVnicProfiles().stream().anyMatch(vnicProfile ->
                vnicProfile.id().equals(vnicProfileId.toString()))) {
            return vnicProfileRepository.saveAndFlush(new VnicProfilePoolMember(vnicProfileId, false));
        } else {
            throw new VnicProfileOvirtNotFoundException(vnicProfileId.toString());
        }

        //TODO add checking if network connected to vnic profile is unique in the pool


        //TODO CHECK VLANS RANGES
    }

    @Override
    @Transactional
    public void removeVnicProfileFromPool(UUID vnicProfileId) {
        if (vnicProfileRepository.findById(vnicProfileId).isEmpty())
            throw new VnicProfileEduvirtNotFoundException(vnicProfileId.toString());

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
