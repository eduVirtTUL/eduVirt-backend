package pl.lodz.p.it.eduvirt.dto.resource_group_pool;

import java.util.UUID;

public record CreateRGPoolDto(String name, UUID courseId) {
}
