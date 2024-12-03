package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.ovirt.engine.sdk4.Connection;
import org.ovirt.engine.sdk4.types.VnicProfile;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.service.OVirtVnicProfileService;
import pl.lodz.p.it.eduvirt.util.connection.ConnectionFactory;

@Service
@LoggerInterceptor
@RequiredArgsConstructor
public class OVirtVnicProfileServiceImpl implements OVirtVnicProfileService {
    private final ConnectionFactory connectionFactory;

    @Override
    public VnicProfile getVnicProfileById(String vnicProfileId) {
        try (Connection connection = connectionFactory.getConnection()) {
            return connection.systemService()
                    .vnicProfilesService()
                    .profileService(vnicProfileId)
                    .get()
                    .send()
                    .profile();
        } catch (Exception e) {
            //todo: error handling
            throw new RuntimeException(e);
        }
    }
}