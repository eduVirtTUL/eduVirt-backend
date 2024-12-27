package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.entity.Course;
import pl.lodz.p.it.eduvirt.entity.CourseMetric;
import pl.lodz.p.it.eduvirt.entity.CourseMetricKey;
import pl.lodz.p.it.eduvirt.entity.general.Metric;
import pl.lodz.p.it.eduvirt.exceptions.CourseNotFoundException;
import pl.lodz.p.it.eduvirt.repository.CourseMetricRepository;
import pl.lodz.p.it.eduvirt.repository.CourseRepository;
import pl.lodz.p.it.eduvirt.repository.MetricRepository;
import pl.lodz.p.it.eduvirt.service.CourseMetricService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseMetricServiceImpl implements CourseMetricService {
    private final MetricRepository metricRepository;
    private final CourseRepository courseRepository;
    private final CourseMetricRepository courseMetricRepository;

    @Override
    @Transactional
    public void addMetricToCourse(UUID courseId, UUID metricId, double value) {
        Metric metric = metricRepository.findById(metricId)
                .orElseThrow(() -> new IllegalArgumentException("Metric not found"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        CourseMetric courseMetric = CourseMetric.builder()
                .course(course)
                .metric(metric)
                .value(value)
                .build();

        courseMetricRepository.save(courseMetric);
    }

    @Override
    @Transactional
    public void removeMetricFromCourse(UUID courseId, UUID metricId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));
        Metric metric = metricRepository.findById(metricId).orElseThrow();

        courseMetricRepository.deleteById(new CourseMetricKey(course, metric));
    }

    @Override
    @Transactional
    public CourseMetric getCourseMetric(UUID courseId, UUID metricId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));
        Metric metric = metricRepository.findById(metricId).orElseThrow();

        return courseMetricRepository.findById(new CourseMetricKey(course, metric)).orElseThrow();
    }

    @Override
    @Transactional
    public List<CourseMetric> getCourseMetrics(UUID courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        return course.getMetrics();
    }
}
