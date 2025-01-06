package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import org.ovirt.engine.sdk4.types.User;
import pl.lodz.p.it.eduvirt.dto.user.OVirtUserWithPermissionsDto;
import pl.lodz.p.it.eduvirt.dto.permission.OvirtUserPermissionDto;
import pl.lodz.p.it.eduvirt.dto.user.OvirtUserDto;

@Mapper(componentModel = "spring")
public interface UserMapper {

    default OVirtUserWithPermissionsDto ovirtUserWithPermissionsToUserDto(User user) {
        return new OVirtUserWithPermissionsDto(
                user.id(),
                user.name(),
                user.userName(),
                user.lastName(),
                user.email(),
                user.department(),
                user.permissions().stream().map(permission ->
                        new OvirtUserPermissionDto(
                                permission.id(),
                                permission.role() != null ? permission.role().id() : null,
                                permission.vm() != null ? permission.vm().id() : null,
                                permission.group() != null ? permission.group().id() : null
                        )).toList()
        );
    }

    default OvirtUserDto ovirtUserToUserDto(User user) {
        return new OvirtUserDto(
                user.id(),
                user.name(),
                user.userName(),
                user.lastName(),
                user.email(),
                user.department()
        );
    }
}
