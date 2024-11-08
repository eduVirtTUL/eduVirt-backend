package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.ovirt.engine.sdk4.Connection;
import org.ovirt.engine.sdk4.types.VnicProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.dto.VnicProfileDto;
import pl.lodz.p.it.eduvirt.dto.VnicProfilePoolDto;
import pl.lodz.p.it.eduvirt.entity.network.VnicProfilePool;
import pl.lodz.p.it.eduvirt.mappers.VnicProfileMapper;
import pl.lodz.p.it.eduvirt.repository.VnicProfileRepository;
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
    public List<VnicProfileDto> fetchVnicProfiles() {
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
    public List<VnicProfilePoolDto> showVnicProfilePool() {
        return vnicProfileRepository.findAll().stream()
                .map(VnicProfilePoolDto::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public boolean expandVnicProfilePool(UUID vnicProfileId) {
        //todo maybe change returning boolean to throwing an exception "entity exists"
        if (vnicProfileRepository.findById(vnicProfileId).isPresent()) return false;

        vnicProfileRepository.save(new VnicProfilePool(vnicProfileId, false));
        return true;
    }
}
