package pl.lodz.p.it.eduvirt.dto.team;

import lombok.Value;

@Value
public class UpdateTeamDto {
    String name;
    int maxSize;
    boolean active;
}
