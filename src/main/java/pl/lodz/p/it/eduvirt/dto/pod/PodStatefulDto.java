package pl.lodz.p.it.eduvirt.dto.pod;

import java.util.UUID;

public record PodStatefulDto(
    UUID id,
    UUID teamId,
    UUID courseId,
    UUID resourceGroupId,
    Integer maxRent
) {}
