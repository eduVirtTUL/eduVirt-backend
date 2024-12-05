package pl.lodz.p.it.eduvirt.dto.pod;

import java.util.UUID;

public record PodStatefulDto(
        UUID id,
        UUID resourceGroupId,
        UUID teamId,
        UUID courseId,
        String clusterId
) {
}
