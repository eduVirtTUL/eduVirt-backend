package pl.lodz.p.it.eduvirt.unit.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.lodz.p.it.eduvirt.aspect.exception.GeneralControllerExceptionResolver;
import pl.lodz.p.it.eduvirt.controller.ReservationController;
import pl.lodz.p.it.eduvirt.dto.reservation.CreateReservationDto;
import pl.lodz.p.it.eduvirt.entity.*;
import pl.lodz.p.it.eduvirt.mappers.ReservationMapper;
import pl.lodz.p.it.eduvirt.service.*;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Import({
        ReservationController.class,
        GeneralControllerExceptionResolver.class,
})
@WebMvcTest(controllers = {ReservationController.class}, useDefaultFilters = false)
public class ReservationControllerTest {

    /* MockMVC */

    @Autowired
    private MockMvc mockMvc;

    /* Services */

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private ResourceGroupService resourceGroupService;

    @MockitoBean
    private ResourceGroupPoolService resourceGroupPoolService;

    @MockitoBean
    private CourseService courseService;

    @MockitoBean
    private TeamService teamService;

    /* Mappers */

    @MockitoBean
    private ReservationMapper reservationMapper;

    /* Initialization */

    private Course course;

    /* Teams */

    private Team team1;
    private Team team2;

    private UUID userId1;
    private UUID userId2;
    private UUID userId3;
    private UUID userId4;

    private User user1;
    private User user2;
    private User user3;
    private User user4;

    /* Resource groups */

    private ResourceGroup resourceGroup1;
    private ResourceGroup resourceGroup2;
    private ResourceGroup resourceGroup3;
    private ResourceGroup resourceGroup4;

    /* Resource groups pools */

    private ResourceGroupPool resourceGroupPool1;
    private ResourceGroupPool resourceGroupPool2;

    /* PODS */

    private PodStateful podStateful1;
    private PodStateful podStateful2;

    private PodStateless podStateless1;
    private PodStateless podStateless2;

    /* Reservations */

    private CreateReservationDto createDto1;
    private CreateReservationDto createDto2;

    private Reservation reservation1;
    private Reservation reservation2;
    private Reservation reservation3;
    private Reservation reservation4;

    /* Maintenance intervals */

    private final UUID existingClusterId = UUID.randomUUID();
    private final UUID nonExistentClusterId = UUID.randomUUID();

    private MaintenanceInterval maintenanceInterval1;
    private MaintenanceInterval maintenanceInterval2;
    private MaintenanceInterval maintenanceInterval3;
    private MaintenanceInterval maintenanceInterval4;

