package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.ovirt.engine.sdk4.Connection;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.dto.vnic_profile.OvirtVnicProfileDto;
import pl.lodz.p.it.eduvirt.entity.eduvirt.network.VnicProfilePoolMember;
import pl.lodz.p.it.eduvirt.exceptions.EntityAlreadyException;
import pl.lodz.p.it.eduvirt.exceptions.VnicProfileEduvirtNotFoundException;
import pl.lodz.p.it.eduvirt.mappers.VnicProfileMapper;
import pl.lodz.p.it.eduvirt.repository.eduvirt.VnicProfileRepository;
import pl.lodz.p.it.eduvirt.service.OVirtVnicProfileService;
import pl.lodz.p.it.eduvirt.util.connection.ConnectionFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@LoggerInterceptor
@RequiredArgsConstructor
public class OVirtVnicProfileServiceImpl implements OVirtVnicProfileService {

    private final ConnectionFactory connectionFactory;
    private final VnicProfileMapper vnicProfileMapper;
    private final VnicProfileRepository vnicProfileRepository;

    @Override
    @Transactional(propagation = Propagation.NEVER)
    public List<OvirtVnicProfileDto> fetchVnicProfiles() {
        try (Connection connection = connectionFactory.getConnection()) {
            return connection.systemService()
                    .vnicProfilesService()
                    .list()
                    .follow("network")
                    .send()
                    .profiles()
                    .stream()
                    .map(vnicProfileMapper::ovirtVnicProfileToDto)
                    .toList();
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
        if (vnicProfileRepository.findById(vnicProfileId).isPresent()) throw new EntityAlreadyException(vnicProfileId.toString());

        return vnicProfileRepository.save(new VnicProfilePoolMember(vnicProfileId, false));
    }

    @Override
    @Transactional
    public void removeVnicProfileFromPool(UUID vnicProfileId) {
        if (vnicProfileRepository.findById(vnicProfileId).isEmpty()) throw new VnicProfileEduvirtNotFoundException(vnicProfileId.toString());

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

        vnicProfileRepository.save(vnicProfile);
    }
}
