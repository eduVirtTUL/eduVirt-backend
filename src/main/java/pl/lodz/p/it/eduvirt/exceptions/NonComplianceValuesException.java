package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.ApplicationBaseException;

public class NonComplianceValuesException extends ApplicationBaseException {

    public NonComplianceValuesException(String key) {
        super(key);
    }
}
