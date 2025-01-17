package pl.lodz.p.it.eduvirt.exceptions.resource_group;

import pl.lodz.p.it.eduvirt.exceptions.general.AlreadyExistsException;

public class NetworkAlreadyExistsException extends AlreadyExistsException {
    public NetworkAlreadyExistsException() {
        super("Network already exists", "networkAlreadyExists");
    }
}
