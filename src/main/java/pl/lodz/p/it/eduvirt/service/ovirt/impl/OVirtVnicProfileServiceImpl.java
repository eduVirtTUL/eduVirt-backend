package pl.lodz.p.it.eduvirt.service.ovirt.impl;

import lombok.RequiredArgsConstructor;
import org.ovirt.engine.sdk4.Connection;
import org.ovirt.engine.sdk4.types.VnicProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.service.ovirt.OVirtVnicProfileService;
import pl.lodz.p.it.eduvirt.util.connection.ConnectionFactory;

import java.util.List;

@Service
@LoggerInterceptor
@RequiredArgsConstructor
@Transactional(propagation = Propagation.NOT_SUPPORTED)
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

    @Override
    public List<VnicProfile> getVnicProfiles() {
        try (Connection connection = connectionFactory.getConnection()) {
            return connection.systemService()
                    .vnicProfilesService()
                    .list()
                    .follow("network")
                    .send()
                    .profiles();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}