package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.NotFoundException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class NetworkNotFoundException extends NotFoundException {

    public NetworkNotFoundException(String message) {
        super(message, I18n.NETWORK_NOT_FOUND);
    }
}
