package pl.lodz.p.it.eduvirt.validation.team;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = TeamKeyFormatValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TeamKeyFormat {
    String message() default "teams.validation.key.format.invalid";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}