package pl.lodz.p.it.eduvirt.dto.access_key;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
public class TeamAccessKeyDto extends AccessKeyDto {
    private UUID teamId;
    private UUID courseId;
}