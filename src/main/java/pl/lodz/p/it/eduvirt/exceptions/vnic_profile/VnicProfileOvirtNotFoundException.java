package pl.lodz.p.it.eduvirt.exceptions.vnic_profile;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import pl.lodz.p.it.eduvirt.exceptions.ApplicationBaseException;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class VnicProfileOvirtNotFoundException extends ApplicationBaseException {

    public VnicProfileOvirtNotFoundException() {
        super();
    }

    public VnicProfileOvirtNotFoundException(String message) {
        super(message);
    }

    public VnicProfileOvirtNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
