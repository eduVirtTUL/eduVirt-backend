package pl.lodz.p.it.eduvirt.dto.resource_group;

public record ResourceGroupDto(String id, String name, String description, boolean stateless, int maxRentTime) {
}
