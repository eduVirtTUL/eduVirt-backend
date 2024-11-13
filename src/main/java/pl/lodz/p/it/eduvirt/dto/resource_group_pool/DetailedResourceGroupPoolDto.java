package pl.lodz.p.it.eduvirt.dto.resource_group_pool;

import pl.lodz.p.it.eduvirt.dto.course.CourseDto;

import java.util.UUID;

public record DetailedResourceGroupPoolDto(UUID id, String name, CourseDto course) {
}
