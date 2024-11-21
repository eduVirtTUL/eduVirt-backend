package pl.lodz.p.it.eduvirt.exceptions.vnic_profile;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import pl.lodz.p.it.eduvirt.exceptions.ApplicationBaseException;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class VnicProfileEduvirtNotFoundException extends ApplicationBaseException {

    public VnicProfileEduvirtNotFoundException() {
        super();
    }

    public VnicProfileEduvirtNotFoundException(String message) {
        super(message);
    }

    public VnicProfileEduvirtNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
