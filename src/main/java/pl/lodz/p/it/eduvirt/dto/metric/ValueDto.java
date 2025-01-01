package pl.lodz.p.it.eduvirt.dto.metric;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.PositiveOrZero;

public record ValueDto(
        @PositiveOrZero(message = "metrics.validation.value.negative")
        @Max(value = 9223372036854775807L, message = "metrics.validation.value.too.large")
        double value
) {}
