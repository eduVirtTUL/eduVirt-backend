package pl.lodz.p.it.eduvirt.dto.pod;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreatePodStatefulDto(
        @NotNull(message = "pods.validation.null.team.id")
        UUID teamId,

        @NotNull(message = "pods.validation.null.rg.id")
        UUID resourceGroupId,

        @Min(value = 1, message = "pods.validation.min.rent.invalid")
        @Max(value = 100, message = "pods.validation.min.rent.invalid")
        Integer maxRent
) {
}