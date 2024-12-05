package pl.lodz.p.it.eduvirt.dto.pod;

import java.util.UUID;

public record CreatePodStatefulDto(
        UUID resourceGroupId,
        UUID teamId,
        UUID courseId,
        String clusterId
) {
}