package pl.lodz.p.it.eduvirt.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Value;

@Value
public class EmailDto {
    @Email(message = "course.validation.email.invalid")
    @NotBlank(message = "course.validation.email.blank")
    String email;
}
