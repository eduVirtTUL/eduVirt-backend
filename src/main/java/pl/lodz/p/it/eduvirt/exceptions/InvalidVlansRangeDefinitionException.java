package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.BadRequestException;

public class InvalidVlansRangeDefinitionException extends BadRequestException {

    public InvalidVlansRangeDefinitionException(String message) {
        super(message, "");
    }
}
