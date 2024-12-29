package pl.lodz.p.it.eduvirt.exceptions.team;

import pl.lodz.p.it.eduvirt.util.I18n;

public class IncorrectTeamTypeException extends TeamValidationException {

    public IncorrectTeamTypeException() {
        super(I18n.INCORRECT_TEAM_TYPE);
    }

    public IncorrectTeamTypeException(String message) {
        super(message);
    }

    public IncorrectTeamTypeException(Throwable cause) {
        super(I18n.INCORRECT_TEAM_TYPE, cause);
    }

    public IncorrectTeamTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
