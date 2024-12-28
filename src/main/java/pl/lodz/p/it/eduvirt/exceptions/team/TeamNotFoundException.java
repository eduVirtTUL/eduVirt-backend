package pl.lodz.p.it.eduvirt.exceptions.team;

import pl.lodz.p.it.eduvirt.util.I18n;

public class TeamNotFoundException extends TeamBaseException {

    public TeamNotFoundException() {
        super(I18n.TEAM_NOT_FOUND);
    }

    public TeamNotFoundException(String message) {
        super(message);
    }

    public TeamNotFoundException(Throwable cause) {
        super(I18n.TEAM_NOT_FOUND, cause);
    }

    public TeamNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
