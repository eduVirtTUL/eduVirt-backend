package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.NotFoundException;
import pl.lodz.p.it.eduvirt.util.I18n;

import java.util.UUID;

public class CourseNotFoundException extends NotFoundException {

    public CourseNotFoundException(String message) {
        super(message, I18n.COURSE_NOT_FOUND);
    }

    public CourseNotFoundException(UUID id) {
        super("Course with id " + id + " not found.", I18n.COURSE_NOT_FOUND);
    }
}
