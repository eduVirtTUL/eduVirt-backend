package pl.lodz.p.it.eduvirt.exceptions.resource_group;

import pl.lodz.p.it.eduvirt.exceptions.general.ApplicationBaseException;

public class NoNetworkAvailableException extends ApplicationBaseException {
    public NoNetworkAvailableException(String message) {
        super(message, "noNetworkAvailable");
    }
}
