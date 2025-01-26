package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.ovirt.engine.sdk4.types.VnicProfile;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.entity.network.VlansRange;
import pl.lodz.p.it.eduvirt.entity.network.VnicProfilePoolMember;
import pl.lodz.p.it.eduvirt.exceptions.VnicProfileAlreadyExistsException;
import pl.lodz.p.it.eduvirt.exceptions.VnicProfileCurrentlyInUseException;
import pl.lodz.p.it.eduvirt.exceptions.VnicProfileEduvirtNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.VnicProfileOvirtNotFoundException;
import pl.lodz.p.it.eduvirt.repository.VlansRangeRepository;
import pl.lodz.p.it.eduvirt.repository.VnicProfileRepository;
import pl.lodz.p.it.eduvirt.service.ovirt.OVirtVnicProfileService;
import pl.lodz.p.it.eduvirt.service.VnicProfilePoolService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

//TODO michal: transactional for vnic profile/vlan ranges controllers/services/repositories
//TODO michal consider managing transaction timeouts rather than excluding API operations from transactions

@Service
@LoggerInterceptor
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED)
public class VnicProfilePoolServiceImpl implements VnicProfilePoolService {

    /* Repositories */

    private final VnicProfileRepository vnicProfileRepository;
    private final VlansRangeRepository vlansRangeRepository;

    /* Services */

    private final OVirtVnicProfileService oVirtVnicProfileService;

    /* Read methods */

    @Override
    public Map<Boolean, List<VnicProfile>> getSynchronizedVnicProfiles(Pageable pageable) {
        // Fetch data from eduVirt
        List<VlansRange> vlansRanges = vlansRangeRepository.findAll();
        // Fetch data from oVirt
        List<VnicProfile> ovirtVnicProfiles = oVirtVnicProfileService.getVnicProfiles(pageable, vlansRanges.toArray(new VlansRange[0]));
        // Fetch data from eduVirt (based on data from oVirt)
        Set<UUID> vnicProfilePoolIds = vnicProfileRepository.findAllWithIds(
                        ovirtVnicProfiles.stream().map(vp -> UUID.fromString(vp.id())).toList()
                ).stream()
                .map(VnicProfilePoolMember::getId)
                .collect(Collectors.toSet());

        List<VnicProfile> vnicProfilesInPool = new ArrayList<>();
        List<VnicProfile> vnicProfilesOutOfPool = new ArrayList<>();

        ovirtVnicProfiles.forEach(oVirtVnicProfile -> {
            if (vnicProfilePoolIds.contains(UUID.fromString(oVirtVnicProfile.id()))) {
                vnicProfilesInPool.add(oVirtVnicProfile);
            } else {
                vnicProfilesOutOfPool.add(oVirtVnicProfile);
            }
        });

        return Map.ofEntries(
                Map.entry(Boolean.TRUE, vnicProfilesInPool),
                Map.entry(Boolean.FALSE, vnicProfilesOutOfPool)
        );
    }

    @Override
    public List<VnicProfilePoolMember> getVnicProfilesPool() {
        //todo optimize
        List<VnicProfilePoolMember> vnicProfilePoolMembers = vnicProfileRepository.findAll();
        Map<String, VnicProfile> vnicProfilesInPool = getSynchronizedVnicProfiles(vnicProfilePoolMembers).get(Boolean.TRUE)
                .stream()
                .collect(Collectors.toUnmodifiableMap(
                                VnicProfile::id,
                                vnicProfile -> vnicProfile
                        )
                );

        List<VnicProfilePoolMember> mappedVnicProfilePoolMembers = vnicProfilePoolMembers.stream()
                .filter(vnicProfilePoolMember -> vnicProfilesInPool.containsKey(vnicProfilePoolMember.getId().toString()))
                .toList();

        mappedVnicProfilePoolMembers.forEach(vnicProfilePoolMember ->
                vnicProfilePoolMember.setName(vnicProfilesInPool.get(vnicProfilePoolMember.getId().toString()).name())
        );
        return mappedVnicProfilePoolMembers;
    }

    @Override
    public VnicProfilePoolMember getVnicProfileFromPool(UUID vnicProfileId) {
        Optional.ofNullable(oVirtVnicProfileService.getVnicProfileById(vnicProfileId.toString()))
                .orElseThrow(() -> new VnicProfileOvirtNotFoundException(vnicProfileId));

        return vnicProfileRepository.findById(vnicProfileId)
                .orElseThrow(() -> new VnicProfileEduvirtNotFoundException(vnicProfileId));
    }

    /* Create methods */

    @Override
    public VnicProfilePoolMember addVnicProfileToPool(UUID vnicProfileId) {
        if (vnicProfileRepository.findById(vnicProfileId).isPresent()) {
            throw new VnicProfileAlreadyExistsException(vnicProfileId);
        }

        VnicProfile oVirtVnicProfile = Optional.ofNullable(oVirtVnicProfileService.getVnicProfileById(vnicProfileId.toString()))
                .orElseThrow(() -> new VnicProfileOvirtNotFoundException(vnicProfileId));

        return vnicProfileRepository.saveAndFlush(
                new VnicProfilePoolMember(vnicProfileId, oVirtVnicProfile.network().vlan().idAsInteger())
        );
    }

    /* Delete methods */

    @Override
    public void removeVnicProfileFromPool(UUID vnicProfileId) {
        Optional<VnicProfilePoolMember> vnicProfileOpt = vnicProfileRepository.findById(vnicProfileId);
        if (vnicProfileOpt.isEmpty()) {
            throw new VnicProfileEduvirtNotFoundException(vnicProfileId);
        }

        if (vnicProfileOpt.get().getInUse()) {
            throw new VnicProfileCurrentlyInUseException(vnicProfileId);
        }

        vnicProfileRepository.deleteById(vnicProfileId);
    }

    /* Update methods */

    @Override
    public void markVnicProfileAsOccupied(UUID vnicProfileId) {
        changeVnicProfilePoolMemberStatus(vnicProfileId, true);
    }

    @Override
    public void markVnicProfileAsFree(UUID vnicProfileId) {
        changeVnicProfilePoolMemberStatus(vnicProfileId, false);
    }

    /* Private methods */

    private Map<Boolean, List<VnicProfile>> getSynchronizedVnicProfiles(List<VnicProfilePoolMember> poolSource) {
        List<VnicProfile> vnicProfilesInPool = new ArrayList<>();
        List<VnicProfile> vnicProfilesOutOfPool = new ArrayList<>();

        oVirtVnicProfileService.getVnicProfiles().forEach(oVirtVnicProfile -> {
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

    private void changeVnicProfilePoolMemberStatus(UUID vnicProfileId, boolean setInUser) {
        Optional<VnicProfilePoolMember> vnicProfileOpt = vnicProfileRepository.findById(vnicProfileId);
        if (vnicProfileOpt.isEmpty()) throw new VnicProfileEduvirtNotFoundException(vnicProfileId);

        VnicProfilePoolMember vnicProfile = vnicProfileOpt.get();

        //todo
//        if (vnicProfile.getInUse()) {
//            throw new VnicProfileCurrentlyInUseException(vnicProfileId);
//        }

        vnicProfile.setInUse(setInUser);

        vnicProfileRepository.saveAndFlush(vnicProfile);
    }
}
