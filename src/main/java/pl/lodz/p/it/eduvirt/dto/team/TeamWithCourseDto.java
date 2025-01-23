package pl.lodz.p.it.eduvirt.dto.team;

import lombok.Builder;
import pl.lodz.p.it.eduvirt.dto.course.CourseBasicDto;
import pl.lodz.p.it.eduvirt.dto.user.UserDto;

import java.util.List;
import java.util.UUID;

@Builder
public record TeamWithCourseDto(UUID id, String name, int maxSize, List<UserDto> users,
                                CourseBasicDto course) {
}