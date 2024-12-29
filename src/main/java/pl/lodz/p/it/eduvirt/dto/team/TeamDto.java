package pl.lodz.p.it.eduvirt.dto.team;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
@AllArgsConstructor
public class TeamDto {
    UUID id;
    String name;
    boolean active;
    int maxSize;
    List<UUID> users;
}