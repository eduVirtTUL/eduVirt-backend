package pl.lodz.p.it.eduvirt.dto.team;

import jakarta.validation.constraints.NotNull;
import lombok.Value;
import java.util.UUID;

@Value
public class LeaveTeamDto {
    @NotNull(message = "teams.validation.team.id.null")
    UUID teamId;
}