package pl.lodz.p.it.eduvirt.dto.metric;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import pl.lodz.p.it.eduvirt.entity.Metric;

public record CreateMetricDto(
        @NotBlank(message = "metrics.validation.name.blank")
        @Size(min = 8, message = "metrics.validation.name.too.short")
        @Size(max = 64, message = "metrics.validation.name.too.long")
        @Pattern(regexp = "[A-Za-z0-9_]{8,64}", message = "metrics.validation.regex.not.met")
        String name,

        @NotNull(message = "metrics.validation.null.category")
        Metric.MetricCategory category
) {}
