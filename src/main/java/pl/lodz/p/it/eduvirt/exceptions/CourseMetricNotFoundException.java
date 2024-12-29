package pl.lodz.p.it.eduvirt.exceptions;

import java.util.UUID;

public class CourseMetricNotFoundException extends NotFoundException {
    public CourseMetricNotFoundException(UUID courseId, UUID metricId) {
        super("Course with id " + courseId + " does not have metric with id " + metricId);
    }
}
