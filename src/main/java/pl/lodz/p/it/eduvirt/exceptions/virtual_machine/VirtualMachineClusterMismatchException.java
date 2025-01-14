package pl.lodz.p.it.eduvirt.exceptions.virtual_machine;

import pl.lodz.p.it.eduvirt.exceptions.general.ApplicationBaseException;

import java.util.UUID;

public class VirtualMachineClusterMismatchException extends ApplicationBaseException {
    public VirtualMachineClusterMismatchException(UUID id) {
        super("Virtual machine with id: " + id + " is on different cluster ", "virtualMachineClusterMismatch");
    }
}
