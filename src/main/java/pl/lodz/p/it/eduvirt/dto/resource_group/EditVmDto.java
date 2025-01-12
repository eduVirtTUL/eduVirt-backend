package pl.lodz.p.it.eduvirt.dto.resource_group;

import jakarta.validation.constraints.NotNull;

public record EditVmDto(@NotNull boolean hidden) {
}
