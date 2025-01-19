package pl.lodz.p.it.eduvirt.dto.team;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import pl.lodz.p.it.eduvirt.dto.user.UserDto;

import java.util.List;
import java.util.UUID;

@Builder
@AllArgsConstructor
@Getter
public class TeamWithKeyDto {
    UUID id;
    String name;
    boolean active;
    int maxSize;
    List<UserDto> users;
    String keyValue;
}