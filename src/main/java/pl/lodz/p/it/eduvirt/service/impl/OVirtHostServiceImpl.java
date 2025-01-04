package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.ovirt.engine.sdk4.Connection;
import org.ovirt.engine.sdk4.services.HostService;
import org.ovirt.engine.sdk4.services.SystemService;
import org.ovirt.engine.sdk4.types.Host;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.service.OVirtHostService;
import pl.lodz.p.it.eduvirt.util.connection.ConnectionFactory;

import java.util.UUID;

@Service
@LoggerInterceptor
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED)
public class OVirtHostServiceImpl implements OVirtHostService {

    private final ConnectionFactory connectionFactory;

    @Override
    public Host findHostById(UUID hostId) {
        try {
            Connection connection = connectionFactory.getConnection();
            SystemService systemService = connection.systemService();

            HostService hostService = systemService.hostsService().
                    hostService(hostId.toString());

            return hostService.get().send().host();
        } catch (org.ovirt.engine.sdk4.Error error) {
            throw new RuntimeException("Host: %s not found");
        }
    }
}
