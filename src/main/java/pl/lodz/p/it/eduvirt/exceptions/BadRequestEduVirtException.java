package pl.lodz.p.it.eduvirt.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class BadRequestEduVirtException extends ApplicationBaseException {

    public BadRequestEduVirtException() {
        super();
    }

    public BadRequestEduVirtException(String message) {
        super(message);
    }

    public BadRequestEduVirtException(String message, Throwable cause) {
        super(message, cause);
    }
}