    @BeforeEach
    public void setUp() throws Exception {
        Field id = AbstractEntity.class.getDeclaredField("id");
        Field version = Updatable.class.getDeclaredField("version");

        /* Maintenance intervals */

        maintenanceInterval1 = new MaintenanceInterval(
                "EXAMPLE_CAUSE_1",
                "EXAMPLE_DESCRIPTION_1",
                MaintenanceInterval.IntervalType.CLUSTER,
                existingClusterId,
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(2).toLocalDateTime(),
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(4).toLocalDateTime()
        );

        maintenanceInterval2 = new MaintenanceInterval(
                "EXAMPLE_CAUSE_2",
                "EXAMPLE_DESCRIPTION_2",
                MaintenanceInterval.IntervalType.SYSTEM,
                null,
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(6).toLocalDateTime(),
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(10).toLocalDateTime()
        );

        maintenanceInterval3 = new MaintenanceInterval(
                "EXAMPLE_CAUSE_3",
                "EXAMPLE_DESCRIPTION_3",
                MaintenanceInterval.IntervalType.CLUSTER,
                existingClusterId,
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(12).toLocalDateTime(),
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(16).toLocalDateTime()
        );

        maintenanceInterval4 = new MaintenanceInterval(
                "EXAMPLE_CAUSE_4",
                "EXAMPLE_DESCRIPTION_4",
                MaintenanceInterval.IntervalType.SYSTEM,
                null,
                OffsetDateTime.now(ZoneOffset.UTC).minusHours(2).toLocalDateTime(),
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(2).toLocalDateTime()
        );

        id.setAccessible(true);
        id.set(maintenanceInterval1, UUID.randomUUID());
        id.set(maintenanceInterval2, UUID.randomUUID());
        id.set(maintenanceInterval3, UUID.randomUUID());
        id.set(maintenanceInterval4, UUID.randomUUID());
        id.setAccessible(false);

        /* Teams */

        userId1 = UUID.randomUUID();
        userId2 = UUID.randomUUID();
        userId3 = UUID.randomUUID();
        userId4 = UUID.randomUUID();

        user1 = new User(userId1, "example1@example.com");
        user2 = new User(userId2, "example2@example.com");
        user3 = new User(userId3, "example3@example.com");
        user4 = new User(userId4, "example4@example.com");

        course = new Course();
        course.setName("Sieciowe System Baz Danych");
        course.setDescription("Network Database Systems");
        course.setClusterId(existingClusterId);

        id.setAccessible(true);
        id.set(course, UUID.randomUUID());
        id.setAccessible(false);

        List<User> listOfUsers1 = List.of(user1, user2);
        team1 = Team.builder()
                .name("Team001")
                .active(true)
                .maxSize(7)
                .course(course)
                .users(new ArrayList<>())
                .statefulPods(new LinkedList<>())
                .statelessPods(new LinkedList<>())
                .build();
        team1.getUsers().addAll(listOfUsers1.stream().map(User::getId).toList());

        List<User> listOfUsers2 = List.of(user3, user4);
        team2 = team1 = Team.builder()
                .name("Team002")
                .active(true)
                .maxSize(7)
                .course(course)
                .users(new ArrayList<>())
                .statefulPods(new LinkedList<>())
                .statelessPods(new LinkedList<>())
                .build();
        team2.getUsers().addAll(listOfUsers2.stream().map(User::getId).toList());

        id.setAccessible(true);
        id.set(team1, UUID.randomUUID());
        id.set(team2, UUID.randomUUID());
        id.setAccessible(false);

        course.setTeams(List.of(team1, team2));
        team1.setCourse(course);
        team2.setCourse(course);

        /* Resource groups */

        resourceGroup1 = new ResourceGroup();
        resourceGroup1.setName("Course-RG1");
        resourceGroup1.setDescription("First resource group for course.");
        resourceGroup1.setMaxRentTime(12);
        resourceGroup1.setStateless(false);

        resourceGroup1.getVms().addAll(List.of(
                VirtualMachine.builder().id(UUID.randomUUID()).build(),
                VirtualMachine.builder().id(UUID.randomUUID()).build(),
                VirtualMachine.builder().id(UUID.randomUUID()).build(),
                VirtualMachine.builder().id(UUID.randomUUID()).build(),
                VirtualMachine.builder().id(UUID.randomUUID()).build()
        ));

        resourceGroup2 = new ResourceGroup();
        resourceGroup2.setName("Course-RG2");
        resourceGroup2.setDescription("Second resource group for course.");
        resourceGroup2.setMaxRentTime(12);
        resourceGroup2.setStateless(false);

        resourceGroup2.getVms().addAll(List.of(
                VirtualMachine.builder().id(UUID.randomUUID()).build(),
                VirtualMachine.builder().id(UUID.randomUUID()).build(),
                VirtualMachine.builder().id(UUID.randomUUID()).build(),
                VirtualMachine.builder().id(UUID.randomUUID()).build(),
                VirtualMachine.builder().id(UUID.randomUUID()).build()
        ));

        resourceGroup3 = new ResourceGroup();
        resourceGroup3.setName("Course-RG3");
        resourceGroup3.setDescription("Third resource group for course.");
        resourceGroup3.setMaxRentTime(12);
        resourceGroup3.setStateless(false);

        resourceGroup3.getVms().addAll(List.of(
                VirtualMachine.builder().id(UUID.randomUUID()).build(),
                VirtualMachine.builder().id(UUID.randomUUID()).build(),
                VirtualMachine.builder().id(UUID.randomUUID()).build(),
                VirtualMachine.builder().id(UUID.randomUUID()).build(),
                VirtualMachine.builder().id(UUID.randomUUID()).build()
        ));

        resourceGroup4 = new ResourceGroup();
        resourceGroup4.setName("Course-RG4");
        resourceGroup4.setDescription("Forth resource group for course.");
        resourceGroup4.setMaxRentTime(12);
        resourceGroup4.setStateless(false);

        resourceGroup4.getVms().addAll(List.of(
                VirtualMachine.builder().id(UUID.randomUUID()).build(),
                VirtualMachine.builder().id(UUID.randomUUID()).build(),
                VirtualMachine.builder().id(UUID.randomUUID()).build(),
                VirtualMachine.builder().id(UUID.randomUUID()).build(),
                VirtualMachine.builder().id(UUID.randomUUID()).build()
        ));

        id.setAccessible(true);
        id.set(resourceGroup1, UUID.randomUUID());
        id.set(resourceGroup2, UUID.randomUUID());
        id.set(resourceGroup3, UUID.randomUUID());
        id.set(resourceGroup4, UUID.randomUUID());
        id.setAccessible(false);

        /* Resource group pools */

        resourceGroupPool1 = new ResourceGroupPool();
        resourceGroupPool1.setName("Course-RGPool1");
        resourceGroupPool1.setMaxRent(12);
        resourceGroupPool1.setGracePeriod(12);

        resourceGroupPool1.setResourceGroups(List.of(resourceGroup3));

        resourceGroupPool2 = new ResourceGroupPool();
        resourceGroupPool2.setName("Course-RGPool2");
        resourceGroupPool2.setMaxRent(12);
        resourceGroupPool2.setGracePeriod(12);

        resourceGroupPool2.setResourceGroups(List.of(resourceGroup4));

        id.setAccessible(true);
        id.set(resourceGroupPool1, UUID.randomUUID());
        id.set(resourceGroupPool2, UUID.randomUUID());
        id.setAccessible(false);

        /* PODS */

        podStateful1 = new PodStateful(resourceGroup1, team1, course);
        podStateful2 = new PodStateful(resourceGroup2, team2, course);

        podStateless1 = new PodStateless(resourceGroupPool1, team1, course);
        podStateless2 = new PodStateless(resourceGroupPool2, team2, course);

        id.setAccessible(true);
        id.set(podStateful1, UUID.randomUUID());
        id.set(podStateful2, UUID.randomUUID());
        id.set(podStateless1, UUID.randomUUID());
        id.set(podStateless2, UUID.randomUUID());
        id.setAccessible(false);

        team1.addStatefulPod(podStateful1);
        team2.addStatefulPod(podStateful2);

        team1.addStatelessPod(podStateless1);
        team2.addStatelessPod(podStateless2);

        /* Reservations */

        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        reservation1 = new Reservation(resourceGroup1, team1, currentTime, currentTime.plusHours(6), true, 10);
        reservation2 = new Reservation(resourceGroup2, team2, currentTime, currentTime.plusHours(6), true, 10);
        reservation3 = new Reservation(resourceGroup3, team1, currentTime, currentTime.plusHours(6), true, 10);
        reservation4 = new Reservation(resourceGroup4, team2, currentTime, currentTime.plusHours(6), true, 10);

        createDto1 = new CreateReservationDto(
                reservation1.getStartTime(),
                reservation1.getEndTime(),
                reservation1.getAutomaticStartup(),
                reservation1.getNotificationTime()
        );

        createDto2 = new CreateReservationDto(
                reservation2.getStartTime(),
                reservation2.getEndTime(),
                reservation2.getAutomaticStartup(),
                reservation2.getNotificationTime()
        );

        id.setAccessible(true);
        id.set(reservation1, UUID.randomUUID());
        id.set(reservation2, UUID.randomUUID());
        id.set(reservation3, UUID.randomUUID());
        id.set(reservation4, UUID.randomUUID());
        id.setAccessible(false);

        version.setAccessible(true);

        version.set(team1, 0L);
        version.set(team2, 0L);

        version.set(reservation1, 0L);
        version.set(reservation2, 0L);
        version.set(reservation3, 0L);
        version.set(reservation4, 0L);

        version.set(resourceGroup1, 0L);
        version.set(resourceGroup2, 0L);
        version.set(resourceGroup3, 0L);
        version.set(resourceGroup4, 0L);

        version.set(resourceGroupPool1, 0L);
        version.set(resourceGroupPool2, 0L);

        version.set(maintenanceInterval1, 0L);
        version.set(maintenanceInterval2, 0L);
        version.set(maintenanceInterval3, 0L);
        version.set(maintenanceInterval4, 0L);

        version.setAccessible(false);
    }

