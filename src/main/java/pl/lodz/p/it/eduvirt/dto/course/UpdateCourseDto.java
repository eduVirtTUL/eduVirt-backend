package pl.lodz.p.it.eduvirt.dto.course;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UpdateCourseDto(
        @NotBlank
        @Size(min = 1, max = 50)
        String name,
        @Size(max = 1000) String description,
        @Size(max = 1000) String externalLink) {
}
