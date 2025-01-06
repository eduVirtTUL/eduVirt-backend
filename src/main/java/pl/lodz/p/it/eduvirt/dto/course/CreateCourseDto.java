package pl.lodz.p.it.eduvirt.dto.course;

import pl.lodz.p.it.eduvirt.entity.key.CourseType;

public record CreateCourseDto(String name, String description, CourseType courseType) {
}
