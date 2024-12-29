package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.NotFoundException;
import pl.lodz.p.it.eduvirt.util.I18n;

import java.util.UUID;

public class ResourceGroupNotFoundException extends NotFoundException {

    public ResourceGroupNotFoundException(UUID uuid) {
        super("Resource group with id " + uuid + " not found", I18n.RESOURCE_GROUP_NOT_FOUND);
    }
}
