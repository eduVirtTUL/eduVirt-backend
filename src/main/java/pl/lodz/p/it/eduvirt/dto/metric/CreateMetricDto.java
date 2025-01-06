package pl.lodz.p.it.eduvirt.dto.metric;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateMetricDto(
        @NotBlank(message = "metrics.validation.name.blank")
        @Size(min = 8, message = "metrics.validation.name.too.short")
        @Size(max = 64, message = "metrics.validation.name.too.long")
        String name
) {}
