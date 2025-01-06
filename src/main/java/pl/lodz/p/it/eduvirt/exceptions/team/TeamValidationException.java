package pl.lodz.p.it.eduvirt.exceptions.team;

import pl.lodz.p.it.eduvirt.util.I18n;

public class TeamValidationException extends TeamBaseException {

    public TeamValidationException() {
        super(I18n.TEAM_VALIDATION);
    }

    public TeamValidationException(String message) {
        super(message);
    }

    public TeamValidationException(Throwable cause) {
        super(I18n.TEAM_VALIDATION, cause);
    }

    public TeamValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
