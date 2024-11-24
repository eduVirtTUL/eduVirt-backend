package pl.lodz.p.it.eduvirt.service;

import org.ovirt.engine.sdk4.types.User;

import java.util.List;
import java.util.UUID;

public interface OVirtUserService {

    List<User> getAllUsers();

    User getUserById(UUID userId);

}
