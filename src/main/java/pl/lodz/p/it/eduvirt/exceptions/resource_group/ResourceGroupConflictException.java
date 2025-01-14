package pl.lodz.p.it.eduvirt.exceptions.resource_group;

import pl.lodz.p.it.eduvirt.exceptions.general.ConflictException;

public class ResourceGroupConflictException extends ConflictException {
    public ResourceGroupConflictException() {
        super("Resource group optimistic lock conflict", "resourceGroupConflict");
    }
}
