package pl.lodz.p.it.eduvirt.exceptions;

import lombok.Setter;
import pl.lodz.p.it.eduvirt.exceptions.general.BadRequestException;
import pl.lodz.p.it.eduvirt.util.I18n;

import java.util.Set;

@Setter
public class ApplicationConstraintViolationException extends BadRequestException {

    private final Set<String> violations;

    public ApplicationConstraintViolationException(String message, Set<String> violations) {
        super(message, I18n.CONSTRAINT_VIOLATION_EXCEPTION);
        this.violations = violations;
    }
}
