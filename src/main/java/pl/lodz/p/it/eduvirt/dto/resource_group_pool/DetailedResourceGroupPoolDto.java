package pl.lodz.p.it.eduvirt.dto.resource_group_pool;

import pl.lodz.p.it.eduvirt.dto.course.CourseDto;
import pl.lodz.p.it.eduvirt.dto.resource_group.ResourceGroupDto;

import java.util.List;
import java.util.UUID;

public record DetailedResourceGroupPoolDto(UUID id, String name, CourseDto course,
                                           List<ResourceGroupDto> resourceGroups, int maxRent, int gracePeriod,
                                           String description, int maxRentTime) {
}
