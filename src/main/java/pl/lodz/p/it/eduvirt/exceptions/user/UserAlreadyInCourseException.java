package pl.lodz.p.it.eduvirt.exceptions.user;

import pl.lodz.p.it.eduvirt.exceptions.team.TeamBaseException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class UserAlreadyInCourseException extends TeamBaseException {

    public UserAlreadyInCourseException() {
        super(I18n.USER_ALREADY_IN_COURSE);
    }

    public UserAlreadyInCourseException(String message) {
        super(message);
    }

    public UserAlreadyInCourseException(Throwable cause) {
        super(I18n.USER_ALREADY_IN_COURSE, cause);
    }

    public UserAlreadyInCourseException(String message, Throwable cause) {
        super(message, cause);
    }
}
