package pl.lodz.p.it.eduvirt.exceptions.virtual_machine;

import pl.lodz.p.it.eduvirt.exceptions.general.NotFoundException;

import java.util.UUID;

public class NetworkInterfaceNotFoundException extends NotFoundException {
    public NetworkInterfaceNotFoundException(UUID networkInterfaceId) {
        super("Network interface with id " + networkInterfaceId + " not found", "networkInterfaceNotFound");
    }
}
