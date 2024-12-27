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
    boolean active;
    int maxSize;
    List<UUID> users;
    UUID courseId;
    String keyValue;
}