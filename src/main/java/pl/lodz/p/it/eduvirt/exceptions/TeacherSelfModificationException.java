package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.ForbiddenException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class TeacherSelfModificationException extends ForbiddenException {

    public TeacherSelfModificationException(String message, String key) {
        super("Teacher cannot remove themselves from a course", I18n.TEACHER_SELF_MODIFICATION_EXCEPTION);
    }

    public TeacherSelfModificationException(String message) {
        super(message, I18n.TEACHER_SELF_MODIFICATION_EXCEPTION);
    }
}
