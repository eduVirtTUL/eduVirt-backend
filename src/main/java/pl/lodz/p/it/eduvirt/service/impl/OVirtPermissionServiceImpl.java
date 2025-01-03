package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.ovirt.engine.sdk4.Connection;
import org.ovirt.engine.sdk4.builders.PermissionBuilder;
import org.ovirt.engine.sdk4.services.AssignedPermissionsService;
import org.ovirt.engine.sdk4.services.SystemService;
import org.ovirt.engine.sdk4.types.Permission;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.exceptions.permission.PermissionNotFoundException;
import pl.lodz.p.it.eduvirt.service.OVirtPermissionService;
import pl.lodz.p.it.eduvirt.util.connection.ConnectionFactory;

import java.util.List;
import java.util.UUID;

@Service
@LoggerInterceptor
@RequiredArgsConstructor
public class OVirtPermissionServiceImpl implements OVirtPermissionService {

    private final ConnectionFactory connectionFactory;

    @Override
    public List<Permission> findPermissionsByVmId(UUID vmId) {
        try {
            Connection connection = connectionFactory.getConnection();
            SystemService systemService = connection.systemService();

            AssignedPermissionsService permissionsService = systemService
                    .vmsService()
                    .vmService(vmId.toString())
                    .permissionsService();

            return permissionsService.list().send().permissions();

        } catch (org.ovirt.engine.sdk4.Error error) {
            throw new PermissionNotFoundException("Permission for vm %s could not be found".formatted(vmId));
        }
    }

    @Override
    public List<Permission> findPermissionsByUserId(UUID userId) {
        try {
            Connection connection = connectionFactory.getConnection();
            SystemService systemService = connection.systemService();

            AssignedPermissionsService permissionsService = systemService
                    .usersService()
                    .userService(userId.toString())
                    .permissionsService();

            return permissionsService.list().send().permissions();

        } catch (org.ovirt.engine.sdk4.Error error) {
            throw new PermissionNotFoundException("Permission for user %s could not be found".formatted(userId));
        }
    }

    @Override
    public void assignPermissionToVmToUser(UUID vmId, UUID userId, String role) {
        try {
            Connection connection = connectionFactory.getConnection();
            SystemService systemService = connection.systemService();

            AssignedPermissionsService permissionsService = systemService
                    .vmsService()
                    .vmService(vmId.toString())
                    .permissionsService();

            permissionsService.add()
                    .permission(
                            new PermissionBuilder()
                                    .user(systemService.usersService().userService(userId.toString()).get().send().user())
                                    .role(
                                            new org.ovirt.engine.sdk4.builders.RoleBuilder()
                                                    .id(role)
                                                    .build()
                                    )
                                    .build()
                    )
                    .send();

        } catch (org.ovirt.engine.sdk4.Error error) {
            throw new PermissionNotFoundException("Permission for user %s on vm %s could not be found".formatted(userId, vmId));
        }
    }

    @Override
    public void revokePermissionToVmFromUser(UUID permissionId) {
        try {
            Connection connection = connectionFactory.getConnection();
            SystemService systemService = connection.systemService();

            systemService
                    .permissionsService().permissionService(permissionId.toString()).remove().send();

        } catch (org.ovirt.engine.sdk4.Error error) {
            throw new PermissionNotFoundException("Permission with id %s could not be found".formatted(permissionId));
        }
    }
}
