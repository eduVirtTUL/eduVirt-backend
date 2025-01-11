package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.ConflictException;

public class VlansRangeConflictException extends ConflictException {

    public VlansRangeConflictException(String message) {
        super(message, "");
    }
}
