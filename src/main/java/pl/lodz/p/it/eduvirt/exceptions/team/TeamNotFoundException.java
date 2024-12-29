package pl.lodz.p.it.eduvirt.exceptions.team;

import org.springframework.web.bind.annotation.ResponseStatus;
import pl.lodz.p.it.eduvirt.util.I18n;

@ResponseStatus()
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
