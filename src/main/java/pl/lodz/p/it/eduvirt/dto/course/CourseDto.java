package pl.lodz.p.it.eduvirt.dto.course;

import pl.lodz.p.it.eduvirt.entity.key.CourseType;

public record CourseDto(String id, String name, String description, CourseType courseType) {
}
