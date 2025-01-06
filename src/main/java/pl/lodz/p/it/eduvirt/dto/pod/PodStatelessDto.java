package pl.lodz.p.it.eduvirt.dto.pod;

import lombok.Builder;

import java.util.UUID;

@Builder
public record PodStatelessDto(
        UUID teamId,
        UUID resourceGroupPoolId
) {
}
