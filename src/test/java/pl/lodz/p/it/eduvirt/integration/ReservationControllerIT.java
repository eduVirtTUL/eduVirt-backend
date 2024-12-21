package pl.lodz.p.it.eduvirt.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import pl.lodz.p.it.eduvirt.dto.pagination.PageDto;
import pl.lodz.p.it.eduvirt.dto.reservation.CreateReservationDto;
import pl.lodz.p.it.eduvirt.dto.reservation.ReservationDetailsDto;
import pl.lodz.p.it.eduvirt.dto.reservation.ReservationDto;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ReservationControllerIT extends IntegrationTestBase {

    /* CreateNewReservation method tests */

    /**
     * TEST REQUIREMENTS:
     *      This method does require only the existence of resource group with existingResourceGroupId in the eduVirt
     *      database (which could have requirements to oVirt DB) and certain user (which will be performing this operation)
     *      and a certain team that they will belong to. Basically, this test would require entire structure of
     *      course - resource group pool - resource group and some teams with users in that course.
     */
    // @Test
    public void Given_ExistingResourceGroupIdentifierIsPassedAndAllTheDataMatchesConstraints_When_CreateNewReservation_Then_CreatesNewReservationSuccessfully() throws Exception {
        CreateReservationDto createDto = new CreateReservationDto(
                existingResourceGroupId,
                OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime().plusHours(12),
                OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime().plusHours(16),
                true
        );

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    /**
     * TEST REQUIREMENTS:
     *      That test does not require any more sample data, than the previous test, since it will be using automatically
     *      generated identifiers to check what would happen if the resource group could not be found.
     */
    // @Test
    public void Given_NonExistentResourceGroupIdentifierIsPassed_When_CreateNewReservation_Then_Returns400BadRequest() throws Exception {
        CreateReservationDto createDto = new CreateReservationDto(
                nonExistentResourceGroupId,
                OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime().plusHours(12),
                OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime().plusHours(16),
                true
        );

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    /**
     *
     * TEST REQUIREMENTS:
     *      That test does not require any more sample data, than the previous test, since it will be used to check
     *      what would happen if the start timestamp of the reservation would be from the past.
     */
    // @Test
    public void Given_ReservationStartIsInThePast_When_CreateNewReservation_Then_Returns409Conflict() throws Exception {
        CreateReservationDto createDto = new CreateReservationDto(
                existingResourceGroupId,
                OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime().minusHours(2),
                OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime().plusHours(2),
                true
        );

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    /**
     *
     * TEST REQUIREMENTS:
     *      That test does not require any more sample data, than the previous test, since it will be used to check
     *      what would happen if the end timestamp of the reservation would be before the start timestamp.
     */
    // @Test
    public void Given_ReservationEndsBeforeItStarts_When_CreateNewReservation_Then_Returns409Conflict() throws Exception {
        CreateReservationDto createDto = new CreateReservationDto(
                existingResourceGroupId,
                OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime().plusHours(8),
                OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime().plusHours(4),
                true
        );

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    /**
     *
     * TEST REQUIREMENTS:
     *      That test does not require any more sample data, than the previous test, since it will be used to check
     *      what would happen if the reservation would exceed the maximum reservation length
     *      (set in the resource pool I guess).
     */
    // @Test
    public void Given_MaximumReservationLengthWouldBeExceeded_When_CreateNewReservation_Then_Returns400BadRequest() throws Exception {
        CreateReservationDto createDto = new CreateReservationDto(
                existingResourceGroupId,
                OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime().plusHours(8),
                OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime().plusHours(16),
                true
        );

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    /**
     *
     * TEST REQUIREMENTS:
     *      That test does not require any more sample data, than the previous test, since it will be used to check what
     *      would happen if the reservation would exceed the maximum number of reservations of given resource group for
     *      given team (as it is obvious, also some reservations for that resource group would be required, depends
     *      on the sample data of the resource group pool).
     */
    // @Test
    public void Given_MaximumReservationNumberOfGivenResourceGroupWouldBeExceeded_When_CreateNewReservation_Then_Returns400BadRequest() throws Exception {
        CreateReservationDto createDto = new CreateReservationDto(
                existingResourceGroupId,
                OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime().plusHours(8),
                OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime().plusHours(12),
                true
        );

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    /**
     *
     * TEST REQUIREMENTS:
     *      That test does not require any more sample data, than the previous test, since it will be testing what
     *      would happen if the grace period of the last reservation of the resource group did not expire (so basically
     *      some reservation with correctly set grace period would be required in the eduVirt DB).
     */
    // @Test
    public void Given_GracePeriodSinceTheLastReservationDidNotExpire_When_CreateNewReservation_Then_Returns400BadRequest() throws Exception {
        CreateReservationDto createDto = new CreateReservationDto(
                existingResourceGroupId,
                OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime().plusHours(2),
                OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime().plusHours(4),
                true
        );

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    /**
     *
     * TEST REQUIREMENTS:
     *      That test would require some resource intensive resource group in order to check what would happen
     *      if the course resources are not sufficient to create new reservation inside it.
     */
    // @Test
    public void Given_CourseResourcesAreNotSufficientForGivenReservation_When_CreateNewReservation_Then_Returns400BadRequest() throws Exception {
        CreateReservationDto createDto = new CreateReservationDto(
                existingResourceGroupId,
                OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime().plusHours(2),
                OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime().plusHours(4),
                true
        );

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    /**
     *
     * TEST REQUIREMENTS:
     *      That test would require some resource intensive resource group in order to check what would happen
     *      if the cluster resources are not sufficient to create new reservation inside it. Quite possibly, it could
     *      require creating new course in the same cluster and try to exceed total resources of the cluster with new reservations
     *      in that course.
     */
    // @Test
    public void Given_ClusterResourcesAreNotSufficientForGivenReservation_When_CreateNewReservation_Then_Returns400BadRequest() throws Exception {
        CreateReservationDto createDto = new CreateReservationDto(
                existingResourceGroupId,
                OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime().plusHours(2),
                OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime().plusHours(4),
                true
        );

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    /**
     *
     * TEST REQUIREMENTS:
     *      That test would require some reservation that overlaps used time period in this test, in order to check
     *      what would happen if the reservation was to be created along that overlapping other reservation (of the same
     *      resource group that is).
     */
    // @Test
    public void Given_ReservationForGivenResourceGroupAlreadyOverlapsSelectedTimePeriod_When_CreateNewReservation_Then_Returns400BadRequest() throws Exception {
        CreateReservationDto createDto = new CreateReservationDto(
                existingResourceGroupId,
                OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime().plusHours(6),
                OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime().plusHours(10),
                true
        );

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    /* GetReservationDetails method tests */

    /**
     * TEST REQUIREMENTS:
     *      That test case require sample reservation to exist in the database, which reservation should have the
     *      same identifier as the existingReservationId, defined in the IntegrationTestBase.
     */
    // @Test
    public void Given_ExistingReservationIdentifierIsPassed_When_GetReservationDetails_Then_ReturnsFoundReservation() throws Exception {
        MvcResult result = mockMvc.perform(get("/reservation/{reservationId}", existingReservationId))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        ReservationDetailsDto foundReservation = mapper.readValue(json, ReservationDetailsDto.class);

        assertNotNull(foundReservation);
        assertNotNull(foundReservation.getResourceGroupId());
        assertNotNull(foundReservation.getTeamId());
        assertNotNull(foundReservation.getStart());
        assertNotNull(foundReservation.getEnd());
    }

    /**
     * TEST REQUIREMENTS:
     *      That test does not require any data to exist in both oVirt and eduVirt DBs, since it is using
     *      automatically generated identifiers to check what would happen if the reservation could not be found.
     */
    // @Test
    public void Given_NonExistentReservationIdentifierIsPassed_When_GetReservationDetails_Then_Returns404NotFound() throws Exception {
        mockMvc.perform(get("/reservation/{reservationId}", nonExistentReservationId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    /* GetReservationForGivenPeriod method tests */

    /**
     * TEST REQUIREMENTS:
     *      That test method requires some reservations for the team that the invoking user belongs to
     *      in the selected time period.
     */
    // @Test
    public void Given_SomeReservationsExistForSomeTeamInSelectedTimePeriod_When_GetReservationForGivenPeriod_Then_ReturnsFoundReservationsSuccessfully() throws Exception {
        MvcResult result = mockMvc.perform(get("/reservations/period", existingReservationId)
                        .param("start", OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime().plusHours(2).toString())
                        .param("end", OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime().plusHours(12).toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        List<ReservationDto> foundReservations = mapper.readValue(json, new TypeReference<>() {});

        assertNotNull(foundReservations);
        assertFalse(foundReservations.isEmpty());
        assertEquals(foundReservations.size(), 2);

        ReservationDto firstReservation = foundReservations.getFirst();
        assertNotNull(firstReservation);

        ReservationDto secondReservation = foundReservations.getLast();
        assertNotNull(secondReservation);
    }

    /**
     * TEST REQUIREMENTS:
     *      That test method requires some reservations for the team that the invoking user belongs to
     *      in the selected time period.
     */
    // @Test
    public void Given_NoReservationsExistForSomeTeamInSelectedTimePeriod_When_GetReservationForGivenPeriod_Then_ReturnsFoundReservationsSuccessfully() throws Exception {
        mockMvc.perform(get("/reservations/period", existingReservationId)
                        .param("start", OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime().minusHours(48).toString())
                        .param("end", OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime().minusHours(36).toString()))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    /* GetActiveReservations method tests */

    /**
     * TEST REQUIREMENTS:
     *      That test case require some active (that is currently in progress or starting in the future) reservations
     *      for team, which the user invoking those methods will belong to. As a result also resource group and
     *      course related data would be required.
     */
    // @Test
    public void Given_SomeActiveReservationsExistWithinTheDBForGivenTeam_When_GetActiveReservations_Then_ReturnsFoundReservationsSuccessfully() throws Exception {
        MvcResult result = mockMvc.perform(get("/reservations/active", existingReservationId))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        PageDto<ReservationDto> pageDto = mapper.readValue(json, new TypeReference<>() {});

        assertNotNull(pageDto);
        assertNotNull(pageDto.page());
        assertNotNull(pageDto.items());

        List<ReservationDto> foundReservations = pageDto.items();

        assertNotNull(foundReservations);
        assertFalse(foundReservations.isEmpty());
        assertEquals(foundReservations.size(), 2);

        ReservationDto firstReservation = foundReservations.getFirst();
        assertNotNull(firstReservation);

        ReservationDto secondReservation = foundReservations.getLast();
        assertNotNull(secondReservation);
    }

    /**
     * TEST REQUIREMENTS:
     *      That test case does not really require any data, since the result of empty reservation list can be achieved with
     *      correct pagination parameters.
     */
    // @Test
    public void Given_ActiveReservationsDoesNotExistWithinTheDBForGivenTeam_When_GetActiveReservations_Then_Returns204NoContent() throws Exception {
        mockMvc.perform(get("/reservations/active", existingReservationId)
                        .param("pageNumber", "100")
                        .param("pageSize", "1"))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    /* GetHistoricReservations method tests */

    /**
     * TEST REQUIREMENTS:
     *      That test case require some historic (that have already finished) reservations
     *      for team, which the user invoking those methods will belong to. As a result also resource group and
     *      course related data would be required.
     */
    // @Test
    public void Given_SomeHistoricReservationsExistWithinTheDBForGivenTeam_When_GetHistoricReservations_Then_ReturnsFoundReservationsSuccessfully() throws Exception {
        MvcResult result = mockMvc.perform(get("/reservations/historic", existingReservationId))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        PageDto<ReservationDto> pageDto = mapper.readValue(json, new TypeReference<>() {});

        assertNotNull(pageDto);
        assertNotNull(pageDto.page());
        assertNotNull(pageDto.items());

        List<ReservationDto> foundReservations = pageDto.items();

        assertNotNull(foundReservations);
        assertFalse(foundReservations.isEmpty());
        assertEquals(foundReservations.size(), 2);

        ReservationDto firstReservation = foundReservations.getFirst();
        assertNotNull(firstReservation);

        ReservationDto secondReservation = foundReservations.getLast();
        assertNotNull(secondReservation);
    }

    /**
     * TEST REQUIREMENTS:
     *      That test case does not really require any data, since the result of empty reservation list can be achieved with
     *      correct pagination parameters.
     */
    // @Test
    public void Given_SomeHistoricReservationsDoesNotExistWithinTheDBForGivenTeam_When_GetActiveReservations_Then_Returns204NoContent() throws Exception {
        mockMvc.perform(get("/reservations/historic", existingReservationId)
                        .param("pageNumber", "100")
                        .param("pageSize", "1"))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    /* CancelReservation method tests */

    /**
     * TEST REQUIREMENTS:
     *      This test require some reservation for the team, which the currently logged-in user belongs to, which starts
     *      in the future.
     */
    // @Test
    public void Given_ReservationDidNotYetStart_When_FinishReservation_Then_CancelsGivenReservationSuccessfully() throws Exception {
        mockMvc.perform(post("/reservations/{reservationId}/cancel", existingReservationId))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    /**
     * TEST REQUIREMENTS:
     *      As it is obvious, this test require some reservation that already begun for team, which the invoking user
     *      belongs to.
     */
    // @Test
    public void Given_ReservationStartedAlready_When_FinishReservation_Then_FinishesGivenReservationEarlierSuccessfully() throws Exception {
        MvcResult result = mockMvc.perform(get("/reservations/{reservationId}", existingReservationId))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        ReservationDetailsDto reservationDetails = mapper.readValue(json, ReservationDetailsDto.class);

        assertNotNull(reservationDetails);
        assertNotNull(reservationDetails.getStart());
        assertNotNull(reservationDetails.getEnd());

        LocalDateTime start = reservationDetails.getStart();
        LocalDateTime end = reservationDetails.getEnd();

        mockMvc.perform(post("/reservations/{reservationId}/cancel", existingReservationId))
                .andDo(print())
                .andExpect(status().isNoContent());

        result = mockMvc.perform(get("/reservations/{reservationId}", existingReservationId))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andReturn();

        String newJson = result.getResponse().getContentAsString();
        ReservationDetailsDto foundReservation = mapper.readValue(newJson, ReservationDetailsDto.class);

        assertNotNull(foundReservation);
        assertNotNull(foundReservation.getStart());
        assertNotNull(foundReservation.getEnd());
        assertEquals(foundReservation.getStart(), start);
        assertNotEquals(foundReservation.getEnd(), end);
    }

    /**
     * TEST REQUIREMENTS:
     *      That test does not require any more data than the previous tests, since it is using automatically generated
     *      identifier to check what would happen if the reservation to cancel could not be found.
     */
    // @Test
    public void Given_NonExistentReservationIdentifierIsPassed_When_FinishReservation_Then_Returns400BadRequest() throws Exception {
        mockMvc.perform(post("/reservations/{reservationId}/cancel", nonExistentReservationId))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
