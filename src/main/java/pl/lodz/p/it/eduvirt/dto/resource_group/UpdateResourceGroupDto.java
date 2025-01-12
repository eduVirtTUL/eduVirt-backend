package pl.lodz.p.it.eduvirt.dto.resource_group;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateResourceGroupDto(@NotBlank @Size(min = 1, max = 50) String name,
                                     @NotBlank @Size(min = 1, max = 1000) String description,
                                     @NotNull int maxRentTime) {
}
