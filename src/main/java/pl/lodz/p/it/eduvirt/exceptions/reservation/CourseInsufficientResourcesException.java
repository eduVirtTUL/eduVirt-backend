package pl.lodz.p.it.eduvirt.exceptions.reservation;

import pl.lodz.p.it.eduvirt.util.I18n;

public class CourseInsufficientResourcesException extends ReservationCreationException {

    public CourseInsufficientResourcesException() {
        super(I18n.COURSE_RESOURCES_INSUFFICIENT);
    }

    public CourseInsufficientResourcesException(String message) {
        super(message);
    }

    public CourseInsufficientResourcesException(Throwable cause) {
        super(I18n.COURSE_RESOURCES_INSUFFICIENT, cause);
    }

    public CourseInsufficientResourcesException(String message, Throwable cause) {
        super(message, cause);
    }
}
