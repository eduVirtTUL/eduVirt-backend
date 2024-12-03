package pl.lodz.p.it.eduvirt.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT)
public class EntityAlreadyException extends ApplicationBaseException{

    public EntityAlreadyException() {
        super();
    }

    public EntityAlreadyException(String message) {
        super(message);
    }

    public EntityAlreadyException(String message, Throwable cause) {
        super(message, cause);
    }
}
