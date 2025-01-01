package pl.lodz.p.it.eduvirt.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.sdk4.types.Cluster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import pl.lodz.p.it.eduvirt.entity.AbstractEntity;
import pl.lodz.p.it.eduvirt.entity.Metric;
import pl.lodz.p.it.eduvirt.entity.ClusterMetric;
import pl.lodz.p.it.eduvirt.exceptions.MetricNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.ClusterMetricExistsException;
import pl.lodz.p.it.eduvirt.exceptions.ClusterMetricNotFoundException;
import pl.lodz.p.it.eduvirt.repository.ClusterMetricRepository;
import pl.lodz.p.it.eduvirt.repository.MetricRepository;
import pl.lodz.p.it.eduvirt.service.impl.ClusterMetricServiceImpl;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClusterMetricServiceTest {

    @Mock
    private MetricRepository metricRepository;

    @Mock
    private ClusterMetricRepository clusterMetricRepository;

    @InjectMocks
    private ClusterMetricServiceImpl clusterMetricService;

    @Mock
    private Cluster cluster;

    /* Initialization */

    private final UUID existingClusterId = UUID.randomUUID();

    private final String metricName1 = "metric_name_no1";
    private final String metricName2 = "metric_name_no2";
    private final String metricName3 = "metric_name_no3";

    private Metric metric1;
    private Metric metric2;
    private Metric metric3;

    private ClusterMetric clusterMetric1;
    private ClusterMetric clusterMetric2;
    private ClusterMetric clusterMetric3;

    @BeforeEach
    public void prepareTestData() throws Exception {
        Field id = AbstractEntity.class.getDeclaredField("id");

        metric1 = new Metric(metricName1);
        metric2 = new Metric(metricName2);
        metric3 = new Metric(metricName3);

        id.setAccessible(true);
        id.set(metric1, UUID.randomUUID());
        id.set(metric2, UUID.randomUUID());
        id.set(metric3, UUID.randomUUID());
        id.setAccessible(false);

        clusterMetric1 = new ClusterMetric(existingClusterId, metric1, 99.9999);
        clusterMetric2 = new ClusterMetric(existingClusterId, metric2, 999.999);
        clusterMetric3 = new ClusterMetric(existingClusterId, metric3, 9999.99);
    }

    /* Tests */

    /* CreateNewValueForMetric method test */

    @Test
    public void Given_ExistingClusterAndMetricIdentifiersArePassed_When_CreateNewValueForMetric_Then_CreatesNewMetricValueSuccessfully() {
        double metricValue = 199.99;
        ClusterMetric newClusterMetric = new ClusterMetric(existingClusterId, metric1, metricValue);

        when(cluster.id()).thenReturn(existingClusterId.toString());
        when(metricRepository.findById(Mockito.eq(metric1.getId()))).thenReturn(Optional.of(metric1));
        when(clusterMetricRepository.findByClusterIdAndMetric(Mockito.eq(existingClusterId), Mockito.eq(metric1))).thenReturn(Optional.empty());
        when(clusterMetricRepository.saveAndFlush(Mockito.any(ClusterMetric.class))).thenReturn(newClusterMetric);

        clusterMetricService.createNewValueForMetric(cluster, metric1.getId(), metricValue);

        verify(cluster, times(1)).id();
        verify(metricRepository, times(1)).findById(Mockito.eq(metric1.getId()));
        verify(clusterMetricRepository, times(1)).findByClusterIdAndMetric(Mockito.eq(existingClusterId), Mockito.eq(metric1));
        verify(clusterMetricRepository, times(1)).saveAndFlush(Mockito.any(ClusterMetric.class));
    }

    @Test
    public void Given_NonExistentMetricIdentifierIsPassed_When_CreateNewValueForMetric_Then_ThrowsException() {
        UUID randomUUID = UUID.randomUUID();
        double metricValue = 199.99;

        when(cluster.id()).thenReturn(existingClusterId.toString());
        when(metricRepository.findById(Mockito.eq(randomUUID))).thenReturn(Optional.empty());

        assertThrows(MetricNotFoundException.class,
                () -> clusterMetricService.createNewValueForMetric(cluster, randomUUID, metricValue));

        verify(cluster, times(1)).id();
        verify(metricRepository, times(1)).findById(Mockito.eq(randomUUID));
    }

    @Test
    public void Given_ClusterMetricValueIsAlreadyDefinedForGivenCluster_When_CreateNewValueForMetric_Then_ThrowsException() {
        double metricValue = 199.99;

        when(cluster.id()).thenReturn(existingClusterId.toString());
        when(metricRepository.findById(Mockito.eq(metric1.getId()))).thenReturn(Optional.of(metric1));
        when(clusterMetricRepository.findByClusterIdAndMetric(Mockito.eq(existingClusterId), Mockito.eq(metric1))).thenReturn(Optional.of(clusterMetric1));

        assertThrows(ClusterMetricExistsException.class,
                () -> clusterMetricService.createNewValueForMetric(cluster, metric1.getId(), metricValue));

        verify(cluster, times(1)).id();
        verify(metricRepository, times(1)).findById(Mockito.eq(metric1.getId()));
        verify(clusterMetricRepository, times(1)).findByClusterIdAndMetric(Mockito.eq(existingClusterId), Mockito.eq(metric1));
    }

    /* FindAllMetricValuesForCluster method test */

    @Test
    public void Given_SomeClusterMetricValuesAreDefinedForGivenCluster_When_FindAllMetricValuesForCluster_Then_ReturnsAllFoundMetricsValuesSuccessfully() {
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        when(cluster.id()).thenReturn(existingClusterId.toString());
        when(clusterMetricRepository.findAllByClusterId(Mockito.eq(existingClusterId), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(clusterMetric1, clusterMetric2, clusterMetric3), pageable, 3));

        Page<ClusterMetric> foundPage = clusterMetricService.findAllMetricValuesForCluster(cluster, pageable);

        assertNotNull(foundPage);
        assertNotNull(foundPage.getContent());

        List<ClusterMetric> foundClusterMetrics = foundPage.getContent();

        assertNotNull(foundClusterMetrics);
        assertFalse(foundClusterMetrics.isEmpty());
        assertEquals(3, foundClusterMetrics.size());

        ClusterMetric firstClusterMetric = foundClusterMetrics.getFirst();
        assertNotNull(firstClusterMetric);
        assertNotNull(firstClusterMetric.getClusterId());
        assertNotNull(firstClusterMetric.getMetric());
        assertNotNull(firstClusterMetric.getValue());

        assertEquals(existingClusterId, firstClusterMetric.getClusterId());
        assertEquals(metric1, firstClusterMetric.getMetric());
        assertEquals(clusterMetric1.getValue(), firstClusterMetric.getValue());

        ClusterMetric secondClusterMetric = foundClusterMetrics.get(1);
        assertNotNull(secondClusterMetric);
        assertNotNull(secondClusterMetric.getClusterId());
        assertNotNull(secondClusterMetric.getMetric());
        assertNotNull(secondClusterMetric.getValue());

        assertEquals(existingClusterId, secondClusterMetric.getClusterId());
        assertEquals(metric2, secondClusterMetric.getMetric());
        assertEquals(clusterMetric2.getValue(), secondClusterMetric.getValue());

        ClusterMetric thirdClusterMetric = foundClusterMetrics.getLast();
        assertNotNull(thirdClusterMetric);
        assertNotNull(thirdClusterMetric.getClusterId());
        assertNotNull(thirdClusterMetric.getMetric());
        assertNotNull(thirdClusterMetric.getValue());

        assertEquals(existingClusterId, thirdClusterMetric.getClusterId());
        assertEquals(metric3, thirdClusterMetric.getMetric());
        assertEquals(clusterMetric3.getValue(), thirdClusterMetric.getValue());

        verify(cluster, times(1)).id();
        verify(clusterMetricRepository, times(1)).findAllByClusterId(Mockito.eq(existingClusterId), Mockito.eq(pageable));
    }

    @Test
    public void Given_NoClusterMetricValuesAreDefinedForGivenCluster_When_FindAllMetricValuesForCluster_Then_ReturnsEmptyPage() {
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        when(cluster.id()).thenReturn(existingClusterId.toString());
        when(clusterMetricRepository.findAllByClusterId(Mockito.eq(existingClusterId), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        Page<ClusterMetric> foundPage = clusterMetricService.findAllMetricValuesForCluster(cluster, pageable);

        assertNotNull(foundPage);
        assertNotNull(foundPage.getContent());

        List<ClusterMetric> foundClusterMetrics = foundPage.getContent();

        assertNotNull(foundClusterMetrics);
        assertTrue(foundClusterMetrics.isEmpty());

        verify(cluster, times(1)).id();
        verify(clusterMetricRepository, times(1)).findAllByClusterId(Mockito.eq(existingClusterId), Mockito.eq(pageable));
    }

    /* UpdateMetricValue method test */

    @Test
    public void Given_ExistingClusterAndMetricIdentifiersArePassed_When_UpdateMetricValue_Then_UpdatesValueOfGivenMetricSuccessfully() {
        double newMetricValue = 199.99;

        assertEquals(clusterMetric1.getValue(), 99.9999);

        clusterMetric1.setValue(newMetricValue);

        when(cluster.id()).thenReturn(existingClusterId.toString());
        when(metricRepository.findById(Mockito.eq(metric1.getId()))).thenReturn(Optional.of(metric1));
        when(clusterMetricRepository.findByClusterIdAndMetric(Mockito.eq(existingClusterId), Mockito.eq(metric1))).thenReturn(Optional.of(clusterMetric1));
        when(clusterMetricRepository.saveAndFlush(Mockito.eq(clusterMetric1))).thenReturn(clusterMetric1);

        ClusterMetric updatedValue = clusterMetricService.updateMetricValue(cluster, metric1.getId(), newMetricValue);

        assertNotNull(updatedValue);
        assertNotNull(updatedValue.getClusterId());
        assertNotNull(updatedValue.getMetric());
        assertNotNull(updatedValue.getValue());

        assertEquals(newMetricValue, updatedValue.getValue());

        verify(cluster, times(1)).id();
        verify(metricRepository, times(1)).findById(Mockito.eq(metric1.getId()));
        verify(clusterMetricRepository, times(1)).findByClusterIdAndMetric(Mockito.eq(existingClusterId), Mockito.eq(metric1));
        verify(clusterMetricRepository, times(1)).saveAndFlush(Mockito.eq(clusterMetric1));
    }

    @Test
    public void Given_NonExistentMetricIdentifiersIsPassed_When_UpdateMetricValue_Then_ThrowsException() {
        UUID randomUUID = UUID.randomUUID();
        double newMetricValue = 199.99;

        assertEquals(clusterMetric1.getValue(), 99.9999);

        clusterMetric1.setValue(newMetricValue);

        when(cluster.id()).thenReturn(existingClusterId.toString());
        when(metricRepository.findById(Mockito.eq(randomUUID))).thenReturn(Optional.empty());

        assertThrows(MetricNotFoundException.class,
                () -> clusterMetricService.updateMetricValue(cluster, randomUUID, newMetricValue));

        verify(cluster, times(1)).id();
        verify(metricRepository, times(1)).findById(Mockito.eq(randomUUID));
    }

    @Test
    public void Given_ClusterMetricIsNotFound_When_UpdateMetricValue_Then_ThrowsException() {
        double newMetricValue = 199.99;

        assertEquals(clusterMetric1.getValue(), 99.9999);

        clusterMetric1.setValue(newMetricValue);

        when(cluster.id()).thenReturn(existingClusterId.toString());
        when(metricRepository.findById(Mockito.eq(metric1.getId()))).thenReturn(Optional.of(metric1));
        when(clusterMetricRepository.findByClusterIdAndMetric(Mockito.eq(existingClusterId), Mockito.eq(metric1))).thenReturn(Optional.empty());

        assertThrows(ClusterMetricNotFoundException.class,
                () -> clusterMetricService.updateMetricValue(cluster, metric1.getId(), newMetricValue));

        verify(cluster, times(1)).id();
        verify(metricRepository, times(1)).findById(Mockito.eq(metric1.getId()));
        verify(clusterMetricRepository, times(1)).findByClusterIdAndMetric(Mockito.eq(existingClusterId), Mockito.eq(metric1));
    }

    /* DeleteMetricValue method test */

    @Test
    public void Given_ExistingClusterAndMetricIdentifiersArePassed_When_DeleteMetricValue_Then_RemovesMetricValueSuccessfully() {
        when(cluster.id()).thenReturn(existingClusterId.toString());
        when(metricRepository.findById(Mockito.eq(metric1.getId()))).thenReturn(Optional.of(metric1));
        when(clusterMetricRepository.findByClusterIdAndMetric(
                Mockito.eq(existingClusterId), Mockito.eq(metric1))).thenReturn(Optional.of(clusterMetric1));

        doNothing().when(clusterMetricRepository).delete(clusterMetric1);

        clusterMetricService.deleteMetricValue(cluster, metric1.getId());

        verify(cluster, times(1)).id();
        verify(metricRepository, times(1)).findById(Mockito.eq(metric1.getId()));
        verify(clusterMetricRepository, times(1))
                .findByClusterIdAndMetric(Mockito.eq(existingClusterId), Mockito.eq(metric1));

        verify(clusterMetricRepository, times(1)).delete(clusterMetric1);
    }

    @Test
    public void Given_NonExistentMetricIdentifiersIsPassed_When_DeleteMetricValue_Then_ThrowsException() {
        UUID randomUUID = UUID.randomUUID();
        when(cluster.id()).thenReturn(existingClusterId.toString());
        when(metricRepository.findById(Mockito.eq(randomUUID))).thenReturn(Optional.empty());

        assertThrows(MetricNotFoundException.class,
                () -> clusterMetricService.deleteMetricValue(cluster, randomUUID));

        verify(cluster, times(1)).id();
        verify(metricRepository, times(1)).findById(Mockito.eq(randomUUID));
    }

    @Test
    public void Given_ClusterMetricIsNotFound_When_DeleteMetricValue_Then_ThrowsException() {
        when(cluster.id()).thenReturn(existingClusterId.toString());
        when(metricRepository.findById(Mockito.eq(metric1.getId()))).thenReturn(Optional.of(metric1));
        when(clusterMetricRepository.findByClusterIdAndMetric(
                Mockito.eq(existingClusterId), Mockito.eq(metric1))).thenReturn(Optional.empty());

        assertThrows(ClusterMetricNotFoundException.class,
                () -> clusterMetricService.deleteMetricValue(cluster, metric1.getId()));

        verify(cluster, times(1)).id();
        verify(metricRepository, times(1)).findById(Mockito.eq(metric1.getId()));
        verify(clusterMetricRepository, times(1))
                .findByClusterIdAndMetric(Mockito.eq(existingClusterId), Mockito.eq(metric1));
    }
}
