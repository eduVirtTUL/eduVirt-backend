package pl.lodz.p.it.eduvirt.exceptions.course;

import pl.lodz.p.it.eduvirt.exceptions.general.ApplicationBaseException;

import java.util.UUID;

public class CourseMetricNetworksNotSufficientException extends ApplicationBaseException {

    public CourseMetricNetworksNotSufficientException(UUID courseId, int networks) {
        super("Cannot create network_count metric with value " + networks + " in course " + courseId, "courseMetricNetworksNotSufficient");
    }
}
