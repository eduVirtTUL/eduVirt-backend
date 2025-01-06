package pl.lodz.p.it.eduvirt.exceptions.user;

public class UserBaseException extends RuntimeException {

    public UserBaseException() {
    }

    public UserBaseException(String message) {
        super(message);
    }

    public UserBaseException(Throwable cause) {
        super(cause);
    }

    public UserBaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
