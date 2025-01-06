package pl.lodz.p.it.eduvirt.exceptions.team;

public class TeamBaseException extends RuntimeException {

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
