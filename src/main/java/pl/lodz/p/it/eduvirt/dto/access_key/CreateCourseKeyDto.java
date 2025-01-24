package pl.lodz.p.it.eduvirt.dto.access_key;

import lombok.Value;
import pl.lodz.p.it.eduvirt.validation.team.TeamKeyFormat;

@Value
public class CreateCourseKeyDto {
    @TeamKeyFormat
    String keyValue;
}