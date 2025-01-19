package pl.lodz.p.it.eduvirt.dto.access_key;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class JoinTeamKeyDto {
    @NotBlank
    @Size(min=4, max=20, message = "teams.validation.key.value.invalid")
    String keyValue;
}
