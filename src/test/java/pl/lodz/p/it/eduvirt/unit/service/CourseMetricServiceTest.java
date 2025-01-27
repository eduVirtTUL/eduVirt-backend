package pl.lodz.p.it.eduvirt.unit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.lodz.p.it.eduvirt.entity.Course;
import pl.lodz.p.it.eduvirt.entity.CourseMetric;
import pl.lodz.p.it.eduvirt.entity.Metric;
import pl.lodz.p.it.eduvirt.exceptions.MetricNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.course.CourseMetricExistsException;
import pl.lodz.p.it.eduvirt.exceptions.course.CourseMetricNetworksNotSufficientException;
import pl.lodz.p.it.eduvirt.exceptions.course.CourseMetricNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.course.CourseNotFoundException;
import pl.lodz.p.it.eduvirt.repository.CourseMetricRepository;
import pl.lodz.p.it.eduvirt.repository.CourseRepository;
import pl.lodz.p.it.eduvirt.repository.MetricRepository;
import pl.lodz.p.it.eduvirt.service.impl.CourseMetricServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CourseMetricServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseMetricRepository courseMetricRepository;

    @Mock
    private MetricRepository metricRepository;

    @Captor
    private ArgumentCaptor<CourseMetric> courseMetricArgumentCaptor;

    @InjectMocks
    private CourseMetricServiceImpl sut;

    @Test
    void Given_CourseMetricsExists_When_GetCourseMetrics_Then_ReturnCourseMetrics() {
        // Given
        UUID courseId = UUID.randomUUID();
        when(courseMetricRepository.findAllByCourseId(any()))
                .thenReturn(List.of(
                        CourseMetric.builder().build()
                ));
        // When
        var result = sut.getCourseMetrics(courseId);
        // Then
        assertEquals(1, result.size());
    }

    @Test
    void Given_CourseMetricsDoNotExists_When_GetCourseMetrics_Then_ReturnEmptyList() {
        // Given
        UUID courseId = UUID.randomUUID();
        when(courseMetricRepository.findAllByCourseId(any()))
                .thenReturn(List.of());
        // When
        var result = sut.getCourseMetrics(courseId);
        // Then
        assertEquals(0, result.size());
    }

    @Test
    void Given_CourseMetricExists_When_GetCourseMetric_Then_ReturnCourseMetric() {
        // Given
        UUID courseId = UUID.randomUUID();
        UUID metricId = UUID.randomUUID();
        when(courseMetricRepository.findById(any()))
                .thenReturn(Optional.of(CourseMetric.builder().build()));
        when(courseRepository.findById(any()))
                .thenReturn(Optional.of(Course.builder().build()));
        when(metricRepository.findById(any()))
                .thenReturn(Optional.of(Metric.builder().build()));
        // When
        var result = sut.getCourseMetric(courseId, metricId);
        // Then
        assertNotNull(result);
    }

    @Test
    void Given_CourseDoNotExists_When_GetCourseMetric_Then_ThrowCourseNotFoundException() {
        // Given
        UUID courseId = UUID.randomUUID();
        UUID metricId = UUID.randomUUID();
        when(courseRepository.findById(any()))
                .thenReturn(Optional.empty());
        // When
        // Then
        assertThrows(CourseNotFoundException.class, () -> sut.getCourseMetric(courseId, metricId));
    }

    @Test
    void Given_MetricDoNotExists_When_GetCourseMetric_Then_ThrowCourseNotFoundException() {
        // Given
        UUID courseId = UUID.randomUUID();
        UUID metricId = UUID.randomUUID();
        when(courseRepository.findById(any()))
                .thenReturn(Optional.of(Course.builder().build()));
        when(metricRepository.findById(any()))
                .thenReturn(Optional.empty());
        // When
        // Then
        assertThrows(MetricNotFoundException.class, () -> sut.getCourseMetric(courseId, metricId));
    }

    @Test
    void Given_CourseMetricDoNotExists_When_GetCourseMetric_Then_ThrowCourseNotFoundException() {
        // Given
        UUID courseId = UUID.randomUUID();
        UUID metricId = UUID.randomUUID();
        when(courseMetricRepository.findById(any()))
                .thenReturn(Optional.empty());
        when(courseRepository.findById(any()))
                .thenReturn(Optional.of(Course.builder().build()));
        when(metricRepository.findById(any()))
                .thenReturn(Optional.of(Metric.builder().build()));
        // When
        // Then
        assertThrows(CourseMetricNotFoundException.class, () -> sut.getCourseMetric(courseId, metricId));
    }

    @Test
    void Given_CourseDoNotExists_When_RemoveMetricFromCourse_Then_ThrowCourseNotFoundException() {
        // Given
        UUID courseId = UUID.randomUUID();
        UUID metricId = UUID.randomUUID();
        when(courseRepository.findById(any()))
                .thenReturn(Optional.empty());
        // When
        // Then
        assertThrows(CourseNotFoundException.class, () -> sut.removeMetricFromCourse(courseId, metricId));
    }

    @Test
    void Given_MetricDoNotExists_When_RemoveMetricFromCourse_Then_ThrowCourseNotFoundException() {
        // Given
        UUID courseId = UUID.randomUUID();
        UUID metricId = UUID.randomUUID();
        when(courseRepository.findById(any()))
                .thenReturn(Optional.of(Course.builder().build()));
        when(metricRepository.findById(any()))
                .thenReturn(Optional.empty());
        // When
        // Then
        assertThrows(MetricNotFoundException.class, () -> sut.removeMetricFromCourse(courseId, metricId));
    }

    @Test
    void Given_InputDataIsCorrect_When_RemoveMetricFromCourse_Then_RemoveCourseMetric() {
        // Given
        UUID courseId = UUID.randomUUID();
        UUID metricId = UUID.randomUUID();
        when(courseRepository.findById(any()))
                .thenReturn(Optional.of(Course.builder().build()));
        when(metricRepository.findById(any()))
                .thenReturn(Optional.of(Metric.builder().build()));
        // When
        sut.removeMetricFromCourse(courseId, metricId);
        // Then
        verify(courseMetricRepository, times(1)).deleteById(any());
    }

    @Test
    void Given_CourseDoNotExists_When_AddMetricToCourse_Then_ThrowCourseNotFoundException() {
        // Given
        UUID courseId = UUID.randomUUID();
        UUID metricId = UUID.randomUUID();
        when(metricRepository.findById(any()))
                .thenReturn(Optional.of(Metric.builder().build()));
        when(courseRepository.findById(any()))
                .thenReturn(Optional.empty());
        // When
        // Then
        assertThrows(CourseNotFoundException.class, () -> sut.addMetricToCourse(courseId, metricId, 1.0));
    }

    @Test
    void Given_MetricDoNotExists_When_AddMetricToCourse_Then_ThrowMetricNotFoundException() {
        // Given
        UUID courseId = UUID.randomUUID();
        UUID metricId = UUID.randomUUID();
        when(metricRepository.findById(any()))
                .thenReturn(Optional.empty());
        // When
        // Then
        assertThrows(MetricNotFoundException.class, () -> sut.addMetricToCourse(courseId, metricId, 1.0));
    }

    @Test
    void Given_CourseMetricExists_When_AddMetricToCourse_Then_ThrowCourseMetricExistsException() {
        // Given
        UUID courseId = UUID.randomUUID();
        UUID metricId = UUID.randomUUID();
        when(courseRepository.findById(any()))
                .thenReturn(Optional.of(Course.builder().build()));
        when(metricRepository.findById(any()))
                .thenReturn(Optional.of(Metric.builder().build()));
        when(courseMetricRepository.existsById(any()))
                .thenReturn(true);
        // When
        // Then
        assertThrows(CourseMetricExistsException.class, () -> sut.addMetricToCourse(courseId, metricId, 1.0));
    }

    @Test
    void Given_InputDataIsCorrect_When_AddMetricToCourse_Then_AddCourseMetric() {
        // Given
        UUID courseId = UUID.randomUUID();
        UUID metricId = UUID.randomUUID();
        Metric metric = Metric.builder()
                .name("cpu_count")
                .build();
        Course course = Course.builder().build();
        when(courseRepository.findById(any()))
                .thenReturn(Optional.of(course));
        when(metricRepository.findById(any()))
                .thenReturn(Optional.of(metric));
        when(courseMetricRepository.existsById(any()))
                .thenReturn(false);
        // When
        sut.addMetricToCourse(courseId, metricId, 1.0);
        // Then
        verify(courseMetricRepository, times(1)).save(courseMetricArgumentCaptor.capture());
        var saved = courseMetricArgumentCaptor.getValue();
        assertEquals(course, saved.getCourse());
        assertEquals(metric, saved.getMetric());
        assertEquals(1.0, saved.getValue());
    }

    @Test
    void Given_MetricIsNetworkCountAndValueIsCorrect_When_AddMetricToCourse_Then_AddCourseMetric() {
        // Given
        UUID courseId = UUID.randomUUID();
        UUID metricId = UUID.randomUUID();
        Metric metric = Metric.builder()
                .name("network_count")
                .build();
        Course course = Course.builder()
                .build();
        when(courseRepository.getStatefulResourceGroupNetworkCount(any()))
                .thenReturn(new ArrayList<>(List.of(2, 3)));
        when(courseRepository.findById(any()))
                .thenReturn(Optional.of(course));
        when(metricRepository.findById(any()))
                .thenReturn(Optional.of(metric));
        when(courseMetricRepository.existsById(any()))
                .thenReturn(false);
        // When
        sut.addMetricToCourse(courseId, metricId, 5.0);
        // Then
    }

    @Test
    void Given_MetricIsNetworkCountAndValueIsIncorrect_When_AddMetricToCourse_Then_ThrowIllegalArgumentException() {
        // Given
        UUID courseId = UUID.randomUUID();
        UUID metricId = UUID.randomUUID();
        Metric metric = Metric.builder()
                .name("network_count")
                .build();
        Course course = Course.builder()
                .build();
        when(courseRepository.getStatefulResourceGroupNetworkCount(any()))
                .thenReturn(new ArrayList<>(List.of(2, 3)));
        when(courseRepository.findById(any()))
                .thenReturn(Optional.of(course));
        when(metricRepository.findById(any()))
                .thenReturn(Optional.of(metric));
        when(courseMetricRepository.existsById(any()))
                .thenReturn(false);
        // When
        // Then
        assertThrows(CourseMetricNetworksNotSufficientException.class, () -> sut.addMetricToCourse(courseId, metricId, 1.0));
    }

    @Test
    void Given_CourseMetricExists_When_UpdateCourseMetric_Then_UpdateCourseMetric() {
        // Given
        double newValue = 2.0;
        Metric metric = Metric.builder()
                .name("cpu_count")
                .build();
        UUID courseId = UUID.randomUUID();
        UUID metricId = UUID.randomUUID();
        when(metricRepository.getReferenceById(any()))
                .thenReturn(metric);
        when(courseMetricRepository.findById(any()))
                .thenReturn(Optional.of(CourseMetric.builder().build()));
        when(courseMetricRepository.save(courseMetricArgumentCaptor.capture()))
                .thenReturn(CourseMetric.builder().build());
        // When
        sut.updateCourseMetric(courseId, metricId, newValue);
        // Then
        verify(courseMetricRepository, times(1)).save(any());
        var saved = courseMetricArgumentCaptor.getValue();
        assertEquals(newValue, saved.getValue());
    }

    @Test
    void Given_CourseMetricDoNotExists_When_UpdateCourseMetric_Then_ThrowCourseMetricNotFoundException() {
        // Given
        double newValue = 2.0;
        UUID courseId = UUID.randomUUID();
        UUID metricId = UUID.randomUUID();
        when(courseMetricRepository.findById(any()))
                .thenReturn(Optional.empty());
        // When
        // Then
        assertThrows(CourseMetricNotFoundException.class, () -> sut.updateCourseMetric(courseId, metricId, newValue));
    }

    @Test
    void Given_MetricIsNetworkCountAndValueIsCorrect_When_UpdateCourseMetric_Then_UpdateCourseMetric() {
        // Given
        UUID courseId = UUID.randomUUID();
        UUID metricId = UUID.randomUUID();
        Metric metric = Metric.builder()
                .name("network_count")
                .build();
        Course course = Course.builder()
                .build();
        when(courseRepository.getStatefulResourceGroupNetworkCount(any()))
                .thenReturn(new ArrayList<>(List.of(2, 3)));
        when(metricRepository.getReferenceById(any()))
                .thenReturn(metric);
        when(courseRepository.getReferenceById(any()))
                .thenReturn(course);
        when(courseMetricRepository.findById(any()))
                .thenReturn(Optional.of(CourseMetric.builder().build()));
        // When
        sut.updateCourseMetric(courseId, metricId, 5.0);
        // Then
        verify(courseMetricRepository, times(1)).save(any());
    }

}











