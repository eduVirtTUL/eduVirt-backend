package pl.lodz.p.it.eduvirt.exceptions.team;

import pl.lodz.p.it.eduvirt.util.I18n;

public class UserAlreadyInTeamException extends TeamBaseException {

    public UserAlreadyInTeamException() {
        super(I18n.USER_ALREADY_IN_TEAM);
    }

    public UserAlreadyInTeamException(String message) {
        super(message);
    }

    public UserAlreadyInTeamException(Throwable cause) {
        super(I18n.USER_ALREADY_IN_TEAM, cause);
    }

    public UserAlreadyInTeamException(String message, Throwable cause) {
        super(message, cause);
    }

}
