package pl.lodz.p.it.eduvirt.exceptions.course;

import pl.lodz.p.it.eduvirt.exceptions.general.ForbiddenException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class TeacherNotInCourseException extends ForbiddenException {
    public TeacherNotInCourseException() {
        super("Teacher is not in this course", I18n.TEACHER_NOT_IN_COURSE);
    }
}
