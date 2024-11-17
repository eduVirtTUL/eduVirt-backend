package pl.lodz.p.it.eduvirt.dto.metric;

import java.util.UUID;

public record MetricValueDto(
    UUID id,
    String name,
    double value
) {}
