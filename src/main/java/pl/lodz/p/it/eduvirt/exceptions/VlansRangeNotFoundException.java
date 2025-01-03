package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.NotFoundException;
import pl.lodz.p.it.eduvirt.util.I18n;

import java.util.UUID;

public class VlansRangeNotFoundException extends NotFoundException {

    public VlansRangeNotFoundException(UUID vlansRangeId) {
        super("Vlans range with id %s could not be found".formatted(vlansRangeId), I18n.VLANS_RANGE_NOT_FOUND);
    }
}
