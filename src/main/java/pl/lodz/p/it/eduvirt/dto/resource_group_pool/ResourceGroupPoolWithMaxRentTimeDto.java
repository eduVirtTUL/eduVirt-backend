package pl.lodz.p.it.eduvirt.dto.resource_group_pool;

import java.util.UUID;

public record ResourceGroupPoolWithMaxRentTimeDto(UUID id, String name, String description, int maxRentTime) {
}
