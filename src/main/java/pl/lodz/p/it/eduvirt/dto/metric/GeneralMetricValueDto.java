package pl.lodz.p.it.eduvirt.dto.metric;

import java.util.UUID;

public record GeneralMetricValueDto(
        UUID id,
        Long version,
        MetricDto metric,
        double value
) {}
