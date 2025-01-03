package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.NotFoundException;
import pl.lodz.p.it.eduvirt.util.I18n;

import java.util.UUID;

public class ResourceGroupPoolNotFoundException extends NotFoundException {

    public ResourceGroupPoolNotFoundException(UUID id) {
        super("Resource group pool with id " + id + " not found.", I18n.RESOURCE_GROUP_POOL_NOT_FOUND);
    }

    public ResourceGroupPoolNotFoundException(String message) {
        super(message, I18n.RESOURCE_GROUP_POOL_NOT_FOUND);
    }
}
