package pl.lodz.p.it.eduvirt.exceptions.course;

import pl.lodz.p.it.eduvirt.exceptions.general.ForbiddenException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class CourseNoTeachersException extends ForbiddenException {

  public CourseNoTeachersException() {
    super("A course cannot contain less than 1 teacher", I18n.COURSE_NO_TEACHERS);
  }

  public CourseNoTeachersException(String message) {
    super(message, I18n.COURSE_NO_TEACHERS);
  }
}
