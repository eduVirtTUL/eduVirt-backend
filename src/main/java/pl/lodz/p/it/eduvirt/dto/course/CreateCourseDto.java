package pl.lodz.p.it.eduvirt.dto.course;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pl.lodz.p.it.eduvirt.entity.key.CourseType;

import java.util.UUID;

public record CreateCourseDto(
        @NotBlank
        @Size(min = 1, max = 50)
        String name,
        @NotBlank
        @Size(min = 1, max = 1000)
        String description,
        @NotNull
        CourseType courseType,
        @NotNull
        UUID clusterId) {
}
