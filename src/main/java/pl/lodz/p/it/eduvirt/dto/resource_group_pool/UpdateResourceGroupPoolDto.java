package pl.lodz.p.it.eduvirt.dto.resource_group_pool;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UpdateResourceGroupPoolDto(
        @NotBlank @Size(min = 1, max = 50) String name,
        @Size(max = 1000) String description,
        @NotNull int maxRent,
        @NotNull int gracePeriod,
        @NotNull int maxRentTime) {
}
