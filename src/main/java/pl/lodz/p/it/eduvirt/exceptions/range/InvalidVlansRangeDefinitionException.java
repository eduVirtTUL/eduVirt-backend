package pl.lodz.p.it.eduvirt.exceptions.range;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import pl.lodz.p.it.eduvirt.exceptions.ApplicationBaseException;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class InvalidVlansRangeDefinitionException extends ApplicationBaseException {

    public InvalidVlansRangeDefinitionException() {
    }

    public InvalidVlansRangeDefinitionException(String message) {
        super(message);
    }

    public InvalidVlansRangeDefinitionException(String message, Throwable cause) {
        super(message, cause);
    }
}
