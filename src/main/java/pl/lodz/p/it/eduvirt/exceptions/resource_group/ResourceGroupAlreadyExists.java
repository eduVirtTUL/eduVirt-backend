package pl.lodz.p.it.eduvirt.exceptions.resource_group;

import pl.lodz.p.it.eduvirt.exceptions.general.AlreadyExistsException;

public class ResourceGroupAlreadyExists extends AlreadyExistsException {
    public ResourceGroupAlreadyExists() {
        super("Resource group already exists", "rgAlreadyExists");
    }
}
