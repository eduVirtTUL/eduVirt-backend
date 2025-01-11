package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.NotFoundException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class StatelessPodAssignmentException extends NotFoundException {

    public StatelessPodAssignmentException(String message) {
        super(message, I18n.STATELESS_POD_NOT_ASSIGNED);
    }
}
