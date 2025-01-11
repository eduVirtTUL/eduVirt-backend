package pl.lodz.p.it.eduvirt.dto.resource_group_pool;

public record UpdateResourceGroupPoolDto(String description, int maxRent, int gracePeriod, int maxRentTime) {
}
