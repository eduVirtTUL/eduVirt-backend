package pl.lodz.p.it.eduvirt.dto.course;

import lombok.Value;
import java.util.UUID;

@Value
public class CourseBasicDto {
    UUID id;
    String name;
    String description;
    String courseType;
}