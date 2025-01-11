package pl.lodz.p.it.eduvirt.dto.metric;

import pl.lodz.p.it.eduvirt.entity.Metric;

import java.util.UUID;

public record MetricDto(
        UUID id,
        String name,
        Metric.MetricCategory category
) {}
