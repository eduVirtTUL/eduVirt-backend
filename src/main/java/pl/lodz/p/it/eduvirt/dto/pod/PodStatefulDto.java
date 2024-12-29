package pl.lodz.p.it.eduvirt.dto.pod;

import lombok.Builder;

import java.util.UUID;

@Builder
public record PodStatefulDto(
        UUID id,
        UUID resourceGroupId,
        UUID teamId,
        UUID courseId
) {
}
