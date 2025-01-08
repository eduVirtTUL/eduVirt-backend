package pl.lodz.p.it.eduvirt.dto.reservation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.lodz.p.it.eduvirt.dto.resource_group.ResourceGroupDto;
import pl.lodz.p.it.eduvirt.dto.team.TeamDto;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDetailsDto {

    private UUID id;
    private ResourceGroupDto resourceGroup;
    private TeamDto team;
    private LocalDateTime start;
    private LocalDateTime end;
    private boolean automaticStartup;
}
