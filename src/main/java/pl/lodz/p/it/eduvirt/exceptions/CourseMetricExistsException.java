package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.AlreadyExistsException;
import pl.lodz.p.it.eduvirt.util.I18n;

import java.util.UUID;

public class CourseMetricExistsException extends AlreadyExistsException {

    public CourseMetricExistsException(UUID courseId, UUID metricId) {
        super("Course with id " + courseId + " already has metric with id " + metricId, I18n.COURSE_METRIC_VALUE_ALREADY_DEFINED);
    }
}
