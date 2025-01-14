package pl.lodz.p.it.eduvirt.exceptions.virtual_machine;

import pl.lodz.p.it.eduvirt.exceptions.general.AlreadyExistsException;

import java.util.UUID;

public class VirtualMachineAlreadyExistsException extends AlreadyExistsException {
    public VirtualMachineAlreadyExistsException(UUID id) {
        super("Virtual machine with id " + id + " already exists", "virtualMachineAlreadyExists");
    }
}
