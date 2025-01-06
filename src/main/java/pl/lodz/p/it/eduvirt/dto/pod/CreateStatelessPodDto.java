package pl.lodz.p.it.eduvirt.dto.pod;

import java.util.UUID;

public record CreateStatelessPodDto(
        UUID teamId,
        UUID resourceGroupPoolId
) {
}
