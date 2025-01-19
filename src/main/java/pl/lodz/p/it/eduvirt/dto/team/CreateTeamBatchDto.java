package pl.lodz.p.it.eduvirt.dto.team;

import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CreateTeamBatchDto {
    @NotNull(message = "teams.validation.null.course.id")
    UUID courseId;

    @Size(min=1, max=40, message = "teams.validation.prefix.invalid")
    @Pattern(regexp = "^[A-Za-z0-9-]+$", message = "teams.validation.prefix.format")
    String prefix;

    @Min(value = 2, message = "teams.validation.team.size.min")
    @Max(value = 8, message = "teams.validation.team.size.max")
    int teamSize;

    @Min(value = 2, message = "teams.validation.team.count.min")
    @Max(value = 15, message = "teams.validation.team.count.max")
    int numberOfTeams;
}