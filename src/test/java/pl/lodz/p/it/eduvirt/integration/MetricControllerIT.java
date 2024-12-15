package pl.lodz.p.it.eduvirt.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import pl.lodz.p.it.eduvirt.dto.metric.CreateMetricDto;
import pl.lodz.p.it.eduvirt.dto.metric.MetricDto;
import pl.lodz.p.it.eduvirt.dto.pagination.PageDto;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MetricControllerIT extends IntegrationTestBase {

    /**
     * TEST REQUIREMENTS:
     *      Basically, to conduct that test only eduVirt database is needed. Even the test data is not needed.
     *      The app needs to be up, as well as the eduVirt DB.
     */
    // @Test
    public void Given_NewMetricName_When_NoMetricWithThatNameExists_Then_CreatesNewMetricSuccessfully() throws Exception {
        CreateMetricDto createDto = new CreateMetricDto("new_metric_name");

        mockMvc.perform(post("/metrics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(createDto)))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    /**
     * TEST REQUIREMENTS:
     *      To conduct that test some test data in the eduVirt database is required - basically a single metric
     *      with the name, which is used in that test.
     */
    // @Test
    public void Given_ExistingMetricName_When_MetricWithGivenNameAlreadyExists_Then_Returns409Conflict() throws Exception {
        CreateMetricDto createDto = new CreateMetricDto("existing_metric_name");

        mockMvc.perform(post("/metrics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(createDto)))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    /**
     * TEST REQUIREMENTS:
     *      To conduct that test some sample metrics are required, at least 2 to show that the GET /metrics
     *      can return multiple metrics
     */
    // @Test
    public void Given_SomeMetricsExistInTheDatabase_When_GetAllMetrics_Then_ReturnsFoundMetrics() throws Exception {
        MvcResult result = mockMvc.perform(get("/metrics"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        PageDto<MetricDto> foundMetrics = mapper.readValue(json, new TypeReference<>(){});

        assertNotNull(foundMetrics);
        assertNotNull(foundMetrics.items());

        List<MetricDto> metrics = foundMetrics.items();

        assertFalse(metrics.isEmpty());
        assertEquals(metrics.size(), 2);

        MetricDto firstMetric = metrics.getFirst();
        assertEquals(firstMetric.name(), "first_metric_name");

        MetricDto secondMetric = metrics.getLast();
        assertEquals(secondMetric.name(), "second_metric_name");
    }

    /**
     * TEST REQUIREMENTS:
     *      To conduct that test some existing metric is required, which identifier will be passed as
     *      the param of the request.
     */
    // @Test
    public void Given_MetricExists_When_ExistingIdentifierIsPassed_Then_RemovesMetricSuccessfully() throws Exception {
        UUID metricId = UUID.fromString("2230b6d2-2628-4699-bd3a-afe27f2bbfb2");

        mockMvc.perform(delete("/metrics/{metricId}", metricId)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    /**
     * TEST REQUIREMENTS:
     *      That test does not require any metric to existing in the eduVirt DB, since random UUID will be used.
     */
    // @Test
    public void Given_MetricDoesNotExists_When_NonExistentIdentifierIsPassed_Then_Returns400BadRequest() throws Exception {
        UUID metricId = UUID.randomUUID();

        mockMvc.perform(delete("/metrics/{metricId}", metricId)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadGateway());
    }
}
