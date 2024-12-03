package pl.lodz.p.it.eduvirt.exceptions;

import java.util.UUID;

public class ResourceGroupPoolNotFoundException extends NotFoundException {
    public ResourceGroupPoolNotFoundException(UUID id) {
        super("Resource group pool with id " + id + " not found.");
    }
}
