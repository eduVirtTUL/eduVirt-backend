package pl.lodz.p.it.eduvirt.dto.metric;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

public record CreateMetricValueDto(
        @NotNull(message = "metrics.validation.null.metric.id")
        UUID metricId,

        @PositiveOrZero(message = "metrics.validation.value.negative")
        double value
) {}