    /* Test */

    /* CreateNewReservationForPod method tests */

    @Test
    @WithMockUser
    public void Given_AllDataInCreateReservationDtoIsValidAndExistingCourseAndPodIdentifiersArePassed_When_CreateNewReservationForPod_Then_() throws Exception {

    }

    @Test
    @WithMockUser
    public void Given_NonExistentCourseIdentifierIsPassed_When_CreateNewReservationForPod_Then_Returns404NotFound() throws Exception {

    }

    @Test
    @WithMockUser
    public void Given_TeamCouldNotBeFoundForTheCurrentlyLoggedInUser_When_CreateNewReservationForPod_Then_Returns404NotFound() throws Exception {

    }

    @Test
    @WithMockUser
    public void Given_PodCouldNotBeFoundForGivenTeam_When_CreateNewReservationForPod_Then_Returns404NotFound() throws Exception {

    }

    @Test
    @WithMockUser
    public void Given_StatefulPodIdentifierIsPassedButItCouldNotBeFound_When_CreateNewReservationForPod_Then_Returns404NotFound() throws Exception {

    }

    @Test
    @WithMockUser
    public void Given_StatelessPodIdentifierIsPassedButItCouldNotBeFound_When_CreateNewReservationForPod_Then_Returns404NotFound() throws Exception {

    }

