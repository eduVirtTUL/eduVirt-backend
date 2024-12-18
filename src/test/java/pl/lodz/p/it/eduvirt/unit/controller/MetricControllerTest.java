package pl.lodz.p.it.eduvirt.unit.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
import pl.lodz.p.it.eduvirt.controller.MetricController;
import pl.lodz.p.it.eduvirt.dto.metric.CreateMetricDto;
import pl.lodz.p.it.eduvirt.dto.metric.MetricDto;
import pl.lodz.p.it.eduvirt.dto.pagination.PageDto;
import pl.lodz.p.it.eduvirt.dto.pagination.PageInfoDto;
import pl.lodz.p.it.eduvirt.entity.eduvirt.AbstractEntity;
import pl.lodz.p.it.eduvirt.entity.eduvirt.general.Metric;
import pl.lodz.p.it.eduvirt.exceptions.metric.MetricNotFoundException;
import pl.lodz.p.it.eduvirt.mappers.MetricMapper;
import pl.lodz.p.it.eduvirt.service.MetricService;

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
        MetricController.class,
        MetricControllerExceptionResolver.class,
        GeneralControllerExceptionResolver.class,
        OVirtAPIExceptionResolver.class
})
@WebMvcTest(controllers = {MetricController.class}, useDefaultFilters = false)
public class MetricControllerTest {

    @MockitoBean
    private MetricService metricService;

    /* Mappers */

    @MockitoBean
    private MetricMapper metricMapper;

    /* Other */

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper();

    /* Initialization */

    private Metric metric1;
    private Metric metric2;
    private Metric metric3;

    @BeforeEach
    public void prepareTestData() throws Exception {
        Field id = AbstractEntity.class.getDeclaredField("id");

        String metricName1 = "metric_name_no1";
        metric1 = new Metric(metricName1);
        String metricName2 = "metric_name_no2";
        metric2 = new Metric(metricName2);
        String metricName3 = "metric_name_no3";
        metric3 = new Metric(metricName3);

        id.setAccessible(true);
        id.set(metric1, UUID.randomUUID());
        id.set(metric2, UUID.randomUUID());
        id.set(metric3, UUID.randomUUID());
        id.setAccessible(false);
    }

    /* Tests */

    /* CreateNewMetric method tests */

    @WithMockUser
    @Test
    public void Given_NewMetricNameIsPassed_When_CreateNewMetric_Then_CreatesNewMetricSuccessfully() throws Exception {
        String newMetricName = "new_metric_name";
        CreateMetricDto createDto = new CreateMetricDto(newMetricName);

        doNothing().when(metricService).createNewMetric(Mockito.eq(newMetricName));

        mockMvc.perform(post("/metrics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(metricService, times(1)).createNewMetric(Mockito.eq(newMetricName));
    }

    @WithMockUser
    // @Test
    public void Given_ExistingMetricNameIsPassed_When_CreateNewMetric_Then_Returns409Conflict() throws Exception {
        String newMetricName = "metric_name_no1";
        CreateMetricDto createDto = new CreateMetricDto(newMetricName);

        doThrow().when(metricService).createNewMetric(Mockito.eq(newMetricName));

        mockMvc.perform(post("/metrics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isConflict());

        verify(metricService, times(1)).createNewMetric(Mockito.eq(newMetricName));
    }

    /* GetAllMetrics method tests */

    @WithMockUser
    @Test
    public void Given_SomeMetricsExistInTheEduVirtDB_When_GetAllMetrics_Then_ReturnsAllFoundMetrics() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        MetricDto dtoNo1 = new MetricDto(metric1.getId(), metric1.getName());
        MetricDto dtoNo2 = new MetricDto(metric2.getId(), metric2.getName());
        MetricDto dtoNo3 = new MetricDto(metric3.getId(), metric3.getName());

        when(metricService.findAllMetrics(Mockito.eq(pageNumber), Mockito.eq(pageSize)))
                .thenReturn(new PageImpl<>(List.of(metric1, metric2, metric3), pageable, 3));
        when(metricMapper.metricToDto(Mockito.any(Metric.class))).thenReturn(dtoNo1, dtoNo2, dtoNo3);

        MvcResult result = mockMvc.perform(get("/metrics")
                        .param("pageNumber", "0")
                        .param("pageSize", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        PageDto<MetricDto> foundPage = mapper.readValue(json, new TypeReference<>() {});

        assertNotNull(foundPage);
        assertNotNull(foundPage.page());
        assertNotNull(foundPage.items());

        PageInfoDto pageInfo = foundPage.page();

        assertNotNull(pageInfo);
        assertEquals(pageInfo.page(), 0);
        assertEquals(pageInfo.elements(), 3);
        assertEquals(pageInfo.totalPages(), 1);
        assertEquals(pageInfo.totalElements(), 3);

        List<MetricDto> foundMetrics = foundPage.items();

        assertNotNull(foundMetrics);
        assertFalse(foundMetrics.isEmpty());
        assertEquals(3, foundMetrics.size());

        MetricDto firstMetric = foundMetrics.getFirst();
        assertNotNull(firstMetric);
        assertEquals(firstMetric.id(), metric1.getId());
        assertEquals(firstMetric.name(), metric1.getName());

        MetricDto secondMetric = foundMetrics.get(1);
        assertNotNull(secondMetric);
        assertEquals(secondMetric.id(), metric2.getId());
        assertEquals(secondMetric.name(), metric2.getName());

        MetricDto thirdMetric = foundMetrics.getLast();
        assertNotNull(thirdMetric);
        assertEquals(thirdMetric.id(), metric3.getId());
        assertEquals(thirdMetric.name(), metric3.getName());

        verify(metricService, times(1)).findAllMetrics(Mockito.eq(pageNumber), Mockito.eq(pageSize));
        verify(metricMapper, times(3)).metricToDto(Mockito.any(Metric.class));
    }

    @WithMockUser
    @Test
    public void Given_NoMetricsExistInTheEduVirtDB_When_GetAllMetrics_Then_ReturnsEmptyListOfMetrics() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        when(metricService.findAllMetrics(Mockito.eq(pageNumber), Mockito.eq(pageSize)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 3));

        mockMvc.perform(get("/metrics")
                        .param("pageNumber", "0")
                        .param("pageSize", "10"))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(metricService, times(1)).findAllMetrics(Mockito.eq(pageNumber), Mockito.eq(pageSize));
    }

    /* DeleteMetric method tests */

    @WithMockUser
    @Test
    public void Given_ExistingMetricIdentifierWasPassed_When_DeleteMetric_Then_RemovesFoundMetricSuccessfully() throws Exception {
        doNothing().when(metricService).deleteMetric(Mockito.eq(metric1.getId()));

        mockMvc.perform(delete("/metrics/{metricId}", metric1.getId())
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(metricService, times(1)).deleteMetric(Mockito.eq(metric1.getId()));
    }

    @WithMockUser
    @Test
    public void Given_NonExistentMetricIdentifierWasPassed_When_DeleteMetric_Then_Returns400BadRequest() throws Exception {
        UUID randomUUID = UUID.randomUUID();
        doThrow(MetricNotFoundException.class).when(metricService).deleteMetric(Mockito.eq(randomUUID));

        mockMvc.perform(delete("/metrics/{metricId}", randomUUID)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(metricService, times(1)).deleteMetric(Mockito.eq(randomUUID));
    }
}
