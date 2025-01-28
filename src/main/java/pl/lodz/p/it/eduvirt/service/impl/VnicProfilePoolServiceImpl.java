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
import pl.lodz.p.it.eduvirt.exceptions.NonComplianceValuesException;
import pl.lodz.p.it.eduvirt.exceptions.VnicProfileAlreadyExistsException;
import pl.lodz.p.it.eduvirt.exceptions.VnicProfileCurrentlyInUseException;
import pl.lodz.p.it.eduvirt.exceptions.VnicProfileEduvirtNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.VnicProfileOvirtNotFoundException;
import pl.lodz.p.it.eduvirt.repository.VlansRangeRepository;
import pl.lodz.p.it.eduvirt.repository.VnicProfileRepository;
import pl.lodz.p.it.eduvirt.service.ovirt.OVirtVnicProfileService;
import pl.lodz.p.it.eduvirt.service.VnicProfilePoolService;
import pl.lodz.p.it.eduvirt.util.I18n;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    public List<VnicProfilePoolMember> getVnicProfilesPool(Pageable pageable) {
        List<VlansRange> vlansRanges = vlansRangeRepository.findAll();
        List<Integer> vlansToInclude = vlansRanges.stream().flatMap(
                        vlansRange -> IntStream.rangeClosed(vlansRange.getFrom(), vlansRange.getTo()).boxed()
                ).toList();

        // Fetch data from eduVirt
        List<VnicProfilePoolMember> eduVirtVnicProfiles = vnicProfileRepository.findAllWithVlanIds(
                vlansToInclude, pageable
        );

        // Fetch data from oVirt (based on data from eduVirt) and check if selected vnic profiles are present in it
        return eduVirtVnicProfiles.parallelStream().peek(
                vnicProfile -> {
                    try {
                        VnicProfile ovirtVnicProfile = oVirtVnicProfileService.getVnicProfileById(vnicProfile.getId().toString());

                        vnicProfile.setNetworkId(ovirtVnicProfile.network().id());
                        compareValuesCompliance(vnicProfile.getVlanId(), ovirtVnicProfile.network().vlan().idAsInteger(), "vlan.id");
                        compareValuesCompliance(vnicProfile.getName(), ovirtVnicProfile.name(), "name");
                        compareValuesCompliance(vnicProfile.getNetworkName(), ovirtVnicProfile.network().name(), "network.name");
                        vnicProfile.setValid(true);
                    } catch (VnicProfileOvirtNotFoundException | NonComplianceValuesException e) {
                        vnicProfile.setValid(false);
                    }
                }
        ).toList();
    }

    private static <T> T compareValuesCompliance(T a, T b, String label) {
         if(Objects.equals(a, b)) {
             return a;
         }
         throw new NonComplianceValuesException(I18n.NON_COMPLIANCE_VALUES.replace("{}", label.toLowerCase()));
    }

    @Override
    public VnicProfilePoolMember getVnicProfileFromPool(UUID vnicProfileId) {
        Optional.ofNullable(oVirtVnicProfileService.getVnicProfileById(vnicProfileId.toString()))
                .orElseThrow(() -> new VnicProfileOvirtNotFoundException(vnicProfileId));

        return vnicProfileRepository.findById(vnicProfileId)
                .orElseThrow(() -> new VnicProfileEduvirtNotFoundException(vnicProfileId));
    }

    @Override
    public Optional<VnicProfilePoolMember> getFirstFreeVnicProfileFromPool() {
        // Fetch the available ranges of VLANs ids
        List<VlansRange> vlansRanges = vlansRangeRepository.findAll();
        List<Integer> vlansToInclude = vlansRanges.stream().flatMap(
                vlansRange -> IntStream.rangeClosed(vlansRange.getFrom(), vlansRange.getTo()).boxed()
        ).toList();

        List<VnicProfilePoolMember> freeVnicProfiles = vnicProfileRepository.findAllFreeVnicProfiles(vlansToInclude);

        for (VnicProfilePoolMember vnicProfile : freeVnicProfiles) {
            try {
                if (Objects.nonNull(oVirtVnicProfileService.getVnicProfileById(vnicProfile.getId().toString()))) {
                    return Optional.of(vnicProfile);
                }
            }
            catch (VnicProfileOvirtNotFoundException ignored) {}
        }

        return Optional.empty();
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
                new VnicProfilePoolMember(vnicProfileId, oVirtVnicProfile.network().vlan().idAsInteger(),
                        oVirtVnicProfile.name(), oVirtVnicProfile.network().name())
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

    private void changeVnicProfilePoolMemberStatus(UUID vnicProfileId, boolean setInUse) {
        Optional<VnicProfilePoolMember> vnicProfileOpt = vnicProfileRepository.findById(vnicProfileId);
        if (vnicProfileOpt.isEmpty()) throw new VnicProfileEduvirtNotFoundException(vnicProfileId);

        VnicProfilePoolMember vnicProfile = vnicProfileOpt.get();

//        if (vnicProfile.getInUse() == setInUse) {
//            throw new RuntimeException(vnicProfileId);
//        }

        vnicProfile.setInUse(setInUse);

        vnicProfileRepository.saveAndFlush(vnicProfile);
    }
}
