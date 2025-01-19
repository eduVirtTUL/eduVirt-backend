package pl.lodz.p.it.eduvirt.dto.pod;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreatePodStatelessDto(
        @NotNull(message = "pods.validation.null.team.id")

        UUID teamId,

        @NotNull(message = "pods.validation.null.rg.id")
        UUID resourceGroupPoolId
) {
}
