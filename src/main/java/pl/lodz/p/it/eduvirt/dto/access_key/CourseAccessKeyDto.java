package pl.lodz.p.it.eduvirt.dto.access_key;

import lombok.Data;

import java.util.UUID;

@Data
public class CourseAccessKeyDto {
    private UUID id;
    private String keyValue;
    private UUID courseId;
}