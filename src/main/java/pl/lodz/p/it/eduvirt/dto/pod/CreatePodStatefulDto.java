package pl.lodz.p.it.eduvirt.dto.pod;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreatePodStatefulDto(
        @NotNull(message = "pods.validation.null.team.id")
        UUID teamId,

        @NotNull(message = "pods.validation.null.rg.id")
        UUID resourceGroupId,

        @Size(min=0, max=1000, message = "pods.validation.max.rent.invalid")
        Integer maxRent
) {}