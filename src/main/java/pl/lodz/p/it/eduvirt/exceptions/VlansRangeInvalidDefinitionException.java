package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.BadRequestException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class VlansRangeInvalidDefinitionException extends BadRequestException {

    public VlansRangeInvalidDefinitionException(String message) {
        super(message, I18n.VLANS_RANGE_INVALID_DEFINITION);
    }
}
