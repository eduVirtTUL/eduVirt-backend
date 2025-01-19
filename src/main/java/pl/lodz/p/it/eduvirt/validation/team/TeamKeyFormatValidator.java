package pl.lodz.p.it.eduvirt.validation.team;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class TeamKeyFormatValidator implements ConstraintValidator<TeamKeyFormat, String> {
    private static final String KEY_PATTERN = "^[A-Za-z0-9_-]{4,20}$";
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return value.matches(KEY_PATTERN);
    }
}