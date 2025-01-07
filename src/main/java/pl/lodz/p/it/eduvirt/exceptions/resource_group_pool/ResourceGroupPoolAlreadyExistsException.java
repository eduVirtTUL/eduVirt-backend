package pl.lodz.p.it.eduvirt.exceptions.resource_group_pool;

import pl.lodz.p.it.eduvirt.exceptions.general.AlreadyExistsException;

public class ResourceGroupPoolAlreadyExistsException extends AlreadyExistsException {
    public ResourceGroupPoolAlreadyExistsException(String name) {
        super("Resource group pool with name " + name + " already exists in this course.", "resourceGroupPoolAlreadyExists");
    }
}
