package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import pl.lodz.p.it.eduvirt.dto.user.UserDto;
import pl.lodz.p.it.eduvirt.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto userToDto(User user);

    User dtoToUser(UserDto userDto);
}
