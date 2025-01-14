package pl.lodz.p.it.eduvirt.exceptions.resource_group_pool;

import pl.lodz.p.it.eduvirt.exceptions.general.ConflictException;

public class ResourceGroupPoolConflictException extends ConflictException {
    public ResourceGroupPoolConflictException() {
        super("Resource group pool conflict, optimistic lock error", "rgPoolConflict");
    }
}
