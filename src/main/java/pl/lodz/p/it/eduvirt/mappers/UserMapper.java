package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import org.ovirt.engine.sdk4.types.User;
import pl.lodz.p.it.eduvirt.dto.OVirtUserDto;

@Mapper(componentModel = "spring")
public interface UserMapper {

    default OVirtUserDto ovirtUserToUserDto(User user) {
        return new OVirtUserDto(
                user.id(),
                user.name(),
                user.email()
        );
    }
}
