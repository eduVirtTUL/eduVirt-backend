package pl.lodz.p.it.eduvirt.dto.resource_group_pool;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.UUID;

@Builder
public record CreateRGPoolDto(
        @NotBlank
        @Size(min = 1, max = 100)
        String name,
        @NotNull
        UUID courseId,
        @Min(0)
        int maxRent,
        @Min(0)
        int gracePeriod,
        @Size(max = 1000)
        String description,
        @Min(0)
        int maxRentTime) {
}
