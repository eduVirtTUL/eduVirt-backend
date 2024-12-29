package pl.lodz.p.it.eduvirt.exceptions.general;

import pl.lodz.p.it.eduvirt.util.I18n;

public class OpeningConnectionException extends ApplicationBaseException {

    public OpeningConnectionException(String message) {
        super(message, I18n.CONNECTION_OPEN_ERROR);
    }
}
