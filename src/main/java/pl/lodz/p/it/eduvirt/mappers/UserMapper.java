package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import org.ovirt.engine.sdk4.types.User;
import pl.lodz.p.it.eduvirt.dto.OVirtUserDto;
import pl.lodz.p.it.eduvirt.dto.permission.UserPermissionDto;

@Mapper(componentModel = "spring")
public interface UserMapper {

    default OVirtUserDto ovirtUserToUserDto(User user) {
        return new OVirtUserDto(
                user.id(),
                user.name(),
                user.email(),
                user.permissions().stream().map(permission ->
                        new UserPermissionDto(
                                permission.id(),
                                permission.role() != null ? permission.role().id() : null,
                                permission.vm() != null ? permission.vm().id() : null,
                                permission.group() != null ? permission.group().id() : null
                        )).toList()
        );
    }
}
