package pl.lodz.p.it.eduvirt.dto;

import pl.lodz.p.it.eduvirt.dto.permission.UserPermissionDto;

import java.util.List;

public record OVirtUserDto(String id, String userName, String email, List<UserPermissionDto> permissions) {
}
