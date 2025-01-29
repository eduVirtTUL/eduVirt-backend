package pl.lodz.p.it.eduvirt.unit.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.ovirt.engine.sdk4.types.Cluster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.lodz.p.it.eduvirt.aspect.exception.GeneralControllerExceptionResolver;
import pl.lodz.p.it.eduvirt.controller.ClusterMetricController;
import pl.lodz.p.it.eduvirt.dto.metric.CreateMetricValueDto;
import pl.lodz.p.it.eduvirt.dto.metric.MetricValueDto;
import pl.lodz.p.it.eduvirt.dto.metric.ValueDto;
import pl.lodz.p.it.eduvirt.dto.pagination.PageDto;
import pl.lodz.p.it.eduvirt.dto.pagination.PageInfoDto;
import pl.lodz.p.it.eduvirt.entity.AbstractEntity;
import pl.lodz.p.it.eduvirt.entity.Metric;
import pl.lodz.p.it.eduvirt.entity.ClusterMetric;
import pl.lodz.p.it.eduvirt.entity.Updatable;
import pl.lodz.p.it.eduvirt.exceptions.ClusterNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.MetricNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.ClusterMetricExistsException;
import pl.lodz.p.it.eduvirt.exceptions.ClusterMetricNotFoundException;
import pl.lodz.p.it.eduvirt.mappers.ClusterMetricMapper;
import pl.lodz.p.it.eduvirt.mappers.ClusterMetricMapperImpl;
import pl.lodz.p.it.eduvirt.mappers.MetricMapper;
import pl.lodz.p.it.eduvirt.mappers.MetricMapperImpl;
import pl.lodz.p.it.eduvirt.service.ClusterMetricService;
import pl.lodz.p.it.eduvirt.service.MetricService;
import pl.lodz.p.it.eduvirt.service.ovirt.impl.OVirtClusterServiceImpl;
import pl.lodz.p.it.eduvirt.util.etag.ETagHelper;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import({
        ClusterMetricController.class,
        GeneralControllerExceptionResolver.class,
        ClusterMetricMapperImpl.class, MetricMapperImpl.class
})
@WebMvcTest(controllers = {ClusterMetricController.class}, useDefaultFilters = false)
public class ClusterMetricControllerTest {

    @MockitoBean
    private ClusterMetricService clusterMetricService;

    @MockitoBean
    private MetricService metricService;

    @MockitoBean
    private OVirtClusterServiceImpl oVirtClusterServiceImpl;

    @MockitoBean
    private ETagHelper eTagHelper;

    /* Mappers */

    @MockitoSpyBean
    private ClusterMetricMapper clusterMetricMapper;

    @MockitoSpyBean
    private MetricMapper metricMapper;

    /* Other */

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper();

    /* Initialization */

    private final UUID existingClusterId = UUID.randomUUID();
    private final UUID nonExistentClusterId = UUID.randomUUID();

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
        Field version = Updatable.class.getDeclaredField("version");

        metric1 = new Metric(metricName1, Metric.MetricCategory.COUNTABLE);
        metric2 = new Metric(metricName2, Metric.MetricCategory.MEMORY);
        metric3 = new Metric(metricName3, Metric.MetricCategory.MEMORY);

        clusterMetric1 = new ClusterMetric(existingClusterId, metric1, 99.9999);
        clusterMetric2 = new ClusterMetric(existingClusterId, metric2, 999.999);
        clusterMetric3 = new ClusterMetric(existingClusterId, metric3, 9999.99);

        id.setAccessible(true);
        id.set(metric1, UUID.randomUUID());
        id.set(metric2, UUID.randomUUID());
        id.set(metric3, UUID.randomUUID());

        id.set(clusterMetric1, UUID.randomUUID());
        id.set(clusterMetric2, UUID.randomUUID());
        id.set(clusterMetric3, UUID.randomUUID());
        id.setAccessible(false);

