package pl.lodz.p.it.eduvirt.dto.pod;

import java.util.UUID;

public record PodStatelessDto(
    UUID id,
    UUID teamId,
    UUID courseId,
    UUID resourceGroupPoolId
) {}
