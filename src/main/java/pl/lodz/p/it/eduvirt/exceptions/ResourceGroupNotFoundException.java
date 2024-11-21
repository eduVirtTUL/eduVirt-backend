package pl.lodz.p.it.eduvirt.exceptions;

import java.util.UUID;

public class ResourceGroupNotFoundException extends NotFoundException {
    public ResourceGroupNotFoundException(UUID uuid) {
        super("Resource group with id " + uuid + " not found");
    }
}
