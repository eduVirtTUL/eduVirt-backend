package pl.lodz.p.it.eduvirt.exceptions;

import java.util.UUID;

public class CourseMetricExistsException extends AlreadyExistsException {
    public CourseMetricExistsException(UUID courseId, UUID metricId) {
        super("Course with id " + courseId + " already has metric with id " + metricId);
    }
}
