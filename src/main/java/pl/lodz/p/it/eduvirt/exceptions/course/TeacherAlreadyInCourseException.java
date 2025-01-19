package pl.lodz.p.it.eduvirt.exceptions.course;

import pl.lodz.p.it.eduvirt.exceptions.general.ForbiddenException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class TeacherAlreadyInCourseException extends ForbiddenException {

    public TeacherAlreadyInCourseException() {
        super("Teacher is already in this course", I18n.TEACHER_ALREADY_IN_COURSE);
    }
}
