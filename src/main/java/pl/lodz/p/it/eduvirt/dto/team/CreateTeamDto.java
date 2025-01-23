package pl.lodz.p.it.eduvirt.dto.team;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import pl.lodz.p.it.eduvirt.validation.team.TeamKeyFormat;

import java.util.UUID;

@Value
@Builder
public class CreateTeamDto {
    @Size(min=1, max=50, message = "teams.validation.name.invalid")
    String name;

    @TeamKeyFormat
    String keyValue;

    @NotNull(message = "teams.validation.null.rg.id")
    UUID courseId;

    @Min(value = 2, message = "teams.validation.max.size.invalid")
    @Max(value = 10, message = "teams.validation.max.size.invalid") 
    int maxSize;
}