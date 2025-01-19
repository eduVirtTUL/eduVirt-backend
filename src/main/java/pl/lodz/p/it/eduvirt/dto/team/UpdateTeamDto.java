package pl.lodz.p.it.eduvirt.dto.team;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateTeamDto(
        @Size(min = 1, max = 50, message = "teams.validation.name.invalid")
        String name,

        @Min(value = 2, message = "teams.validation.max.size.invalid")
        @Max(value = 10, message = "teams.validation.max.size.invalid")
        int maxSize,

        @NotNull(message = "teams.validation.null.active")
        boolean active
) {}
