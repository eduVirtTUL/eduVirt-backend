package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.ConflictException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class VlansRangeConflictingRangeException extends ConflictException {

    public VlansRangeConflictingRangeException(String message) {
        super(message, I18n.VLANS_RANGE_CONFLICTING_RANGE);
    }
}
