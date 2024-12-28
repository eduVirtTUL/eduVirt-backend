package pl.lodz.p.it.eduvirt.exceptions.team;

import pl.lodz.p.it.eduvirt.exceptions.ApplicationBaseException;

public class TeamBaseException extends ApplicationBaseException {

    public TeamBaseException() {
    }

    public TeamBaseException(String message) {
        super(message);
    }

    public TeamBaseException(Throwable cause) {
        super(cause);
    }

    public TeamBaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
