package pl.lodz.p.it.eduvirt.dto.access_key;

import lombok.Data;
import java.util.UUID;

@Data
public abstract class AccessKeyDto {
    private UUID id;
    private String keyValue;
}
