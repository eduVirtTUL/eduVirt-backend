package pl.lodz.p.it.eduvirt.service.ovirt;

import org.ovirt.engine.sdk4.types.User;

import java.util.List;
import java.util.UUID;

public interface OVirtUserService {

    List<User> getAllUsersWithPermissions();

    List<User> getAllUsers();

    User getUserById(UUID userId);

    User getUserByPrincipal(String principal);

}
