package pl.lodz.p.it.eduvirt.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import pl.lodz.p.it.eduvirt.dto.metric.CreateMetricValueDto;
import pl.lodz.p.it.eduvirt.dto.metric.MetricValueDto;
import pl.lodz.p.it.eduvirt.dto.metric.ValueDto;
import pl.lodz.p.it.eduvirt.dto.pagination.PageDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ClusterMetricControllerIT extends IntegrationTestBase {

    /**
     * TEST REQUIREMENTS:
     *      This test require the sample cluster, as well as sample metric to exist in oVirt and eduVirt DBs
     *      respectively, since their identifiers are used to create a new value.
     */
    @Test
    public void Given_ClusterAndMetricIsFoundAndMetricDoesNotHaveValueForGivenCluster_When_CreateMetricValue_Then_CreatesMetricValueSuccessfully() throws Exception {
        CreateMetricValueDto metricValueDto = new CreateMetricValueDto(existingMetricId, 100.0);

        mockMvc.perform(post("/clusters/{clusterId}/metrics", existingClusterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(metricValueDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    /**
     * TEST REQUIREMENTS:
     *      That test does not require any data to exist in both oVirt and eduVirt DBs, since it is using
     *      randomly generated identifiers.
     */
    @Test
    public void Given_NonExistentClusterIdentifierIsPassed_When_CreateMetricValue_Then_Throws400BadRequest() throws Exception {
        CreateMetricValueDto metricValueDto = new CreateMetricValueDto(nonExistentMetricId, 100.0);

        mockMvc.perform(post("/clusters/{clusterId}/metrics", nonExistentClusterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(metricValueDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    /**
     * TEST REQUIREMENTS:
     *      That test does require sample cluster to exist within oVirt database.
     *      Besides that, no additional data is required.
     */
    @Test
    public void Given_NonExistentMetricIdentifierIsPassed_When_CreateMetricValue_Then_Throws400BadRequest() throws Exception {
        CreateMetricValueDto metricValueDto = new CreateMetricValueDto(nonExistentMetricId, 100.0);

        mockMvc.perform(post("/clusters/{clusterId}/metrics", existingClusterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(metricValueDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    /**
     * TEST REQUIREMENTS:
     *      This test require the sample cluster, as well as sample metric to exist in oVirt and eduVirt DBs
     *      respectively, since their identifiers are used to create a new value. Apart from those requirements
     *      also example value for given metric and given cluster needs to exist.
     */
    @Test
    public void Given_MetricWithIdentifierPassedAlreadyHasValueForGivenCluster_When_CreateMetricValue_Then_Throws409Conflict() throws Exception {
        CreateMetricValueDto metricValueDto = new CreateMetricValueDto(existingClusterId, 100.0);

        mockMvc.perform(post("/clusters/{clusterId}/metrics", existingClusterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(metricValueDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    /**
     * TEST REQUIREMENTS:
     *      This test case requires sample cluster presence in the oVirt DB, as well as sample metric existence
     *      in the eduVirt DB (at least two of them). Apart from that there should be metric values defined
     *      for those metrics and given cluster.
     */
    @Test
    public void Given_ExistingClusterIdIsPassed_When_GetAllMetricValues_Then_ReturnsAllFoundMetricValues() throws Exception {
        MvcResult result = mockMvc.perform(get("/clusters/{clusterId}/metrics", existingClusterId))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        PageDto<MetricValueDto> pageDto = mapper.readValue(json, new TypeReference<>() {});

        assertNotNull(pageDto);
        assertNotNull(pageDto.page());
        assertNotNull(pageDto.items());

        List<MetricValueDto> foundMetricValues = pageDto.items();

        assertNotNull(foundMetricValues);
        assertFalse(foundMetricValues.isEmpty());
        assertEquals(foundMetricValues.size(), 2);

        MetricValueDto firstMetricValue = foundMetricValues.getFirst();
        assertNotNull(firstMetricValue);

        MetricValueDto secondMetricValue = foundMetricValues.getLast();
        assertNotNull(secondMetricValue);
    }

    /**
     * TEST REQUIREMENTS:
     *      This test does not require any data to be present in both oVirt and eduVirt DBs since its
     *      using automatically generated identifiers for testing how situation where cluster could
     *      not be found is handled.
     */
    @Test
    public void Given_NonExistentClusterIdIsPassed_When_GetAllMetricValues_Then_ReturnsAllFoundMetricValues() throws Exception {
        mockMvc.perform(get("/clusters/{clusterId}/metrics", nonExistentClusterId))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    /**
     * TEST REQUIREMENTS:
     *      This test requires sample cluster to exist in the oVirt database as well as sample metric to exist in the
     *      eduVirt database. Besides that, it is also required that given metric has value associated with sample cluster,
     *      so that it could be modified.
     */
    @Test
    public void Given_ExistingClusterAndMetricIdentifierArePassedAndMetricValueAssociatedWithGivenClusterExists_When_UpdateMetricValue_Then_UpdatesGivenMetricValueSuccessfully() throws Exception {
        ValueDto newValueDto = new ValueDto(100.0);

        MvcResult result = mockMvc.perform(patch("/clusters/{clusterId}/metrics/{metricId}", existingClusterId, existingMetricId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newValueDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        MetricValueDto valueDto = mapper.readValue(json, MetricValueDto.class);

        assertNotNull(valueDto);
        assertEquals(valueDto.value(), newValueDto.value());
    }

    /**
     * TEST REQUIREMENTS:
     *      This test does not require any sample data to exist in both oVirt and eduVirt DBs since it is using
     *      randomly generated identifiers in order to check systems response to cluster which could not be found.
     */
    @Test
    public void Given_NonExistentClusterIdentifierIsPassed_When_UpdateMetricValue_Then_UpdatesGivenMetricValueSuccessfully() throws Exception {
        ValueDto newValueDto = new ValueDto(100.0);

        mockMvc.perform(patch("/clusters/{clusterId}/metrics/{metricId}", nonExistentClusterId, nonExistentMetricId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newValueDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    /**
     * TEST REQUIREMENTS:
     *      This test does only require sample cluster to exist in the oVirt DB.
     */
    @Test
    public void Given_NonExistentMetricIdentifierIsPassed_When_UpdateMetricValue_Then_UpdatesGivenMetricValueSuccessfully() throws Exception {
        ValueDto newValueDto = new ValueDto(100.0);

        mockMvc.perform(patch("/clusters/{clusterId}/metrics/{metricId}", existingClusterId, nonExistentMetricId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newValueDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    /**
     * TEST REQUIREMENTS:
     *      This test does require sample cluster to exist in the oVirt DB as well as sample metric to be present
     *      in the eduVirt DB. Besides, no example value of given metric for given cluster can be present during that
     *      test.
     */
    @Test
    public void Given_GivenMetricDoesNotHaveValueAssociatedWithGivenCluster_When_UpdateMetricValue_Then_UpdatesGivenMetricValueSuccessfully() throws Exception {
        ValueDto newValueDto = new ValueDto(100.0);

        mockMvc.perform(patch("/clusters/{clusterId}/metrics/{metricId}", existingClusterId, existingClusterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newValueDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    /**
     * TEST REQUIREMENTS:
     *      That test requires sample cluster to exist in the oVirt DB. Also, it does require metric to be present
     *      in the eduVirt database and have a value associated with sample cluster.
     */
    @Test
    public void Given_ExistingClusterAndMetricIdentifiersArePassedAndMetricValueIsDefinedForGivenCluster_When_DeleteMetric_Then_RemovesMetricValueSuccessfully() throws Exception {
        mockMvc.perform(delete("/clusters/{clusterId}/metrics/{metricId}", existingClusterId, existingMetricId)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    /**
     * TEST REQUIREMENTS:
     *      That test does not require any data to exist in the oVirt / eduVirt databases, since it uses
     *      identifiers that are automatically generated.
     */
    @Test
    public void Given_NonExistentClusterIdentifierWasPassed_When_DeleteMetric_Then_Returns400BadRequest() throws Exception {
        mockMvc.perform(delete("/clusters/{clusterId}/metrics/{metricId}", nonExistentClusterId, nonExistentMetricId)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    /**
     * TEST REQUIREMENTS:
     *      This test requires only the sample cluster to exist in the oVirt database.
     */
    @Test
    public void Given_NonExistentMetricIdentifierWasPassed_When_DeleteMetric_Then_Returns400BadRequest() throws Exception {
        mockMvc.perform(delete("/clusters/{clusterId}/metrics/{metricId}", existingMetricId, nonExistentMetricId)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    /**
     * TEST REQUIREMENTS:
     *      This test require the sample cluster, as well as sample metric to exist in oVirt and eduVirt DBs
     *      respectively. Apart from those requirements, this method requires that there
     *      is no value for given metric and value.
     */
    @Test
    public void Given_GivenMetricDoesNotHaveValueAssociatedToGivenCluster_When_DeleteMetric_Then_Returns400BadRequest() throws Exception {
        mockMvc.perform(delete("/clusters/{clusterId}/metrics/{metricId}", existingMetricId, existingMetricId)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
