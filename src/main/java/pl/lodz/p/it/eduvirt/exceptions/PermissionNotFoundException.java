package pl.lodz.p.it.eduvirt.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class PermissionNotFoundException extends ApplicationBaseException {

    public PermissionNotFoundException() {
    }

    public PermissionNotFoundException(String message) {
        super(message);
    }

    public PermissionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
