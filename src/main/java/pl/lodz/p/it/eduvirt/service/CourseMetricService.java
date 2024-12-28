package pl.lodz.p.it.eduvirt.service;

import pl.lodz.p.it.eduvirt.entity.CourseMetric;

import java.util.List;
import java.util.UUID;

public interface CourseMetricService {
    void addMetricToCourse(UUID courseId, UUID metricId, double value);

    void removeMetricFromCourse(UUID courseId, UUID metricId);

    CourseMetric getCourseMetric(UUID courseId, UUID metricId);

    List<CourseMetric> getCourseMetrics(UUID courseId);
}
