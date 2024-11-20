package pl.lodz.p.it.eduvirt.exceptions.range;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import pl.lodz.p.it.eduvirt.exceptions.ApplicationBaseException;

@ResponseStatus(code = HttpStatus.CONFLICT)
public class VlansRangeConflictException extends ApplicationBaseException {

    public VlansRangeConflictException() {
    }

    public VlansRangeConflictException(String message) {
        super(message);
    }

    public VlansRangeConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
