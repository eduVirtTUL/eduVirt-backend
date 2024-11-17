package pl.lodz.p.it.eduvirt.dto.metric;

import java.util.UUID;

public record MetricDto(
        UUID id,
        String name
) {}