        version.setAccessible(true);
        version.set(clusterMetric1, 0L);
        version.set(clusterMetric2, 0L);
        version.set(clusterMetric3, 0L);
        version.setAccessible(false);
    }

    /* Tests */

    /* CreateMetricValue method tests */

    @WithMockUser
    @Test
    public void Given_MetricValueDoesNotExistForGivenCluster_When_CreateMetricValue_Then_CreatesNewMetricValueSuccessfully() throws Exception {
        double metricValue = 100.00;
        CreateMetricValueDto createDto = new CreateMetricValueDto(
                metric1.getId(),
                metricValue
        );

        Cluster cluster = mock(Cluster.class);

        when(oVirtClusterServiceImpl.findClusterById(existingClusterId)).thenReturn(cluster);
        doNothing().when(clusterMetricService).createNewValueForMetric(cluster, metric1.getId(), metricValue);

        mockMvc.perform(post("/clusters/{clusterId}/metrics", existingClusterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(oVirtClusterServiceImpl, times(1)).findClusterById(existingClusterId);
        verify(clusterMetricService, times(1)).createNewValueForMetric(cluster, metric1.getId(), metricValue);
    }

    @WithMockUser
    @Test
    public void Given_NonExistentClusterIdentifierIsPassed_When_CreateMetricValue_Then_Returns400BadRequest() throws Exception {
        double metricValue = 100.00;
        CreateMetricValueDto createDto = new CreateMetricValueDto(
                metric1.getId(),
                metricValue
        );

        when(oVirtClusterServiceImpl.findClusterById(nonExistentClusterId))
                .thenThrow(ClusterNotFoundException.class);

        mockMvc.perform(post("/clusters/{clusterId}/metrics", nonExistentClusterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(oVirtClusterServiceImpl, times(1))
                .findClusterById(nonExistentClusterId);
    }

    @WithMockUser
    @Test
    public void Given_NonExistentMetricIdentifierIsPassed_When_CreateMetricValue_Then_Returns409Conflict() throws Exception {
        double metricValue = 100.00;
        UUID metricId = UUID.randomUUID();
        CreateMetricValueDto createDto = new CreateMetricValueDto(
                metricId,
                metricValue
        );

        Cluster cluster = mock(Cluster.class);

        when(oVirtClusterServiceImpl.findClusterById(existingClusterId)).thenReturn(cluster);

        doThrow(MetricNotFoundException.class).when(clusterMetricService)
                .createNewValueForMetric(cluster, metricId, metricValue);

        mockMvc.perform(post("/clusters/{clusterId}/metrics", existingClusterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(oVirtClusterServiceImpl, times(1)).findClusterById(existingClusterId);

        verify(clusterMetricService, times(1))
                .createNewValueForMetric(cluster, metricId, metricValue);
    }

    @WithMockUser
    @Test
    public void Given_MetricValueAlreadyExistsForGivenCluster_When_CreateMetricValue_Then_Returns409Conflict() throws Exception {
        double metricValue = 100.00;
        CreateMetricValueDto createDto = new CreateMetricValueDto(
                metric1.getId(),
                metricValue
        );

        Cluster cluster = mock(Cluster.class);

        when(oVirtClusterServiceImpl.findClusterById(existingClusterId)).thenReturn(cluster);

        doThrow(ClusterMetricExistsException.class).when(clusterMetricService)
                .createNewValueForMetric(cluster, metric1.getId(), metricValue);

        mockMvc.perform(post("/clusters/{clusterId}/metrics", existingClusterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(oVirtClusterServiceImpl, times(1)).findClusterById(existingClusterId);

        verify(clusterMetricService, times(1))
                .createNewValueForMetric(cluster, metric1.getId(), metricValue);
    }

    /* GetAllMetricValues method tests */

    @WithMockUser
    @Test
    public void Given_SomeMetricValuesAreDefinedForGivenCluster_When_GetAllMetricValues_Then_ReturnsAllFoundMetricValuesForGivenCluster() throws Exception {
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);

        Cluster cluster = mock(Cluster.class);

        MetricValueDto metricValueDto1 = new MetricValueDto(metric1.getId(), metric1.getName(), Metric.MetricCategory.COUNTABLE, clusterMetric1.getValue());
        MetricValueDto metricValueDto2 = new MetricValueDto(metric2.getId(), metric2.getName(), Metric.MetricCategory.MEMORY, clusterMetric2.getValue());
        MetricValueDto metricValueDto3 = new MetricValueDto(metric3.getId(), metric3.getName(), Metric.MetricCategory.MEMORY, clusterMetric3.getValue());

        when(oVirtClusterServiceImpl.findClusterById(existingClusterId)).thenReturn(cluster);

        when(clusterMetricService.findAllMetricValuesForCluster(cluster, pageable))
                .thenReturn(new PageImpl<>(List.of(clusterMetric1, clusterMetric2, clusterMetric3), pageable, 3));

        when(clusterMetricMapper.clusterMetricToDto(any(ClusterMetric.class)))
                .thenReturn(metricValueDto1, metricValueDto2, metricValueDto3);

        MvcResult result = mockMvc.perform(get("/clusters/{clusterId}/metrics", existingClusterId)
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        PageDto<MetricValueDto> foundPage = mapper.readValue(json, new TypeReference<>() {
        });

        assertNotNull(foundPage);
        assertNotNull(foundPage.page());
        assertNotNull(foundPage.items());

        PageInfoDto pageInfo = foundPage.page();
        assertNotNull(pageInfo);
        assertEquals(pageInfo.page(), 0);
        assertEquals(pageInfo.elements(), 3);
        assertEquals(pageInfo.totalPages(), 1);
        assertEquals(pageInfo.totalElements(), 3);

        List<MetricValueDto> foundMetricValues = foundPage.items();
        assertNotNull(foundMetricValues);
        assertFalse(foundMetricValues.isEmpty());
        assertEquals(3, foundMetricValues.size());

        MetricValueDto firstMetricValue = foundMetricValues.getFirst();
        assertNotNull(firstMetricValue);
        assertEquals(firstMetricValue.id(), metric1.getId());
        assertEquals(firstMetricValue.name(), metric1.getName());
        assertEquals(firstMetricValue.category(), metric1.getCategory());
        assertEquals(firstMetricValue.value(), clusterMetric1.getValue());

        MetricValueDto secondMetricValue = foundMetricValues.get(1);
        assertNotNull(secondMetricValue);
        assertEquals(secondMetricValue.id(), metric2.getId());
        assertEquals(secondMetricValue.name(), metric2.getName());
        assertEquals(secondMetricValue.category(), metric2.getCategory());
        assertEquals(secondMetricValue.value(), clusterMetric2.getValue());

        MetricValueDto thirdMetricValue = foundMetricValues.getLast();
        assertNotNull(thirdMetricValue);
        assertEquals(thirdMetricValue.id(), metric3.getId());
        assertEquals(thirdMetricValue.name(), metric3.getName());
        assertEquals(thirdMetricValue.category(), metric3.getCategory());
        assertEquals(thirdMetricValue.value(), clusterMetric3.getValue());

        verify(oVirtClusterServiceImpl, times(1))
                .findClusterById(existingClusterId);

        verify(clusterMetricService, times(1))
                .findAllMetricValuesForCluster(cluster, pageable);

        verify(clusterMetricMapper, times(3))
                .clusterMetricToDto(any(ClusterMetric.class));
    }

    @WithMockUser
    @Test
    public void Given_NonExistentClusterIdentifierIsPassed_When_GetAllMetricValues_Then_Returns400BadRequest() throws Exception {
        int page = 0;
        int size = 10;

        when(oVirtClusterServiceImpl.findClusterById(nonExistentClusterId))
                .thenThrow(ClusterNotFoundException.class);

        mockMvc.perform(get("/clusters/{clusterId}/metrics", nonExistentClusterId)
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(oVirtClusterServiceImpl, times(1))
                .findClusterById(nonExistentClusterId);
    }

    @WithMockUser
    @Test
    public void Given_NoMetricValuesAreDefinedForGivenCluster_When_GetAllMetricValues_Then_ReturnsEmptyClusterMetricValueList() throws Exception {
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);

        Cluster cluster = mock(Cluster.class);

        when(oVirtClusterServiceImpl.findClusterById(existingClusterId)).thenReturn(cluster);

        when(clusterMetricService.findAllMetricValuesForCluster(cluster, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        mockMvc.perform(get("/clusters/{clusterId}/metrics", existingClusterId)
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(oVirtClusterServiceImpl, times(1)).findClusterById(existingClusterId);

        verify(clusterMetricService, times(1))
                .findAllMetricValuesForCluster(cluster, pageable);
    }

    /* UpdateMetricValue method tests */

    @WithMockUser
    @Test
    public void Given_ExistingClusterAndMetricIdentifiersArePassed_When_UpdateMetricValue_Then_UpdatesMetricValueSuccessfully() throws Exception {
        String ifMatch = "VALID_IF_MATCH_HEADER_CONTENT";
        clusterMetric1.setValue(100.0);
        ValueDto newValueDto = new ValueDto(clusterMetric1.getId(), clusterMetric1.getVersion(), 100.0);

        ClusterMetric newMetric1 = new ClusterMetric(
                metric1.getId(),
                metric1,
                newValueDto.value()
        );

        MetricValueDto metricValueDto = new MetricValueDto(
                metric1.getId(),
                metric1.getName(),
                Metric.MetricCategory.COUNTABLE,
                newValueDto.value()
        );

        ClusterMetric updateClusterMetric = new ClusterMetric(
                clusterMetric1.getClusterId(),
                clusterMetric1.getMetric(),
                clusterMetric1.getValue()
        );

        Cluster cluster = mock(Cluster.class);
        when(cluster.id()).thenReturn(existingClusterId.toString());
        when(oVirtClusterServiceImpl.findClusterById(existingClusterId)).thenReturn(cluster);
        when(metricService.findById(metric1.getId())).thenReturn(metric1);

        when(clusterMetricService.updateMetricValue(eq(clusterMetric1.getId()), any(ClusterMetric.class), eq(ifMatch)))
                .thenReturn(newMetric1);

        when(clusterMetricMapper.clusterMetricToDto(newMetric1)).thenReturn(metricValueDto);

        MvcResult result = mockMvc.perform(patch("/clusters/{clusterId}/metrics/{metricIUd}", existingClusterId, metric1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newValueDto))
                        .header(HttpHeaders.IF_MATCH, ifMatch)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        MetricValueDto foundMetricValue = mapper.readValue(json, MetricValueDto.class);

        assertNotNull(foundMetricValue);
        assertNotNull(foundMetricValue.id());
        assertEquals(foundMetricValue.id(), metric1.getId());
        assertNotNull(foundMetricValue.name());
        assertEquals(foundMetricValue.name(), metric1.getName());
        assertEquals(foundMetricValue.value(), newValueDto.value());

        verify(cluster, times(1)).id();
        verify(oVirtClusterServiceImpl, times(1)).findClusterById(existingClusterId);
        verify(metricService, times(1)).findById(metric1.getId());

        verify(clusterMetricService, times(1))
                .updateMetricValue(eq(clusterMetric1.getId()), any(ClusterMetric.class), eq(ifMatch));

        verify(clusterMetricMapper, times(1)).clusterMetricToDto(newMetric1);
    }

    @WithMockUser
    @Test
    public void Given_NonExistentClusterIdentifierIsPassed_When_UpdateMetricValue_Then_Returns400BadRequest() throws Exception {
        String ifMatch = "VALID_IF_MATCH_HEADER_CONTENT";
        ValueDto newValueDto = new ValueDto(clusterMetric1.getId(), clusterMetric1.getVersion(), 100.0);

        when(oVirtClusterServiceImpl.findClusterById(nonExistentClusterId))
                .thenThrow(ClusterNotFoundException.class);

        mockMvc.perform(patch("/clusters/{clusterId}/metrics/{metricIUd}", nonExistentClusterId, metric1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newValueDto))
                        .header(HttpHeaders.IF_MATCH, ifMatch)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(oVirtClusterServiceImpl, times(1)).findClusterById(nonExistentClusterId);
    }

    @WithMockUser
    @Test
    public void Given_NonExistentMetricIdentifierIsPassed_When_UpdateMetricValue_Then_Returns400BadRequest() throws Exception {
        UUID randomUUID = UUID.randomUUID();
        String ifMatch = "VALID_IF_MATCH_HEADER_CONTENT";
        ValueDto newValueDto = new ValueDto(clusterMetric1.getId(), clusterMetric1.getVersion(), 100.0);

        Cluster cluster = mock(Cluster.class);
        when(oVirtClusterServiceImpl.findClusterById(existingClusterId)).thenReturn(cluster);
        when(metricService.findById(randomUUID)).thenThrow(MetricNotFoundException.class);

        mockMvc.perform(patch("/clusters/{clusterId}/metrics/{metricIUd}", existingClusterId, randomUUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newValueDto))
                        .header(HttpHeaders.IF_MATCH, ifMatch)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(oVirtClusterServiceImpl, times(1)).findClusterById(existingClusterId);
        verify(metricService, times(1)).findById(randomUUID);
    }

    @WithMockUser
    @Test
    public void Given_ClusterMetricValueCouldNotBeFound_When_UpdateMetricValue_Then_Returns400BadRequest() throws Exception {
        UUID nonExistentClusterMetricId = UUID.randomUUID();
        String ifMatch = "VALID_IF_MATCH_HEADER_CONTENT";
        ValueDto newValueDto = new ValueDto(nonExistentClusterMetricId, clusterMetric1.getVersion(), 100.0);

        Cluster cluster = mock(Cluster.class);
        when(cluster.id()).thenReturn(existingClusterId.toString());
        when(oVirtClusterServiceImpl.findClusterById(existingClusterId)).thenReturn(cluster);
        when(metricService.findById(metric1.getId())).thenReturn(metric1);

        when(clusterMetricService.updateMetricValue(eq(nonExistentClusterMetricId), any(ClusterMetric.class),
                eq(ifMatch))).thenThrow(ClusterMetricNotFoundException.class);

        mockMvc.perform(patch("/clusters/{clusterId}/metrics/{metricIUd}", existingClusterId, metric1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newValueDto))
                        .header(HttpHeaders.IF_MATCH, ifMatch)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(cluster, times(1)).id();
        verify(oVirtClusterServiceImpl, times(1)).findClusterById(existingClusterId);
        verify(metricService, times(1)).findById(metric1.getId());

        verify(clusterMetricService, times(1))
                .updateMetricValue(eq(nonExistentClusterMetricId), any(ClusterMetric.class), eq(ifMatch));
    }

    /* DeleteMetric method tests */

    @WithMockUser
    @Test
    public void Given_ExistingClusterAndMetricIdentifiersArePassed_When_DeleteMetric_Then_RemovesGivenMetricSuccessfully() throws Exception {
        Cluster cluster = mock(Cluster.class);

        when(oVirtClusterServiceImpl.findClusterById(existingClusterId)).thenReturn(cluster);
        doNothing().when(clusterMetricService).deleteMetricValue(cluster, metric1.getId());

        mockMvc.perform(delete("/clusters/{clusterId}/metrics/{metricId}", existingClusterId, metric1.getId())
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(oVirtClusterServiceImpl, times(1)).findClusterById(existingClusterId);
        verify(clusterMetricService, times(1)).deleteMetricValue(cluster, metric1.getId());
    }

    @WithMockUser
    @Test
    public void Given_NonExistentClusterIdentifierIsPassed_When_DeleteMetric_Then_Returns400BadRequest() throws Exception {
        when(oVirtClusterServiceImpl.findClusterById(nonExistentClusterId)).thenThrow(ClusterNotFoundException.class);

        mockMvc.perform(delete("/clusters/{clusterId}/metrics/{metricId}", nonExistentClusterId, metric1.getId())
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(oVirtClusterServiceImpl, times(1)).findClusterById(nonExistentClusterId);
    }

    @WithMockUser
    @Test
    public void Given_NonExistentMetricIdentifierIsPassed_When_DeleteMetric_Then_Returns400BadRequest() throws Exception {
        UUID randomUUID = UUID.randomUUID();
        Cluster cluster = mock(Cluster.class);

        when(oVirtClusterServiceImpl.findClusterById(existingClusterId)).thenReturn(cluster);
        doThrow(MetricNotFoundException.class).when(clusterMetricService).deleteMetricValue(cluster, randomUUID);

        mockMvc.perform(delete("/clusters/{clusterId}/metrics/{metricId}", existingClusterId, randomUUID)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(oVirtClusterServiceImpl, times(1)).findClusterById(existingClusterId);
        verify(clusterMetricService, times(1)).deleteMetricValue(cluster, randomUUID);
    }

    @WithMockUser
    @Test
    public void Given_ClusterMetricValueIsNotDefinedForTGivenMetric_When_DeleteMetric_Then_Returns400BadRequest() throws Exception {
        Cluster cluster = mock(Cluster.class);

        when(oVirtClusterServiceImpl.findClusterById(existingClusterId)).thenReturn(cluster);
        doThrow(ClusterMetricNotFoundException.class).when(clusterMetricService).deleteMetricValue(cluster, metric1.getId());

        mockMvc.perform(delete("/clusters/{clusterId}/metrics/{metricId}", existingClusterId, metric1.getId())
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(oVirtClusterServiceImpl, times(1)).findClusterById(existingClusterId);
        verify(clusterMetricService, times(1)).deleteMetricValue(cluster, metric1.getId());
    }
}
