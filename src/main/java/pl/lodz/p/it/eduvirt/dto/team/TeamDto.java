package pl.lodz.p.it.eduvirt.dto.team;

import lombok.Builder;
import lombok.Value;
import java.util.List;
import java.util.UUID;

@Value
@Builder
public class TeamDto {
    UUID id;
    String name;
    String key;
    boolean active;
    List<UUID> users;
    UUID course;
}