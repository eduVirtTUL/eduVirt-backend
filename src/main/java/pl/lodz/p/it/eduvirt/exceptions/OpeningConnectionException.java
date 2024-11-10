package pl.lodz.p.it.eduvirt.exceptions;

public class OpeningConnectionException extends ApplicationBaseException {
    public OpeningConnectionException(String message) {
        super(message);
    }

    public OpeningConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
