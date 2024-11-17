package pl.lodz.p.it.eduvirt.service;

import org.ovirt.engine.sdk4.types.Permission;

import java.util.List;
import java.util.UUID;

public interface OVirtAssignedPermissionService {

    List<Permission> findPermissionsByVmId(UUID vmId);

    List<Permission> findPermissionsByUserId(UUID userId);

    void assignPermissionToVmToUser(UUID vmId, UUID userId, String role);

    void revokePermissionToVmFromUser(UUID permissionId);
}
