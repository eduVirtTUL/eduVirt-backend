package pl.lodz.p.it.eduvirt.dto.team;

import lombok.Builder;
import lombok.Value;
import java.util.UUID;

@Value
@Builder
public class CreateTeamDto {
    String name;
    String keyValue;
    UUID courseId;
    int maxSize;
}