package pl.lodz.p.it.eduvirt.exceptions.permission;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import pl.lodz.p.it.eduvirt.exceptions.ApplicationBaseException;

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
