package pl.lodz.p.it.eduvirt.dto.user;

import pl.lodz.p.it.eduvirt.dto.permission.OvirtUserPermissionDto;

import java.util.List;

public record OVirtUserWithPermissionsDto(String id, String name, String userName, String lastName, String email, String department, List<OvirtUserPermissionDto> permissions) {
}
