package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import org.ovirt.engine.sdk4.types.Permission;
import pl.lodz.p.it.eduvirt.dto.permission.OvirtUserPermissionDto;
import pl.lodz.p.it.eduvirt.dto.permission.VmPermissionDto;

@Mapper(componentModel = "spring")
public interface PermissionMapper {

    default VmPermissionDto ovirtVmPermissionToPermissionDto(Permission permission) {
        return new VmPermissionDto(
                permission.id(),
                permission.user() != null ? permission.user().id() : null,
                permission.role() != null ? permission.role().id() : null,
                permission.group() != null ? permission.group().id() : null
        );
    }

    default OvirtUserPermissionDto ovirtUserPermissionToPermissionDto(Permission permission) {
        return new OvirtUserPermissionDto(
                permission.id(),
                permission.role() != null ? permission.role().id() : null,
                permission.vm() != null ? permission.vm().id() : null,
                permission.group() != null ? permission.group().id() : null
        );
    }
}