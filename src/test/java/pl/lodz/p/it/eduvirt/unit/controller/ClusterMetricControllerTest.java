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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.lodz.p.it.eduvirt.aspect.exception.GeneralControllerExceptionResolver;
import pl.lodz.p.it.eduvirt.aspect.exception.MetricControllerExceptionResolver;
import pl.lodz.p.it.eduvirt.aspect.exception.OVirtAPIExceptionResolver;
import pl.lodz.p.it.eduvirt.controller.ClusterMetricController;
import pl.lodz.p.it.eduvirt.dto.metric.CreateMetricValueDto;
import pl.lodz.p.it.eduvirt.dto.metric.MetricValueDto;
import pl.lodz.p.it.eduvirt.dto.metric.ValueDto;
import pl.lodz.p.it.eduvirt.dto.pagination.PageDto;
import pl.lodz.p.it.eduvirt.dto.pagination.PageInfoDto;
import pl.lodz.p.it.eduvirt.entity.eduvirt.AbstractEntity;
import pl.lodz.p.it.eduvirt.entity.eduvirt.general.Metric;
import pl.lodz.p.it.eduvirt.entity.eduvirt.reservation.ClusterMetric;
import pl.lodz.p.it.eduvirt.exceptions.ClusterNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.metric.MetricNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.metric.MetricValueAlreadyDefined;
import pl.lodz.p.it.eduvirt.exceptions.metric.MetricValueNotDefinedException;
import pl.lodz.p.it.eduvirt.mappers.ClusterMetricMapper;
import pl.lodz.p.it.eduvirt.service.ClusterMetricService;
import pl.lodz.p.it.eduvirt.service.impl.OVirtClusterServiceImpl;

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
        MetricControllerExceptionResolver.class,
        GeneralControllerExceptionResolver.class,
        OVirtAPIExceptionResolver.class
})
@WebMvcTest(controllers = {ClusterMetricController.class}, useDefaultFilters = false)
public class ClusterMetricControllerTest {

    @MockitoBean
    private ClusterMetricService clusterMetricService;

    @MockitoBean
    private OVirtClusterServiceImpl oVirtClusterServiceImpl;

    /* Mappers */

    @MockitoBean
    private ClusterMetricMapper clusterMetricMapper;

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

        metric1 = new Metric(metricName1);
        metric2 = new Metric(metricName2);
        metric3 = new Metric(metricName3);

        clusterMetric1 = new ClusterMetric(existingClusterId, metric1, 99.9999);
        clusterMetric2 = new ClusterMetric(existingClusterId, metric2, 999.999);
        clusterMetric3 = new ClusterMetric(existingClusterId, metric3, 9999.99);

        id.setAccessible(true);
        id.set(metric1, UUID.randomUUID());
        id.set(metric2, UUID.randomUUID());
        id.set(metric3, UUID.randomUUID());
        id.setAccessible(false);
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

        when(oVirtClusterServiceImpl.findClusterById(Mockito.eq(existingClusterId))).thenReturn(cluster);

        doNothing().when(clusterMetricService)
                .createNewValueForMetric(Mockito.eq(cluster), Mockito.eq(metric1.getId()), Mockito.eq(metricValue));

