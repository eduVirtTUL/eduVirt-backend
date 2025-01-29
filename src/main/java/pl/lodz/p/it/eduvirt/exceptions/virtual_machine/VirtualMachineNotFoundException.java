package pl.lodz.p.it.eduvirt.exceptions.virtual_machine;

import pl.lodz.p.it.eduvirt.exceptions.general.NotFoundException;

import java.util.UUID;

public class VirtualMachineNotFoundException extends NotFoundException {
    public VirtualMachineNotFoundException(UUID vmId) {
        super("Virtual machine with id " + vmId + " not found", "vmNotFound");
    }
}
