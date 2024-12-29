package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.util.I18n;

import java.util.UUID;

public class CourseInsufficientResourcesException extends ReservationCreationException {

    public CourseInsufficientResourcesException(UUID courseId) {
        super("Resources assigned to course %s are insufficient to create new reservation".formatted(courseId),
                I18n.COURSE_RESOURCES_INSUFFICIENT);
    }
}
