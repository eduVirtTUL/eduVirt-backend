package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.NotFoundException;
import pl.lodz.p.it.eduvirt.util.I18n;

import java.util.UUID;

public class CourseMetricNotFoundException extends NotFoundException {

    public CourseMetricNotFoundException(UUID courseId, UUID metricId) {
        super("Course with id " + courseId + " does not have metric with id " + metricId, I18n.COURSE_METRIC_VALUE_NOT_DEFINED);
    }
}
