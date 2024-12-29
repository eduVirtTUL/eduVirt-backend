package pl.lodz.p.it.eduvirt.exceptions.team;

import pl.lodz.p.it.eduvirt.util.I18n;

public class TeamAlreadyExistsException extends TeamValidationException {

    public TeamAlreadyExistsException() {
        super(I18n.TEAM_ALREADY_EXISTS);
    }

    public TeamAlreadyExistsException(String message) {
        super(message);
    }

    public TeamAlreadyExistsException(Throwable cause) {
        super(I18n.TEAM_ALREADY_EXISTS, cause);
    }

    public TeamAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