        mockMvc.perform(post("/clusters/{clusterId}/metrics", existingClusterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(oVirtClusterServiceImpl, times(1)).findClusterById(Mockito.eq(existingClusterId));

        verify(clusterMetricService, times(1))
                .createNewValueForMetric(Mockito.eq(cluster), Mockito.eq(metric1.getId()), Mockito.eq(metricValue));
    }

    @WithMockUser
    @Test
    public void Given_NonExistentClusterIdentifierIsPassed_When_CreateMetricValue_Then_Returns400BadRequest() throws Exception {
        double metricValue = 100.00;
        CreateMetricValueDto createDto = new CreateMetricValueDto(
                metric1.getId(),
                metricValue
        );

        when(oVirtClusterServiceImpl.findClusterById(Mockito.eq(nonExistentClusterId)))
                .thenThrow(ClusterNotFoundException.class);

        mockMvc.perform(post("/clusters/{clusterId}/metrics", nonExistentClusterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(oVirtClusterServiceImpl, times(1))
                .findClusterById(Mockito.eq(nonExistentClusterId));
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

        when(oVirtClusterServiceImpl.findClusterById(Mockito.eq(existingClusterId))).thenReturn(cluster);

        doThrow(MetricNotFoundException.class).when(clusterMetricService)
                .createNewValueForMetric(Mockito.eq(cluster), Mockito.eq(metricId), Mockito.eq(metricValue));

        mockMvc.perform(post("/clusters/{clusterId}/metrics", existingClusterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(oVirtClusterServiceImpl, times(1)).findClusterById(Mockito.eq(existingClusterId));

        verify(clusterMetricService, times(1))
                .createNewValueForMetric(Mockito.eq(cluster), Mockito.eq(metricId), Mockito.eq(metricValue));
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

        when(oVirtClusterServiceImpl.findClusterById(Mockito.eq(existingClusterId))).thenReturn(cluster);

        doThrow(MetricValueAlreadyDefined.class).when(clusterMetricService)
                .createNewValueForMetric(Mockito.eq(cluster), Mockito.eq(metric1.getId()), Mockito.eq(metricValue));

        mockMvc.perform(post("/clusters/{clusterId}/metrics", existingClusterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isConflict());

        verify(oVirtClusterServiceImpl, times(1)).findClusterById(Mockito.eq(existingClusterId));

        verify(clusterMetricService, times(1))
                .createNewValueForMetric(Mockito.eq(cluster), Mockito.eq(metric1.getId()), Mockito.eq(metricValue));
    }

    /* GetAllMetricValues method tests */

    @WithMockUser
    @Test
    public void Given_SomeMetricValuesAreDefinedForGivenCluster_When_GetAllMetricValues_Then_ReturnsAllFoundMetricValuesForGivenCluster() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Cluster cluster = mock(Cluster.class);

        MetricValueDto metricValueDto1 = new MetricValueDto(metric1.getId(), metric1.getName(), clusterMetric1.getValue());
        MetricValueDto metricValueDto2 = new MetricValueDto(metric2.getId(), metric2.getName(), clusterMetric2.getValue());
        MetricValueDto metricValueDto3 = new MetricValueDto(metric3.getId(), metric3.getName(), clusterMetric3.getValue());

        when(oVirtClusterServiceImpl.findClusterById(Mockito.eq(existingClusterId)))
                .thenReturn(cluster);

        when(clusterMetricService.findAllMetricValuesForCluster(Mockito.eq(cluster), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(clusterMetric1, clusterMetric2, clusterMetric3), pageable, 3));

        when(clusterMetricMapper.clusterMetricToDto(Mockito.any(ClusterMetric.class)))
                .thenReturn(metricValueDto1, metricValueDto2, metricValueDto3);

        MvcResult result = mockMvc.perform(get("/clusters/{clusterId}/metrics", existingClusterId)
                        .param("pageNumber", String.valueOf(pageNumber))
                        .param("pageSize", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        PageDto<MetricValueDto> foundPage = mapper.readValue(json, new TypeReference<>() {});

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
        assertEquals(firstMetricValue.value(), clusterMetric1.getValue());

        MetricValueDto secondMetricValue = foundMetricValues.get(1);
        assertNotNull(secondMetricValue);
        assertEquals(secondMetricValue.id(), metric2.getId());
        assertEquals(secondMetricValue.name(), metric2.getName());
        assertEquals(secondMetricValue.value(), clusterMetric2.getValue());

        MetricValueDto thirdMetricValue = foundMetricValues.getLast();
        assertNotNull(thirdMetricValue);
        assertEquals(thirdMetricValue.id(), metric3.getId());
        assertEquals(thirdMetricValue.name(), metric3.getName());
        assertEquals(thirdMetricValue.value(), clusterMetric3.getValue());

        verify(oVirtClusterServiceImpl, times(1))
                .findClusterById(Mockito.eq(existingClusterId));

        verify(clusterMetricService, times(1))
                .findAllMetricValuesForCluster(Mockito.eq(cluster), Mockito.eq(pageable));

        verify(clusterMetricMapper, times(3))
                .clusterMetricToDto(Mockito.any(ClusterMetric.class));
    }

    @WithMockUser
    @Test
    public void Given_NonExistentClusterIdentifierIsPassed_When_GetAllMetricValues_Then_Returns400BadRequest() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;

        when(oVirtClusterServiceImpl.findClusterById(Mockito.eq(nonExistentClusterId)))
                .thenThrow(ClusterNotFoundException.class);

        mockMvc.perform(get("/clusters/{clusterId}/metrics", nonExistentClusterId)
                        .param("pageNumber", String.valueOf(pageNumber))
                        .param("pageSize", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(oVirtClusterServiceImpl, times(1))
                .findClusterById(Mockito.eq(nonExistentClusterId));
    }

    @WithMockUser
    @Test
    public void Given_IncorrectPaginationParametersArePassed_When_GetAllMetricValues_Then_ReturnsEmptyClusterMetricValueList() throws Exception {
        int pageNumber = -1;
        int pageSize = 10;

        Cluster cluster = mock(Cluster.class);
        when(oVirtClusterServiceImpl.findClusterById(Mockito.eq(existingClusterId)))
                .thenReturn(cluster);

        mockMvc.perform(get("/clusters/{clusterId}/metrics", existingClusterId)
                        .param("pageNumber", String.valueOf(pageNumber))
                        .param("pageSize", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(oVirtClusterServiceImpl, times(1))
                .findClusterById(Mockito.eq(existingClusterId));
    }

    @WithMockUser
    @Test
    public void Given_NoMetricValuesAreDefinedForGivenCluster_When_GetAllMetricValues_Then_ReturnsEmptyClusterMetricValueList() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Cluster cluster = mock(Cluster.class);

        when(oVirtClusterServiceImpl.findClusterById(Mockito.eq(existingClusterId)))
                .thenReturn(cluster);

        when(clusterMetricService.findAllMetricValuesForCluster(Mockito.eq(cluster), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        mockMvc.perform(get("/clusters/{clusterId}/metrics", existingClusterId)
                        .param("pageNumber", String.valueOf(pageNumber))
                        .param("pageSize", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(oVirtClusterServiceImpl, times(1))
                .findClusterById(Mockito.eq(existingClusterId));

        verify(clusterMetricService, times(1))
                .findAllMetricValuesForCluster(Mockito.eq(cluster), Mockito.eq(pageable));
    }

    /* UpdateMetricValue method tests */

    @WithMockUser
    @Test
    public void Given_ExistingClusterAndMetricIdentifiersArePassed_When_UpdateMetricValue_Then_UpdatesMetricValueSuccessfully() throws Exception {
        ValueDto newValueDto = new ValueDto(100.00);

        ClusterMetric newMetric1 = new ClusterMetric(
                metric1.getId(),
                metric1,
                newValueDto.value()
        );

        MetricValueDto metricValueDto = new MetricValueDto(
                metric1.getId(),
                metric1.getName(),
                newValueDto.value()
        );

        Cluster cluster = mock(Cluster.class);
        when(oVirtClusterServiceImpl.findClusterById(Mockito.eq(existingClusterId)))
                .thenReturn(cluster);

        when(clusterMetricService.updateMetricValue(Mockito.eq(cluster), Mockito.eq(metric1.getId()), Mockito.eq(newValueDto.value())))
                .thenReturn(newMetric1);

        when(clusterMetricMapper.clusterMetricToDto(Mockito.eq(newMetric1))).thenReturn(metricValueDto);

        MvcResult result = mockMvc.perform(patch("/clusters/{clusterId}/metrics/{metricIUd}", existingClusterId, metric1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newValueDto))
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

        verify(oVirtClusterServiceImpl, times(1)).findClusterById(Mockito.eq(existingClusterId));

        verify(clusterMetricService, times(1))
                .updateMetricValue(Mockito.eq(cluster), Mockito.eq(metric1.getId()), Mockito.eq(newValueDto.value()));

        verify(clusterMetricMapper, times(1)).clusterMetricToDto(Mockito.eq(newMetric1));
    }

    @WithMockUser
    @Test
    public void Given_NonExistentClusterIdentifierIsPassed_When_UpdateMetricValue_Then_Returns400BadRequest() throws Exception {
        ValueDto newValueDto = new ValueDto(100.00);

        when(oVirtClusterServiceImpl.findClusterById(Mockito.eq(nonExistentClusterId)))
                .thenThrow(ClusterNotFoundException.class);

        mockMvc.perform(patch("/clusters/{clusterId}/metrics/{metricIUd}", nonExistentClusterId, metric1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newValueDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(oVirtClusterServiceImpl, times(1)).findClusterById(Mockito.eq(nonExistentClusterId));
    }

    @WithMockUser
    @Test
    public void Given_NonExistentMetricIdentifierIsPassed_When_UpdateMetricValue_Then_Returns400BadRequest() throws Exception {
        UUID randomUUID = UUID.randomUUID();
        ValueDto newValueDto = new ValueDto(100.00);

        Cluster cluster = mock(Cluster.class);
        when(oVirtClusterServiceImpl.findClusterById(Mockito.eq(existingClusterId)))
                .thenReturn(cluster);

        when(clusterMetricService.updateMetricValue(Mockito.eq(cluster), Mockito.eq(randomUUID), Mockito.eq(newValueDto.value())))
                .thenThrow(MetricNotFoundException.class);

        mockMvc.perform(patch("/clusters/{clusterId}/metrics/{metricIUd}", existingClusterId, randomUUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newValueDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(oVirtClusterServiceImpl, times(1)).findClusterById(Mockito.eq(existingClusterId));

        verify(clusterMetricService, times(1))
                .updateMetricValue(Mockito.eq(cluster), Mockito.eq(randomUUID), Mockito.eq(newValueDto.value()));
    }

    @WithMockUser
    @Test
    public void Given_ClusterMetricValueIsNotDefinedForGivenCluster_When_UpdateMetricValue_Then_Returns400BadRequest() throws Exception {
        ValueDto newValueDto = new ValueDto(100.00);

        Cluster cluster = mock(Cluster.class);
        when(oVirtClusterServiceImpl.findClusterById(Mockito.eq(existingClusterId)))
                .thenReturn(cluster);

        when(clusterMetricService.updateMetricValue(Mockito.eq(cluster), Mockito.eq(metric1.getId()), Mockito.eq(newValueDto.value())))
                .thenThrow(MetricValueNotDefinedException.class);

        mockMvc.perform(patch("/clusters/{clusterId}/metrics/{metricIUd}", existingClusterId, metric1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newValueDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(oVirtClusterServiceImpl, times(1)).findClusterById(Mockito.eq(existingClusterId));

        verify(clusterMetricService, times(1))
                .updateMetricValue(Mockito.eq(cluster), Mockito.eq(metric1.getId()), Mockito.eq(newValueDto.value()));
    }

    /* DeleteMetric method tests */

    @WithMockUser
    @Test
    public void Given_ExistingClusterAndMetricIdentifiersArePassed_When_DeleteMetric_Then_RemovesGivenMetricSuccessfully() throws Exception {
        Cluster cluster = mock(Cluster.class);

        when(oVirtClusterServiceImpl.findClusterById(Mockito.eq(existingClusterId)))
                .thenReturn(cluster);

        doNothing().when(clusterMetricService).deleteMetricValue(Mockito.eq(cluster), Mockito.eq(metric1.getId()));

        mockMvc.perform(delete("/clusters/{clusterId}/metrics/{metricId}", existingClusterId, metric1.getId())
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(oVirtClusterServiceImpl, times(1))
                .findClusterById(Mockito.eq(existingClusterId));

        verify(clusterMetricService, times(1))
                .deleteMetricValue(Mockito.eq(cluster), Mockito.eq(metric1.getId()));
    }

    @WithMockUser
    @Test
    public void Given_NonExistentClusterIdentifierIsPassed_When_DeleteMetric_Then_Returns400BadRequest() throws Exception {
        when(oVirtClusterServiceImpl.findClusterById(Mockito.eq(nonExistentClusterId)))
                .thenThrow(ClusterNotFoundException.class);

        mockMvc.perform(delete("/clusters/{clusterId}/metrics/{metricId}", nonExistentClusterId, metric1.getId())
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(oVirtClusterServiceImpl, times(1))
                .findClusterById(Mockito.eq(nonExistentClusterId));
    }

    @WithMockUser
    @Test
    public void Given_NonExistentMetricIdentifierIsPassed_When_DeleteMetric_Then_Returns400BadRequest() throws Exception {
        UUID randomUUID = UUID.randomUUID();
        Cluster cluster = mock(Cluster.class);

        when(oVirtClusterServiceImpl.findClusterById(Mockito.eq(existingClusterId)))
                .thenReturn(cluster);

        doThrow(MetricNotFoundException.class).when(clusterMetricService)
                .deleteMetricValue(Mockito.eq(cluster), Mockito.eq(randomUUID));

        mockMvc.perform(delete("/clusters/{clusterId}/metrics/{metricId}", existingClusterId, randomUUID)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(oVirtClusterServiceImpl, times(1))
                .findClusterById(Mockito.eq(existingClusterId));

        verify(clusterMetricService, times(1))
                .deleteMetricValue(Mockito.eq(cluster), Mockito.eq(randomUUID));
    }

    @WithMockUser
    @Test
    public void Given_ClusterMetricValueIsNotDefinedForTGivenMetric_When_DeleteMetric_Then_Returns400BadRequest() throws Exception {
        Cluster cluster = mock(Cluster.class);

        when(oVirtClusterServiceImpl.findClusterById(Mockito.eq(existingClusterId)))
                .thenReturn(cluster);

        doThrow(MetricValueNotDefinedException.class).when(clusterMetricService)
                .deleteMetricValue(Mockito.eq(cluster), Mockito.eq(metric1.getId()));

        mockMvc.perform(delete("/clusters/{clusterId}/metrics/{metricId}", existingClusterId, metric1.getId())
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(oVirtClusterServiceImpl, times(1))
                .findClusterById(Mockito.eq(existingClusterId));

        verify(clusterMetricService, times(1))
                .deleteMetricValue(Mockito.eq(cluster), Mockito.eq(metric1.getId()));
    }
}
