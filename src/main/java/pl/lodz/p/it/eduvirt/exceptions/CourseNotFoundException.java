package pl.lodz.p.it.eduvirt.exceptions;

import java.util.UUID;

public class CourseNotFoundException extends NotFoundException {
    public CourseNotFoundException(String message) {
        super(message);
    }

    public CourseNotFoundException(UUID id) {
        super("Course with id " + id + " not found.");
    }
}
