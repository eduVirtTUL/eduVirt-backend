package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.NotFoundException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class StatefulPodAssignmentException extends NotFoundException {

    public StatefulPodAssignmentException(String message) {
        super(message, I18n.STATEFUL_POD_NOT_ASSIGNED);
    }
}
