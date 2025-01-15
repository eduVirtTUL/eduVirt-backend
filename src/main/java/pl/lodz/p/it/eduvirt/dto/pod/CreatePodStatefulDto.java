package pl.lodz.p.it.eduvirt.dto.pod;

import java.util.UUID;

public record CreatePodStatefulDto(
        UUID teamId,
        UUID resourceGroupId,
        Integer maxRent
) {}