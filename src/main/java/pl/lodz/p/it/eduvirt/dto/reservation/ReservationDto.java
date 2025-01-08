package pl.lodz.p.it.eduvirt.dto.reservation;

import pl.lodz.p.it.eduvirt.dto.resource_group.ResourceGroupDto;
import pl.lodz.p.it.eduvirt.dto.team.TeamDto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationDto(
    UUID id,
    ResourceGroupDto resourceGroup,
    TeamDto team,
    LocalDateTime start,
    LocalDateTime end
) {}
