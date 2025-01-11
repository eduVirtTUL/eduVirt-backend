package pl.lodz.p.it.eduvirt.dto.pod;

import java.util.UUID;

public record CreatePodStatelessDto(
        UUID teamId,
        UUID resourceGroupPoolId
) {
}
