package pl.lodz.p.it.eduvirt.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import pl.lodz.p.it.eduvirt.dto.maintenance_interval.CreateMaintenanceIntervalDto;
import pl.lodz.p.it.eduvirt.dto.maintenance_interval.MaintenanceIntervalDetailsDto;
import pl.lodz.p.it.eduvirt.dto.maintenance_interval.MaintenanceIntervalDto;
import pl.lodz.p.it.eduvirt.dto.pagination.PageDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MaintenanceIntervalControllerIT extends IntegrationTestBase {

    /**
     * TEST REQUIREMENTS:
     *      This test requires some sample cluster to be present in the oVirt DB. Apart from that, no more
     *      sample data is required.
     */
    // @Test
    public void Given_MaintenanceIntervalBeginsInFutureAndNoOtherIntervalIsDefined_When_CreateNewClusterMaintenanceInterval_Then_CreatesNewIntervalSuccessfully() throws Exception {
        CreateMaintenanceIntervalDto createDto = new CreateMaintenanceIntervalDto(
                "EXAMPLE_CAUSE",
                "EXAMPLE_DESCRIPTION",
                LocalDateTime.now().plusHours(12),
                LocalDateTime.now().plusHours(18)
        );

        mockMvc.perform(post("/maintenance-intervals/cluster/{clusterId}", existingClusterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    /**
     * TEST REQUIREMENTS:
     *      This test does not really require any particular data to be present in both oVirt / eduVirt DBs since
     *      it is using randomly generated identifiers for cluster identifier, to check what will happen if the
     *      cluster could not be found.
     */
    // @Test
    public void Given_NonExistentClusterIdentifierIsPassed_When_CreateNewClusterMaintenanceInterval_Then_Returns400BadRequest() throws Exception {
        CreateMaintenanceIntervalDto createDto = new CreateMaintenanceIntervalDto(
                "EXAMPLE_CAUSE",
                "EXAMPLE_DESCRIPTION",
                LocalDateTime.now().plusHours(12),
                LocalDateTime.now().plusHours(18)
        );

        mockMvc.perform(post("/maintenance-intervals/cluster/{clusterId}", nonExistentClusterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    /**
     * TEST REQUIREMENTS:
     *      This test requires some sample cluster to be present in the oVirt DB. Apart from that, no more
     *      sample data is required.
     */
    // @Test
    public void Given_MaintenanceIntervalEndsBeforeItBegins_When_CreateNewClusterMaintenanceInterval_Then_Returns400BadRequest() throws Exception {
        CreateMaintenanceIntervalDto createDto = new CreateMaintenanceIntervalDto(
                "EXAMPLE_CAUSE",
                "EXAMPLE_DESCRIPTION",
                LocalDateTime.now().plusHours(14),
                LocalDateTime.now().plusHours(12)
        );

        mockMvc.perform(post("/maintenance-intervals/cluster/{clusterId}", existingClusterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    /**
     * TEST REQUIREMENTS:
     *      This test requires some sample cluster to be present in the oVirt DB. Apart from that, no more
     *      sample data is required.
     */
    // @Test
    public void Given_MaintenanceIntervalBeginsInThePast_When_CreateNewClusterMaintenanceInterval_Then_Returns400BadRequest() throws Exception {
        CreateMaintenanceIntervalDto createDto = new CreateMaintenanceIntervalDto(
                "EXAMPLE_CAUSE",
                "EXAMPLE_DESCRIPTION",
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().plusHours(4)
        );

        mockMvc.perform(post("/maintenance-intervals/cluster/{clusterId}", existingClusterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    /**
     * TEST REQUIREMENTS:
     *      This test requires some sample cluster to be present in the oVirt DB. Apart from that, no more
     *      sample data is required. Apart from that, this test case also require other maintenance interval
     *      to exist or rather overlap the time window which is used in the test.
     */
    // @Test
    public void Given_OtherMaintenanceIntervalExistsForClusterInTheOverlappingTimeWindow_When_CreateNewClusterMaintenanceInterval_Then_Returns409Conflict() throws Exception {
        CreateMaintenanceIntervalDto createDto = new CreateMaintenanceIntervalDto(
                "EXAMPLE_CAUSE",
                "EXAMPLE_DESCRIPTION",
                LocalDateTime.now().plusHours(12),
                LocalDateTime.now().plusHours(18)
        );

        mockMvc.perform(post("/maintenance-intervals/cluster/{clusterId}", existingClusterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    /**
     * TEST REQUIREMENTS:
     *      That method does not require any sample data is both oVirt / eduVirt DBs.
     */
    // @Test
    public void Given_MaintenanceIntervalBeginsInFuture_When_CreateNewSystemMaintenanceInterval_Then_CreatesNewIntervalSuccessfully() throws Exception {
        CreateMaintenanceIntervalDto createDto = new CreateMaintenanceIntervalDto(
                "EXAMPLE_CAUSE",
                "EXAMPLE_DESCRIPTION",
                LocalDateTime.now().plusHours(24),
                LocalDateTime.now().plusHours(28)
        );

        mockMvc.perform(post("/maintenance-intervals/system")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    /**
     * TEST REQUIREMENTS:
     *      That method does not require any sample data is both oVirt / eduVirt DBs.
     */
    // @Test
    public void Given_MaintenanceIntervalEndsBeforeItBegins_When_CreateNewSystemMaintenanceInterval_Then_Returns400BadRequest() throws Exception {
        CreateMaintenanceIntervalDto createDto = new CreateMaintenanceIntervalDto(
                "EXAMPLE_CAUSE",
                "EXAMPLE_DESCRIPTION",
                LocalDateTime.now().plusHours(28),
                LocalDateTime.now().plusHours(24)
        );

        mockMvc.perform(post("/maintenance-intervals/system")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    /**
     * TEST REQUIREMENTS:
     *      That method does not require any sample data is both oVirt / eduVirt DBs.
     */
    // @Test
    public void Given_MaintenanceIntervalBeginsInPast_When_CreateNewSystemMaintenanceInterval_Then_CreatesNewIntervalSuccessfully() throws Exception {
        CreateMaintenanceIntervalDto createDto = new CreateMaintenanceIntervalDto(
                "EXAMPLE_CAUSE",
                "EXAMPLE_DESCRIPTION",
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().plusHours(2)
        );

        mockMvc.perform(post("/maintenance-intervals/system")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    /**
     * TEST REQUIREMENTS:
     *      That method does not require any sample data is both oVirt / eduVirt DBs.
     */
    // @Test
    public void Given_OtherSystemMaintenanceIntervalIsDefinedInGivenTimePeriod_When_CreateNewSystemMaintenanceInterval_Then_Returns409Conflict() throws Exception {
        CreateMaintenanceIntervalDto createDto = new CreateMaintenanceIntervalDto(
                "EXAMPLE_CAUSE",
                "EXAMPLE_DESCRIPTION",
                LocalDateTime.now().plusHours(8),
                LocalDateTime.now().plusHours(12)
        );

        mockMvc.perform(post("/maintenance-intervals/system")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    /**
     * TEST REQUIREMENTS:
     *      This test requires some sample cluster to be present in the oVirt DB. Apart from that, some sample maintenance
     *      intervals, defined for that cluster and beginning in the future should also be present in the eduVirt
     *      DB (at least 2 of them should suffice).
     */
    // @Test
    public void Given_SomeMaintenanceIntervalsAreAlreadyDefinedAndAreDefinedForClusterAndActive_When_GetAllMaintenanceIntervals_Then_ReturnsAllFoundMaintenanceIntervals() throws Exception {
        MvcResult result = mockMvc.perform(get("/maintenance-intervals")
                        .param("clusterId", existingClusterId.toString())
                        .param("active", "true")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        PageDto<MaintenanceIntervalDto> pageDto = mapper.readValue(json, new TypeReference<>() {});

        assertNotNull(pageDto);
        assertNotNull(pageDto.page());
        assertNotNull(pageDto.items());

        List<MaintenanceIntervalDto> foundMaintenanceIntervals = pageDto.items();

        assertNotNull(foundMaintenanceIntervals);
        assertFalse(foundMaintenanceIntervals.isEmpty());
        assertEquals(foundMaintenanceIntervals.size(), 2);

        MaintenanceIntervalDto firstMaintenanceInterval = foundMaintenanceIntervals.getFirst();
        assertNotNull(firstMaintenanceInterval);

        MaintenanceIntervalDto secondMaintenanceInterval = foundMaintenanceIntervals.getLast();
        assertNotNull(secondMaintenanceInterval);
    }

    /**
     * TEST REQUIREMENTS:
     *      This test does not require any sample data present in both oVirt / eduVirt DBs, since its using only
     *      automatically generated identifiers, to check what would happen if the cluster could not be found.
     */
    // @Test
    public void Given_NonExistentClusterIdentifierIsPassed_When_GetAllMaintenanceIntervals_Then_ReturnsAllFoundMaintenanceIntervals() throws Exception {
        mockMvc.perform(get("/maintenance-intervals")
                        .param("clusterId", nonExistentClusterId.toString())
                        .param("active", "true")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    /**
     * TEST REQUIREMENTS:
     *      This test requires some sample cluster to be present in the oVirt DB. Apart from that, some sample maintenance
     *      intervals, defined for that cluster and beginning (and ending) in the past should also be present in the eduVirt
     *      DB (at least 2 of them should suffice).
     */
    // @Test
    public void Given_SomeMaintenanceIntervalsAreAlreadyDefinedAndAreDefinedForClusterAndInactive_When_GetAllMaintenanceIntervals_Then_ReturnsAllFoundMaintenanceIntervals() throws Exception {
        MvcResult result = mockMvc.perform(get("/maintenance-intervals")
                        .param("clusterId", existingClusterId.toString())
                        .param("active", "false")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        PageDto<MaintenanceIntervalDto> pageDto = mapper.readValue(json, new TypeReference<>() {});

        assertNotNull(pageDto);
        assertNotNull(pageDto.page());
        assertNotNull(pageDto.items());

        List<MaintenanceIntervalDto> foundMaintenanceIntervals = pageDto.items();

        assertNotNull(foundMaintenanceIntervals);
        assertFalse(foundMaintenanceIntervals.isEmpty());
        assertEquals(foundMaintenanceIntervals.size(), 2);

        MaintenanceIntervalDto firstMaintenanceInterval = foundMaintenanceIntervals.getFirst();
        assertNotNull(firstMaintenanceInterval);

        MaintenanceIntervalDto secondMaintenanceInterval = foundMaintenanceIntervals.getLast();
        assertNotNull(secondMaintenanceInterval);
    }

    /**
     * TEST REQUIREMENTS:
     *      This test requires some sample maintenance intervals, defined for that system and beginning in the future to
     *      be present in the eduVirt DB (at least 2 of them should suffice).
     */
    // @Test
    public void Given_SomeMaintenanceIntervalsAreAlreadyDefinedAndAreDefinedForSystemAndActive_When_GetAllMaintenanceIntervals_Then_ReturnsAllFoundMaintenanceIntervals() throws Exception {
        MvcResult result = mockMvc.perform(get("/maintenance-intervals")
                        .param("active", "true")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        PageDto<MaintenanceIntervalDto> pageDto = mapper.readValue(json, new TypeReference<>() {});

        assertNotNull(pageDto);
        assertNotNull(pageDto.page());
        assertNotNull(pageDto.items());

        List<MaintenanceIntervalDto> foundMaintenanceIntervals = pageDto.items();

        assertNotNull(foundMaintenanceIntervals);
        assertFalse(foundMaintenanceIntervals.isEmpty());
        assertEquals(foundMaintenanceIntervals.size(), 2);

        MaintenanceIntervalDto firstMaintenanceInterval = foundMaintenanceIntervals.getFirst();
        assertNotNull(firstMaintenanceInterval);

        MaintenanceIntervalDto secondMaintenanceInterval = foundMaintenanceIntervals.getLast();
        assertNotNull(secondMaintenanceInterval);
    }

    /**
     * TEST REQUIREMENTS:
     *      This test requires some sample maintenance intervals, defined for that system and beginning (and ending)
     *      in the past to be present in the eduVirt DB (at least 2 of them should suffice).
     */
    // @Test
    public void Given_SomeMaintenanceIntervalsAreAlreadyDefinedAndAreDefinedForSystemAndInactive_When_GetAllMaintenanceIntervals_Then_ReturnsAllFoundMaintenanceIntervals() throws Exception {
        MvcResult result = mockMvc.perform(get("/maintenance-intervals")
                        .param("active", "false")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        PageDto<MaintenanceIntervalDto> pageDto = mapper.readValue(json, new TypeReference<>() {});

        assertNotNull(pageDto);
        assertNotNull(pageDto.page());
        assertNotNull(pageDto.items());

        List<MaintenanceIntervalDto> foundMaintenanceIntervals = pageDto.items();

        assertNotNull(foundMaintenanceIntervals);
        assertFalse(foundMaintenanceIntervals.isEmpty());
        assertEquals(foundMaintenanceIntervals.size(), 2);

        MaintenanceIntervalDto firstMaintenanceInterval = foundMaintenanceIntervals.getFirst();
        assertNotNull(firstMaintenanceInterval);

        MaintenanceIntervalDto secondMaintenanceInterval = foundMaintenanceIntervals.getLast();
        assertNotNull(secondMaintenanceInterval);
    }

    /**
     * TEST REQUIREMENTS:
     *      This test method requires some maintenance intervals (could be defined for the system) to exist so that
     *      they overlap the time window which will begin 24 hours from now, and end in 72 hours from now.
     */
    // @Test
    public void Given_SomeMaintenanceIntervalsExistWithinTestedTimePeriod_When_GetMaintenanceIntervalsWithinTimePeriod_Then_ReturnsFoundMaintenanceIntervals() throws Exception {
        MvcResult result = mockMvc.perform(get("/maintenance-intervals/time-period")
                        .param("start", LocalDateTime.now().plusHours(24).toString())
                        .param("end", LocalDateTime.now().plusHours(72).toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        List<MaintenanceIntervalDto> foundMaintenanceIntervals = mapper.readValue(json, new TypeReference<>() {});

        assertNotNull(foundMaintenanceIntervals);
        assertFalse(foundMaintenanceIntervals.isEmpty());
        assertEquals(foundMaintenanceIntervals.size(), 2);

        MaintenanceIntervalDto firstMaintenanceInterval = foundMaintenanceIntervals.getFirst();
        assertNotNull(firstMaintenanceInterval);

        MaintenanceIntervalDto secondMaintenanceInterval = foundMaintenanceIntervals.getLast();
        assertNotNull(secondMaintenanceInterval);
    }

    /**
     * TEST REQUIREMENTS:
     *      That test does not require any sample data to be performed, since it tests what will happen if no
     *      maintenance intervals will be found.
     */
    // @Test
    public void Given_SomeMaintenanceIntervalsDoNotExistWithinTestedTimePeriod_When_GetMaintenanceIntervalsWithinTimePeriod_Then_ReturnsFoundMaintenanceIntervals() throws Exception {
        mockMvc.perform(get("/maintenance-intervals/time-period")
                        .param("start", LocalDateTime.now().minusHours(72).toString())
                        .param("end", LocalDateTime.now().minusHours(48).toString()))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andReturn();
    }

    /**
     * TEST REQUIREMENTS:
     *      That test case requires sample maintenance interval existence in the database, and apparently only that, since
     *      maintenance interval is only identifier by its id, even though it could be defined for cluster.
     */
    // @Test
    public void Given_ExistingMaintenanceIntervalIdentifierIsPassed_When_GetMaintenanceInterval_Then_CancelsGivenMaintenanceIntervalSuccessfully() throws Exception {
        MvcResult result = mockMvc.perform(get("/maintenance-intervals/{intervalId}", existingMaintenanceIntervalId))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        MaintenanceIntervalDetailsDto foundMaintenanceInterval = mapper.readValue(json, MaintenanceIntervalDetailsDto.class);

        assertNotNull(foundMaintenanceInterval);
        assertNotNull(foundMaintenanceInterval.id());
        assertNotNull(foundMaintenanceInterval.cause());
        assertNotNull(foundMaintenanceInterval.description());
        assertTrue(foundMaintenanceInterval.beginAt().isBefore(foundMaintenanceInterval.endAt()));
    }

    /**
     * TEST REQUIREMENTS:
     *      That test case does not require any specific data in oVirt / eduVirt DBs, since it is using automatically
     *      generated identifier to test what would happen if the maintenance interval could not be found.
     */
    // @Test
    public void Given_NonExistentMaintenanceIntervalIdentifierIsPassed_When_GetMaintenanceInterval_Then_CancelsGivenMaintenanceIntervalSuccessfully() throws Exception {
        mockMvc.perform(get("/maintenance-intervals/{intervalId}", nonExistentMaintenanceIntervalId))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    /**
     * TEST REQUIREMENTS:
     *      That test case requires sample maintenance interval existence in the database, and apparently only that, since
     *      maintenance interval is only identifier by its id, even though it could be defined for cluster.
     */
    // @Test
    public void Given_ExistingMaintenanceIntervalIdentifierIsPassedAndTheMaintenanceIntervalDidNotStart_When_FinishMaintenanceInterval_Then_RemovesGivenMaintenanceIntervalSuccessfully() throws Exception {
        mockMvc.perform(delete("/maintenance-intervals/{intervalId}", existingMaintenanceIntervalId))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    /**
     * TEST REQUIREMENTS:
     *      That test case does not require any specific data in oVirt / eduVirt DBs, since it is using automatically
     *      generated identifier to test what would happen if the maintenance interval could not be found.
     */
    // @Test
    public void Given_NonExistentMaintenanceIntervalIdentifierIsPassed_When_FinishMaintenanceInterval_Then_Returns400BadRequest() throws Exception {
        mockMvc.perform(delete("/maintenance-intervals/{intervalId}", nonExistentMaintenanceIntervalId))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    /**
     * TEST REQUIREMENTS:
     *      That test case requires sample maintenance interval existence in the database, and apparently only that, since
     *      maintenance interval is only identifier by its id, even though it could be defined for cluster. Important note is
     *      that this interval needs to start in the past in order for that test to function properly.
     */
    // @Test
    public void Given_ExistingMaintenanceIntervalIdentifierIsPassedAndTheMaintenanceIntervalAlreadyBegun_When_FinishMaintenanceInterval_Then_RemovesGivenMaintenanceIntervalSuccessfully() throws Exception {
        mockMvc.perform(delete("/maintenance-intervals/{intervalId}", existingMaintenanceIntervalId))
                .andDo(print())
                .andExpect(status().isNoContent());

        MvcResult result = mockMvc.perform(get("/maintenance-intervals/{intervalId}", existingMaintenanceIntervalId))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String originalJson = result.getResponse().getContentAsString();
        MaintenanceIntervalDetailsDto originalInterval = mapper.readValue(originalJson, MaintenanceIntervalDetailsDto.class);

        LocalDateTime beginAt = originalInterval.beginAt();
        LocalDateTime endAt = originalInterval.endAt();

        mockMvc.perform(delete("/maintenance-intervals/{intervalId}", existingMaintenanceIntervalId))
                .andDo(print())
                .andExpect(status().isNoContent());

        result = mockMvc.perform(get("/maintenance-intervals/{intervalId}", existingMaintenanceIntervalId))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String newJson = result.getResponse().getContentAsString();
        MaintenanceIntervalDetailsDto foundMaintenanceInterval = mapper.readValue(newJson, MaintenanceIntervalDetailsDto.class);

        assertNotNull(foundMaintenanceInterval);
        assertNotNull(foundMaintenanceInterval.beginAt());
        assertNotNull(foundMaintenanceInterval.endAt());
        assertEquals(foundMaintenanceInterval.beginAt(), beginAt);
        assertNotEquals(foundMaintenanceInterval.endAt(), endAt);
    }
}
