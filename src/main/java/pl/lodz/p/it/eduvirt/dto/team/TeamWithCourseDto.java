package pl.lodz.p.it.eduvirt.dto.team;

import lombok.Builder;
import pl.lodz.p.it.eduvirt.dto.course.CourseBasicDto;

import java.util.List;
import java.util.UUID;

@Builder
public record TeamWithCourseDto(UUID id, String name, boolean active, int maxSize, List<UUID> users,
                                CourseBasicDto course) {
}