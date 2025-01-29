package pl.lodz.p.it.eduvirt.exceptions.resource_group;

import pl.lodz.p.it.eduvirt.exceptions.general.NotFoundException;

import java.util.UUID;

public class ResourceGroupNetworkNotFoundException extends NotFoundException {
    public ResourceGroupNetworkNotFoundException(UUID id) {
        super("Resource group network with id " + id + " not found", "resourceGroupNetworkNotFound");
    }
}
