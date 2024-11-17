package pl.lodz.p.it.eduvirt.dto.metric;

import java.util.UUID;

public record CreateMetricValueDto(
        UUID metricId,
        double value
) {}