    /* GetReservationDetails method tests */

    @Test
    @WithMockUser
    public void Given_ExistingReservationIdentifierIsPassed_When_GetReservationDetails_Then_ReturnsFoundReservationsDetails() throws Exception {

    }

    @Test
    @WithMockUser
    public void Given_NonExistentReservationIdentifierIsPassed_When_GetReservationDetails_Then_Returns404NotFound() throws Exception {

    }

    /* GetPreviousReservations method tests */

    @Test
    @WithMockUser
    public void Given_ExistingCourseAndPodIdentifiersArePassed_When_GetPreviousReservations_Then_ReturnsListOfFoundReservationsSuccessfully() throws Exception {

    }

    @Test
    @WithMockUser
    public void Given_ExistingCourseAndPodIdentifiersArePassed_When_GetPreviousReservations_Then_ReturnsEmptyListOfReservations() throws Exception {

    }

    @Test
    @WithMockUser
    public void Given_NonExistentCourseIdentifierIsPassed_When_GetPreviousReservations_Then_Returns404NotFound() throws Exception {

    }

    @Test
    @WithMockUser
    public void Given_TeamCouldNotBeFoundForCurrentlyAuthenticatedUser_When_GetPreviousReservations_Then_Returns404NotFound() throws Exception {

    }

    @Test
    @WithMockUser
    public void Given_PodCouldNotBeFound_When_GetPreviousReservations_Then_Returns404NotFound() throws Exception {

    }

    @Test
    @WithMockUser
    public void Given_StatefulPodIdentifierIsPassedButItCouldNotBeFound_When_GetPreviousReservations_Then_Returns404NotFound() throws Exception {

    }

    @Test
    @WithMockUser
    public void Given_StatelessPodIdentifierIsPassedButItCouldNotBeFound_When_GetPreviousReservations_Then_Returns404NotFound() throws Exception {

    }

    /* GetRgReservationsInGivenCourse method tests */

    @Test
    @WithMockUser
    public void Given_ExistingCourseAndResourceGroupIdentifiersArePassedAndSomeReservationsExist_When_GetRgReservationsInGivenCourse_Then_ReturnsListOfFoundReservations() throws Exception {

    }

    @Test
    @WithMockUser
    public void Given_ExistingCourseAndResourceGroupIdentifiersArePassedAndNoReservationsExist_When_GetRgReservationsInGivenCourse_Then_ReturnsEmptyListOfReservations() throws Exception {

    }

    @Test
    @WithMockUser
    public void Given_NonExistentCourseIdentifierIsPassed_When_GetRgReservationsInGivenCourse_Then_Returns404NotFound() throws Exception {

    }

    @Test
    @WithMockUser
    public void Given_NonExistentResourceGroupIdentifierIsPassed_When_GetRgReservationsInGivenCourse_Then_Returns404NotFound() throws Exception {

    }

    /* GetRgPoolReservationsInGivenCourse method tests */

    @Test
    @WithMockUser
    public void Given_ExistingCourseAndResourceGroupPoolIdentifiersArePassedAndSomeReservationsExist_When_GetRgPoolReservationsInGivenCourse_Then_ReturnsListOfFoundReservations() throws Exception {

    }

    @Test
    @WithMockUser
    public void Given_ExistingCourseAndResourceGroupPoolIdentifiersArePassedAndNoReservationsExist_When_GetRgPoolReservationsInGivenCourse_Then_ReturnsEmptyListOfReservations() throws Exception {

    }

