package pl.lodz.p.it.eduvirt.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import pl.lodz.p.it.eduvirt.entity.Metric;
import pl.lodz.p.it.eduvirt.exceptions.MetricNotFoundException;
import pl.lodz.p.it.eduvirt.repository.ClusterMetricRepository;
import pl.lodz.p.it.eduvirt.repository.CourseMetricRepository;
import pl.lodz.p.it.eduvirt.repository.MetricRepository;
import pl.lodz.p.it.eduvirt.service.impl.MetricServiceImpl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MetricServiceTest {

    @Mock
    private MetricRepository metricRepository;

    @Mock
    private CourseMetricRepository courseMetricRepository;

    @Mock
    private ClusterMetricRepository clusterMetricRepository;

    @InjectMocks
    private MetricServiceImpl metricService;

    private final String metricName1 = "metric_name_no1";
    private final String metricName2 = "metric_name_no2";

    private Metric metric1;
    private Metric metric2;

    /* Initialization */

    @BeforeEach
    public void prepareTestData() {
        metric1 = new Metric(metricName1, Metric.MetricCategory.MEMORY);
        metric2 = new Metric(metricName2, Metric.MetricCategory.MEMORY);
    }

    /* Tests */

    /* CreateNewMetric method test */

    @Test
    public void Given_AllTheDataIsValid_When_CreateNewMetric_Then_CreateNewMetricSuccessfully() {
        String metricName = "new_metric_name";
        Metric newMetric = new Metric(metricName, Metric.MetricCategory.COUNTABLE);

        when(metricRepository.saveAndFlush(any())).thenReturn(newMetric);
        metricService.createNewMetric(metricName, Metric.MetricCategory.COUNTABLE);
        verify(metricRepository, times(1)).saveAndFlush(any());
    }

    /* FindById method test */

    @Test
    public void Given_ExistingMetricIdentifierIsPassed_When_FindById_Then_ReturnsFoundMetricSuccessfully() {
        when(metricRepository.findById(metric1.getId())).thenReturn(Optional.of(metric1));
        metricService.findById(metric1.getId());
        verify(metricRepository, times(1)).findById(metric1.getId());
    }

    @Test
    public void Given_NonExistentMetricIdentifierIsPassed_When_FindById_Then_ThrowsException() {
        UUID nonExistentMetricId = UUID.randomUUID();
        when(metricRepository.findById(nonExistentMetricId)).thenReturn(Optional.empty());
        assertThrows(MetricNotFoundException.class, () -> metricService.findById(nonExistentMetricId));
        verify(metricRepository, times(1)).findById(nonExistentMetricId);
    }

    /* FindAllMetrics method test */

    @Test
    public void Given_CorrectPageNumberAndPageSizeIsPassed_When_FindAllMetrics_Then_ReturnsAllFoundMetricsSuccessfully() {
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("name"));

        when(metricRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(metric1, metric2), pageable, 2));

        Page<Metric> foundPage = metricService.findAllMetrics(pageable);

        assertNotNull(foundPage);
        assertNotNull(foundPage.getContent());

        List<Metric> foundMetrics = foundPage.getContent();

        assertNotNull(foundMetrics);
        assertFalse(foundMetrics.isEmpty());
        assertEquals(2, foundMetrics.size());

        Metric firstMetric = foundMetrics.getFirst();
        assertNotNull(firstMetric);
        assertNotNull(firstMetric.getName());
        assertEquals(metricName1, firstMetric.getName());

        Metric secondMetric = foundMetrics.getLast();
        assertNotNull(secondMetric);
        assertNotNull(secondMetric.getName());
        assertEquals(metricName2, secondMetric.getName());

        verify(metricRepository, times(1)).findAll(pageable);
    }

    /* DeleteMetric method test */

    @Test
    public void Given_ExistingMetricIdentifierIsPassed_When_DeleteMetric_Then_RemovesMetricSuccessfully() {
        UUID metricId = UUID.randomUUID();

        when(metricRepository.findById(metricId)).thenReturn(Optional.of(metric1));
        doNothing().when(metricRepository).delete(metric1);
        doNothing().when(courseMetricRepository).deleteByMetric(metric1);
        doNothing().when(clusterMetricRepository).deleteByMetric(metric1);

        metricService.deleteMetric(metricId);

        verify(metricRepository, times(1)).findById(metricId);
        verify(metricRepository, times(1)).delete(metric1);
        verify(courseMetricRepository, times(1)).deleteByMetric(metric1);
        verify(clusterMetricRepository, times(1)).deleteByMetric(metric1);
    }

    @Test
    public void Given_NonExistentMetricIdentifierIsPassed_When_DeleteMetric_Then_ThrowsException() {
        UUID metricId = UUID.randomUUID();
        when(metricRepository.findById(metricId)).thenReturn(Optional.empty());
        assertThrows(MetricNotFoundException.class, () -> metricService.deleteMetric(metricId));
        verify(metricRepository, times(1)).findById(metricId);
    }
}
