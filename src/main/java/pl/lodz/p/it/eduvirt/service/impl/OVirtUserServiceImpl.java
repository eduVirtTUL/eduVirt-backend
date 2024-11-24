package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.ovirt.engine.sdk4.Connection;
import org.ovirt.engine.sdk4.services.SystemService;
import org.ovirt.engine.sdk4.types.User;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.exceptions.UserNotFoundException;
import pl.lodz.p.it.eduvirt.service.OVirtUserService;
import pl.lodz.p.it.eduvirt.util.connection.ConnectionFactory;

import java.util.List;
import java.util.UUID;

@Service
@LoggerInterceptor
@RequiredArgsConstructor
public class OVirtUserServiceImpl implements OVirtUserService {

    private final ConnectionFactory connectionFactory;

    @Override
    public List<User> getAllUsers() {
        try {
            Connection connection = connectionFactory.getConnection();
            SystemService systemService = connection.systemService();

            return systemService.usersService().list().send().users();
        } catch (org.ovirt.engine.sdk4.Error error) {
            throw new UserNotFoundException();
        }
    }

    @Override
    public User getUserById(UUID userId) {
        try {
            Connection connection = connectionFactory.getConnection();
            SystemService systemService = connection.systemService();

            return systemService.usersService().userService(userId.toString()).get().send().user();
        } catch (org.ovirt.engine.sdk4.Error error) {
            throw new UserNotFoundException();
        }
    }
}
