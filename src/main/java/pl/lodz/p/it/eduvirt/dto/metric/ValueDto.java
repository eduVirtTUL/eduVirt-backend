package pl.lodz.p.it.eduvirt.dto.metric;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

public record ValueDto(
        @NotNull(message = "metrics.validation.id.null")
        UUID metricValueId,

        @PositiveOrZero(message = "metrics.validation.version.negative")
        Long version,

        @PositiveOrZero(message = "metrics.validation.value.negative")
        @Max(value = 9223372036854775807L, message = "metrics.validation.value.too.large")
        double value
) {}
