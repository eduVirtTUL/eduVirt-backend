package pl.lodz.p.it.eduvirt.exceptions.user;

import pl.lodz.p.it.eduvirt.exceptions.ApplicationBaseException;

public class UsersNotFoundException extends ApplicationBaseException {

    public UsersNotFoundException() {
    }

    public UsersNotFoundException(String message) {
        super(message);
    }

    public UsersNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
