package pl.lodz.p.it.eduvirt.dto.course;

import java.util.UUID;

public record CreateCourseDto(String name, String description, boolean teamBased, UUID clusterId) {
}
