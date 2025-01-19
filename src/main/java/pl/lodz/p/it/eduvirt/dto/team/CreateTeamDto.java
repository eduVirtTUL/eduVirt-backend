package pl.lodz.p.it.eduvirt.dto.team;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class CreateTeamDto {
    @Size(min=1, max=50, message = "teams.validation.name.invalid")
    String name;

    @Size(min=4, max=20, message = "teams.validation.key.value.invalid")
    String keyValue;

    @NotNull(message = "teams.validation.null.rg.id")
    UUID courseId;

    @Min(value = 2, message = "teams.validation.max.size.invalid")
    @Max(value = 10, message = "teams.validation.max.size.invalid") 
    int maxSize;
}