package pl.lodz.p.it.eduvirt.exceptions.team;

import pl.lodz.p.it.eduvirt.util.I18n;

public class TeamSizeException extends TeamValidationException {

    public TeamSizeException() {
        super(I18n.INCORRECT_TEAM_SIZE);
    }

    public TeamSizeException(String message) {
        super(message);
    }

    public TeamSizeException(Throwable cause) {
        super(I18n.INCORRECT_TEAM_SIZE, cause);
    }

    public TeamSizeException(String message, Throwable cause) {
        super(message, cause);
    }
}
