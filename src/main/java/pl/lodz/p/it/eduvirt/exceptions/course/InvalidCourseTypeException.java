package pl.lodz.p.it.eduvirt.exceptions.course;

import pl.lodz.p.it.eduvirt.exceptions.general.BadRequestException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class InvalidCourseTypeException extends BadRequestException {
    public InvalidCourseTypeException(String message) {
        super(message, I18n.COURSE_INVALID_TYPE);
    }
}
