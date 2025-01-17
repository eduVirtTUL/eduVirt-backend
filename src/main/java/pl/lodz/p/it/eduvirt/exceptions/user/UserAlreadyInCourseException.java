package pl.lodz.p.it.eduvirt.exceptions.user;

import pl.lodz.p.it.eduvirt.exceptions.general.ForbiddenException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class UserAlreadyInCourseException extends ForbiddenException {

    public UserAlreadyInCourseException() {
        super("User is already in this course", I18n.USER_ALREADY_IN_COURSE);
    }

}
