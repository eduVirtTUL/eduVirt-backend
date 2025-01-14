package pl.lodz.p.it.eduvirt.exceptions.virtual_machine;

import pl.lodz.p.it.eduvirt.exceptions.general.ConflictException;

public class VirtualMachineConflictException extends ConflictException {
    public VirtualMachineConflictException() {
        super("Virtual machine conflict, optimistic lock error", "vmConflict");
    }
}
