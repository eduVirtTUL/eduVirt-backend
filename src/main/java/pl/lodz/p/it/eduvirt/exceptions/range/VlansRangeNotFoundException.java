package pl.lodz.p.it.eduvirt.exceptions.range;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import pl.lodz.p.it.eduvirt.exceptions.ApplicationBaseException;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class VlansRangeNotFoundException extends ApplicationBaseException {

    public VlansRangeNotFoundException() {
    }

    public VlansRangeNotFoundException(String message) {
        super(message);
    }

    public VlansRangeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
