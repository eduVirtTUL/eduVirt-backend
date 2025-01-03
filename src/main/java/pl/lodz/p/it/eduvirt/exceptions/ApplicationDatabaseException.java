package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.IntervalServerError;

public class ApplicationDatabaseException extends IntervalServerError {

    public ApplicationDatabaseException(String message) {
        super(message);
    }
}