    @Test
    @WithMockUser
    public void Given_NonExistentCourseIdentifierIsPassedAndNoReservationsExist_When_GetRgPoolReservationsInGivenCourse_Then_Returns404NotFound() throws Exception {

    }

    @Test
    @WithMockUser
    public void Given_NonExistentResourceGroupPoolIdentifierIsPassedAndNoReservationsExist_When_GetRgPoolReservationsInGivenCourse_Then_Returns404NotFound() throws Exception {

    }

    /* GetActiveReservations method tests */

    @Test
    @WithMockUser
    public void Given_ExistingCourseIdentifierIsPassedAndTeamCouldBeFoundForCurrentUserAndSomeActiveReservationsExist_When_GetActiveReservations_Then_ReturnsListOfActiveReservations() throws Exception {

    }

    @Test
    @WithMockUser
    public void Given_ExistingCourseIdentifierIsPassedAndTeamCouldBeFoundForCurrentUserAndNoActiveReservationsExist_When_GetActiveReservations_Then_ReturnsEmptyListOfReservations() throws Exception {

    }

    @Test
    @WithMockUser
    public void Given_NonExistentCourseIdentifierIsPassed_When_GetActiveReservations_Then_Returns404NotFound() throws Exception {

    }

    @Test
    @WithMockUser
    public void Given_TeamCouldNotBeFoundForCurrentlyAuthenticatedUser_When_GetActiveReservations_Then_Returns404NotFound() throws Exception {

    }

    /* GetHistoricReservations method tests */

    @Test
    @WithMockUser
    public void Given_ExistingCourseIdentifierIsPassedAndTeamCouldBeFoundForCurrentUserAndSomeHistoricReservationsExist_When_GetHistoricReservations_Then_ReturnsListOfHistoricalReservations() throws Exception {

    }

    @Test
    @WithMockUser
    public void Given_ExistingCourseIdentifierIsPassedAndTeamCouldBeFoundForCurrentUserAndNoHistoricReservationsExist_When_GetHistoricReservations_Then_ReturnsEmptyListOfReservations() throws Exception {

    }

    @Test
    @WithMockUser
    public void Given_NonExistentCourseIdentifierIsPassed_When_GetHistoricReservations_Then_Returns404NotFound() throws Exception {

    }

    @Test
    @WithMockUser
    public void Given_TeamCouldNotBeFoundForCurrentlyAuthenticatedUser_When_GetHistoricReservations_Then_Returns404NotFound() throws Exception {

    }

    /* GetActiveReservationsForTeam method tests */

    @Test
    @WithMockUser
    public void Given_ExistingTeamIdentifierIsPassedAndSomeActiveReservationsExist_When_GetActiveReservationsForTeam_Then_ReturnsListOfActiveReservations() throws Exception {

    }

    @Test
    @WithMockUser
    public void Given_ExistingTeamIdentifierIsPassedAndNoActiveReservationsExist_When_GetActiveReservationsForTeam_Then_ReturnsEmptyListOfReservations() throws Exception {

    }

    @Test
    @WithMockUser
    public void Given_NonExistentTeamIdentifierIsPassed_When_GetActiveReservationsForTeam_Then_Returns404NotFound() throws Exception {

    }

    /* GetHistoricReservationsForTeam method tests */

    @Test
    @WithMockUser
    public void Given_ExistingTeamIdentifierIsPassedAndSomeHistoricReservationsExist_When_GetHistoricReservationsForTeam_Then_ReturnsListOfHistoricReservations() throws Exception {

    }

    @Test
    @WithMockUser
    public void Given_ExistingTeamIdentifierIsPassedAndNoHistoricReservationsExist_When_GetHistoricReservationsForTeam_Then_ReturnsEmptyListOfReservations() throws Exception {

    }

    @Test
    @WithMockUser
    public void Given_NonExistentTeamIdentifierIsPassed_When_GetHistoricReservationsForTeam_Then_Returns404NotFound() throws Exception {

    }

    /* FinishReservation method tests */

    @Test
    @WithMockUser
    public void Given_ExistingReservationIdentifierIsPassed_When_FinishReservation_Then_Returns204NoContent() throws Exception {

    }

    @Test
    @WithMockUser
    public void Given_NonExistentReservationIdentifierIsPassed_When_FinishReservation_Then_Returns404NotFound() throws Exception {

    }
}
