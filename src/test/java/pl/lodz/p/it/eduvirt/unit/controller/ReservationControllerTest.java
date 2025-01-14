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
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.lodz.p.it.eduvirt.aspect.exception.GeneralControllerExceptionResolver;
import pl.lodz.p.it.eduvirt.controller.ReservationController;
import pl.lodz.p.it.eduvirt.dto.pagination.PageDto;
import pl.lodz.p.it.eduvirt.dto.pagination.PageInfoDto;
import pl.lodz.p.it.eduvirt.dto.reservation.CreateReservationDto;
import pl.lodz.p.it.eduvirt.dto.reservation.ReservationDetailsDto;
import pl.lodz.p.it.eduvirt.dto.reservation.ReservationDto;
import pl.lodz.p.it.eduvirt.entity.*;
import pl.lodz.p.it.eduvirt.exceptions.CourseNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.ReservationNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.ResourceGroupNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.TeamNotFoundException;
import pl.lodz.p.it.eduvirt.mappers.*;
import pl.lodz.p.it.eduvirt.repository.UserRepository;
import pl.lodz.p.it.eduvirt.service.*;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import({
        ReservationController.class,
        GeneralControllerExceptionResolver.class,
        ReservationMapperImpl.class,
        TeamMapperImpl.class,
        ResourceGroupMapperImpl.class
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

    /* Repository */

    @MockitoBean
    private UserRepository userRepository;

    /* Mappers */

    @MockitoSpyBean
    private ReservationMapper reservationMapper;

    @MockitoSpyBean
    private TeamMapper teamMapper;

    @MockitoSpyBean
    private ResourceGroupMapper resourceGroupMapper;

    private final ObjectMapper mapper = new ObjectMapper();

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

    private UUID nonExistentUserId1 = UUID.fromString("608a99f5-7b74-427a-884e-4ccc21b14243");
    private UUID nonExistentUserId2 = UUID.fromString("57907da0-8187-4448-99a3-6db50aea33fd");

    private UUID adminId;
    private UUID teacherId1;
    private UUID teacherId2;
    private UUID studentId;

    private User admin;
    private User teacher1;
    private User teacher2;
    private User student;

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
        mapper.findAndRegisterModules();


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

        userId1 = UUID.fromString("5da2cdeb-38da-4a27-bf8d-6b33ae49726f");
        userId2 = UUID.fromString("506673f3-2282-4e31-88cf-6bfa5c49cbf2");
        userId3 = UUID.fromString("a1de3736-ea14-4e45-b856-338a4c1d67f9");
        userId4 = UUID.fromString("d4edf046-a2d5-400e-b2bc-12cf6dfaedfa");

        user1 = new User(userId1, UUID.randomUUID(), "example1@example.com", "UserName1", "FirstName1", "LastName1");
        user2 = new User(userId2, UUID.randomUUID(), "example2@example.com", "UserName2", "FirstName2", "LastName2");
        user3 = new User(userId3, UUID.randomUUID(), "example3@example.com", "UserName3", "FirstName3", "LastName3");
        user4 = new User(userId4, UUID.randomUUID(), "example4@example.com", "UserName4", "FirstName4", "LastName4");

        adminId = UUID.fromString("63c7b8c8-6a46-4784-9843-1096442dafd2");
        teacherId1 = UUID.fromString("e989c375-5eed-4ec6-b4b3-8ace83ed99fe");
        teacherId2 = UUID.fromString("f1ff980e-d8e8-497d-b9f8-b7cb72a27356");
        studentId = UUID.fromString("f758db9b-3227-4b40-b709-52ea13f814a4");

        admin = new User(adminId, UUID.randomUUID(), "admin@example.com", "Admin1", "FirstName", "LastName");
        teacher1 = new User(teacherId1, UUID.randomUUID(), "teacher1@example.com", "Teacher1", "FirstName", "LastName");
        teacher2 = new User(teacherId2, UUID.randomUUID(), "teacher2@example.com", "Teacher2", "FirstName", "LastName");
        student = new User(studentId, UUID.randomUUID(), "student@example.com", "Student", "FirstName", "LastName");

        course = new Course();
        course.setName("Sieciowe System Baz Danych");
        course.setDescription("Network Database Systems");
        course.setClusterId(existingClusterId);

        course.setTeachers(List.of(teacher1));

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
        team1.getUsers().addAll(listOfUsers1);

        List<User> listOfUsers2 = List.of(user3, user4);
        team2 = Team.builder()
                .name("Team002")
                .active(true)
                .maxSize(7)
                .course(course)
                .users(new ArrayList<>())
                .statefulPods(new LinkedList<>())
                .statelessPods(new LinkedList<>())
                .build();
        team2.getUsers().addAll(listOfUsers2);

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
    @WithMockUser(username = "5da2cdeb-38da-4a27-bf8d-6b33ae49726f", authorities = "student")
    public void Given_AllDataInCreateReservationDtoIsValidAndExistingCourseAndStatefulPodIdentifiersArePassed_When_CreateNewReservationForPod_Then_() throws Exception {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(6);

        CreateReservationDto createDto = new CreateReservationDto(
                start, end,
                reservation1.getAutomaticStartup(),
                reservation1.getNotificationTime()
        );

        when(courseService.getCourse(Mockito.eq(course.getId()))).thenReturn(course);
        when(teamService.getTeamByCourseAndUser(Mockito.eq(course), Mockito.eq(userId1))).thenReturn(team1);
        doNothing().when(reservationService).createReservationForStatefulPod(
                Mockito.eq(team1), Mockito.eq(podStateful1), Mockito.eq(createDto));

        mockMvc.perform(post("/reservations/course/{courseId}/pod/{podId}", course.getId(), podStateful1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto))
                        .secure(true)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(courseService, times(1)).getCourse(Mockito.eq(course.getId()));
        verify(teamService, times(1)).getTeamByCourseAndUser(Mockito.eq(course), Mockito.eq(userId1));
        verify(reservationService, times(1)).createReservationForStatefulPod(
                Mockito.eq(team1), Mockito.eq(podStateful1), Mockito.eq(createDto));
    }

    @Test
    @WithMockUser(username = "5da2cdeb-38da-4a27-bf8d-6b33ae49726f", authorities = "student")
    public void Given_AllDataInCreateReservationDtoIsValidAndExistingCourseAndStatelessPodIdentifiersArePassed_When_CreateNewReservationForPod_Then_() throws Exception {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(6);

        CreateReservationDto createDto = new CreateReservationDto(
                start, end,
                reservation1.getAutomaticStartup(),
                reservation1.getNotificationTime()
        );

        when(courseService.getCourse(Mockito.eq(course.getId()))).thenReturn(course);
        when(teamService.getTeamByCourseAndUser(Mockito.eq(course), Mockito.eq(userId1))).thenReturn(team1);
        doNothing().when(reservationService).createReservationForStatelessPod(
                Mockito.eq(team1), Mockito.eq(podStateless1), Mockito.eq(createDto));

        mockMvc.perform(post("/reservations/course/{courseId}/pod/{podId}", course.getId(), podStateless1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto))
                        .secure(true)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(courseService, times(1)).getCourse(Mockito.eq(course.getId()));
        verify(teamService, times(1)).getTeamByCourseAndUser(Mockito.eq(course), Mockito.eq(userId1));
        verify(reservationService, times(1)).createReservationForStatelessPod(
                Mockito.eq(team1), Mockito.eq(podStateless1), Mockito.eq(createDto));
    }

    @Test
    @WithMockUser(username = "5da2cdeb-38da-4a27-bf8d-6b33ae49726f", authorities = "student")
    public void Given_NonExistentCourseIdentifierIsPassed_When_CreateNewReservationForPod_Then_Returns404NotFound() throws Exception {
        UUID nonExistentCourseId = UUID.randomUUID();
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(6);

        CreateReservationDto createDto = new CreateReservationDto(
                start, end,
                reservation1.getAutomaticStartup(),
                reservation1.getNotificationTime()
        );

        when(courseService.getCourse(Mockito.eq(nonExistentCourseId)))
                .thenThrow(new CourseNotFoundException(nonExistentCourseId));

        mockMvc.perform(post("/reservations/course/{courseId}/pod/{podId}", nonExistentCourseId, podStateful1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto))
                        .secure(true)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(courseService, times(1)).getCourse(Mockito.eq(nonExistentCourseId));
    }

    @Test
    @WithMockUser(username = "608a99f5-7b74-427a-884e-4ccc21b14243", authorities = "student")
    public void Given_TeamCouldNotBeFoundForTheCurrentlyLoggedInUser_When_CreateNewReservationForPod_Then_Returns404NotFound() throws Exception {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(6);

        CreateReservationDto createDto = new CreateReservationDto(
                start, end,
                reservation1.getAutomaticStartup(),
                reservation1.getNotificationTime()
        );

        when(courseService.getCourse(Mockito.eq(course.getId()))).thenReturn(course);
        when(teamService.getTeamByCourseAndUser(Mockito.eq(course), Mockito.eq(nonExistentUserId1)))
                .thenThrow(TeamNotFoundException.class);

        mockMvc.perform(post("/reservations/course/{courseId}/pod/{podId}", course.getId(), podStateful1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto))
                        .secure(true)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(courseService, times(1)).getCourse(Mockito.eq(course.getId()));
        verify(teamService, times(1)).getTeamByCourseAndUser(Mockito.eq(course), Mockito.eq(nonExistentUserId1));
    }

    @Test
    @WithMockUser(username = "5da2cdeb-38da-4a27-bf8d-6b33ae49726f", authorities = "student")
    public void Given_NonExistentPodForGivenTeamIsPassed_When_CreateNewReservationForPod_Then_Returns404NotFound() throws Exception {
        UUID nonExistentPodId = UUID.randomUUID();
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(6);

        CreateReservationDto createDto = new CreateReservationDto(
                start, end,
                reservation1.getAutomaticStartup(),
                reservation1.getNotificationTime()
        );

        when(courseService.getCourse(Mockito.eq(course.getId()))).thenReturn(course);
        when(teamService.getTeamByCourseAndUser(Mockito.eq(course), Mockito.eq(userId1))).thenReturn(team1);

        mockMvc.perform(post("/reservations/course/{courseId}/pod/{podId}", course.getId(), nonExistentPodId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto))
                        .secure(true)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(courseService, times(1)).getCourse(Mockito.eq(course.getId()));
        verify(teamService, times(1)).getTeamByCourseAndUser(Mockito.eq(course), Mockito.eq(userId1));
    }

    /* GetReservationDetails method tests */

    @Test
    @WithMockUser(username = "5da2cdeb-38da-4a27-bf8d-6b33ae49726f", authorities = "student")
    public void Given_ExistingReservationIdentifierIsPassedAsStudentInCourse_When_GetReservationDetails_Then_ReturnsFoundReservationsDetails() throws Exception {
        when(reservationService.findReservationById(Mockito.eq(reservation1.getId())))
                .thenReturn(Optional.of(reservation1));

        when(userRepository.findById(Mockito.eq(userId1))).thenReturn(Optional.of(user1));

        MvcResult result = mockMvc.perform(get("/reservations/{reservationId}", reservation1.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        ReservationDetailsDto reservation = mapper.readValue(json, ReservationDetailsDto.class);

        assertNotNull(reservation);
        assertEquals(reservation1.getId(), reservation.getId());

        assertEquals(reservation1.getResourceGroup().getId(), resourceGroup1.getId());
        assertEquals(reservation1.getResourceGroup().getName(), resourceGroup1.getName());
        assertEquals(reservation1.getResourceGroup().getDescription(), resourceGroup1.getDescription());
        assertEquals(reservation1.getResourceGroup().isStateless(), resourceGroup1.isStateless());
        assertEquals(reservation1.getResourceGroup().getMaxRentTime(), resourceGroup1.getMaxRentTime());

        assertEquals(reservation1.getTeam().getId(), team1.getId());
        assertEquals(reservation1.getTeam().getName(), team1.getName());
        assertEquals(reservation1.getTeam().isActive(), team1.isActive());
        assertEquals(reservation1.getTeam().getMaxSize(), team1.getMaxSize());

        assertEquals(reservation1.getStartTime(), reservation.getStart());
        assertEquals(reservation1.getEndTime(), reservation.getEnd());
        assertEquals(reservation1.getAutomaticStartup(), reservation.isAutomaticStartup());

        verify(reservationService, times(1))
                .findReservationById(Mockito.eq(reservation1.getId()));
        verify(userRepository, times(1)).findById(Mockito.eq(userId1));
    }

    @Test
    @WithMockUser(username = "63c7b8c8-6a46-4784-9843-1096442dafd2", authorities = "administrator")
    public void Given_ExistingReservationIdentifierIsPassedAsAdmin_When_GetReservationDetails_Then_ReturnsFoundReservationsDetails() throws Exception {
        when(reservationService.findReservationById(Mockito.eq(reservation1.getId())))
                .thenReturn(Optional.of(reservation1));

        when(userRepository.findById(Mockito.eq(adminId))).thenReturn(Optional.of(admin));

        MvcResult result = mockMvc.perform(get("/reservations/{reservationId}", reservation1.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        ReservationDetailsDto reservation = mapper.readValue(json, ReservationDetailsDto.class);

        assertNotNull(reservation);
        assertEquals(reservation1.getId(), reservation.getId());

        assertEquals(reservation1.getResourceGroup().getId(), resourceGroup1.getId());
        assertEquals(reservation1.getResourceGroup().getName(), resourceGroup1.getName());
        assertEquals(reservation1.getResourceGroup().getDescription(), resourceGroup1.getDescription());
        assertEquals(reservation1.getResourceGroup().isStateless(), resourceGroup1.isStateless());
        assertEquals(reservation1.getResourceGroup().getMaxRentTime(), resourceGroup1.getMaxRentTime());

        assertEquals(reservation1.getTeam().getId(), team1.getId());
        assertEquals(reservation1.getTeam().getName(), team1.getName());
        assertEquals(reservation1.getTeam().isActive(), team1.isActive());
        assertEquals(reservation1.getTeam().getMaxSize(), team1.getMaxSize());

        assertEquals(reservation1.getStartTime(), reservation.getStart());
        assertEquals(reservation1.getEndTime(), reservation.getEnd());
        assertEquals(reservation1.getAutomaticStartup(), reservation.isAutomaticStartup());

        verify(reservationService, times(1))
                .findReservationById(Mockito.eq(reservation1.getId()));
        verify(userRepository, times(1)).findById(Mockito.eq(adminId));
    }

    @Test
    @WithMockUser(username = "e989c375-5eed-4ec6-b4b3-8ace83ed99fe", authorities = "teacher")
    public void Given_ExistingReservationIdentifierIsPassedAsTeacherInCourse_When_GetReservationDetails_Then_ReturnsFoundReservationsDetails() throws Exception {
        when(reservationService.findReservationById(Mockito.eq(reservation1.getId())))
                .thenReturn(Optional.of(reservation1));

        when(userRepository.findById(Mockito.eq(teacherId1))).thenReturn(Optional.of(teacher1));

        MvcResult result = mockMvc.perform(get("/reservations/{reservationId}", reservation1.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        ReservationDetailsDto reservation = mapper.readValue(json, ReservationDetailsDto.class);

        assertNotNull(reservation);
        assertEquals(reservation1.getId(), reservation.getId());

        assertEquals(reservation1.getResourceGroup().getId(), resourceGroup1.getId());
        assertEquals(reservation1.getResourceGroup().getName(), resourceGroup1.getName());
        assertEquals(reservation1.getResourceGroup().getDescription(), resourceGroup1.getDescription());
        assertEquals(reservation1.getResourceGroup().isStateless(), resourceGroup1.isStateless());
        assertEquals(reservation1.getResourceGroup().getMaxRentTime(), resourceGroup1.getMaxRentTime());

        assertEquals(reservation1.getTeam().getId(), team1.getId());
        assertEquals(reservation1.getTeam().getName(), team1.getName());
        assertEquals(reservation1.getTeam().isActive(), team1.isActive());
        assertEquals(reservation1.getTeam().getMaxSize(), team1.getMaxSize());

        assertEquals(reservation1.getStartTime(), reservation.getStart());
        assertEquals(reservation1.getEndTime(), reservation.getEnd());
        assertEquals(reservation1.getAutomaticStartup(), reservation.isAutomaticStartup());

        verify(reservationService, times(1))
                .findReservationById(Mockito.eq(reservation1.getId()));
        verify(userRepository, times(1)).findById(Mockito.eq(teacherId1));
    }

    @Test
    @WithMockUser(username = "f1ff980e-d8e8-497d-b9f8-b7cb72a27356", authorities = "teacher")
    public void Given_ExistingReservationIdentifierIsPassedAsTeacherNotInCourse_When_GetReservationDetails_Then_ReturnsFoundReservationsDetails() throws Exception {
        when(reservationService.findReservationById(Mockito.eq(reservation1.getId())))
                .thenReturn(Optional.of(reservation1));

        when(userRepository.findById(Mockito.eq(teacherId2))).thenReturn(Optional.of(teacher2));

        mockMvc.perform(get("/reservations/{reservationId}", reservation1.getId()))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(reservationService, times(1))
                .findReservationById(Mockito.eq(reservation1.getId()));
        verify(userRepository, times(1)).findById(Mockito.eq(teacherId2));
    }

    @Test
    @WithMockUser(username = "f758db9b-3227-4b40-b709-52ea13f814a4", authorities = "student")
    public void Given_ExistingUserIsAuthenticatedButNotInCourse_When_GetReservationDetails_Then_Returns404NotFound() throws Exception {
        when(reservationService.findReservationById(Mockito.eq(reservation1.getId()))).thenReturn(Optional.of(reservation1));
        when(userRepository.findById(Mockito.eq(studentId))).thenReturn(Optional.of(student));

        mockMvc.perform(get("/reservations/{reservationId}", reservation1.getId()))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(reservationService, times(1)).findReservationById(Mockito.eq(reservation1.getId()));
        verify(userRepository, times(1)).findById(Mockito.eq(studentId));
    }

    @Test
    @WithMockUser
    public void Given_NonExistentReservationIdentifierIsPassed_When_GetReservationDetails_Then_Returns404NotFound() throws Exception {
        UUID nonExistentReservationId = UUID.randomUUID();
        when(reservationService.findReservationById(Mockito.eq(reservation1.getId())))
                .thenThrow(new ReservationNotFoundException(nonExistentReservationId));

        mockMvc.perform(get("/reservations/{reservationId}", nonExistentReservationId))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(reservationService, times(1))
                .findReservationById(Mockito.eq(nonExistentReservationId));
    }

    @Test
    @WithMockUser(username = "608a99f5-7b74-427a-884e-4ccc21b14243", authorities = "student")
    public void Given_NonExistentUserIsAuthenticated_When_GetReservationDetails_Then_Returns404NotFound() throws Exception {
        when(reservationService.findReservationById(Mockito.eq(reservation1.getId()))).thenReturn(Optional.of(reservation1));
        when(userRepository.findById(Mockito.eq(nonExistentUserId1))).thenReturn(Optional.empty());

        mockMvc.perform(get("/reservations/{reservationId}", reservation1.getId()))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(reservationService, times(1)).findReservationById(Mockito.eq(reservation1.getId()));
        verify(userRepository, times(1)).findById(Mockito.eq(nonExistentUserId1));
    }

    /* GetPreviousReservations method tests */

    @Test
    @WithMockUser(username = "5da2cdeb-38da-4a27-bf8d-6b33ae49726f", authorities = "student")
    public void Given_ExistingCourseAndStatefulPodIdentifiersArePassed_When_GetPreviousReservations_Then_ReturnsListOfFoundReservationsSuccessfully() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        when(courseService.getCourse(Mockito.eq(course.getId()))).thenReturn(course);
        when(teamService.getTeamByCourseAndUser(Mockito.eq(course), Mockito.eq(userId1))).thenReturn(team1);

        when(reservationService.findReservationsForStatefulPod(
                Mockito.eq(podStateful1), Mockito.eq(team1), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(reservation1, reservation3), pageable, 2));

        MvcResult result = mockMvc.perform(get("/reservations/course/{courseId}/pods/{podId}/previous",
                        course.getId(), podStateful1.getId())
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        PageDto<ReservationDto> reservationPage = mapper.readValue(json, new TypeReference<>() {});

        assertNotNull(reservationPage);

        PageInfoDto pageInfo = reservationPage.page();
        assertNotNull(pageInfo);
        assertEquals(pageInfo.page(), 0);
        assertEquals(pageInfo.elements(), 2);
        assertEquals(pageInfo.totalPages(), 1);
        assertEquals(pageInfo.totalElements(), 2);

        List<ReservationDto> foundReservation = reservationPage.items();
        assertNotNull(foundReservation);
        assertFalse(foundReservation.isEmpty());
        assertEquals(2, foundReservation.size());

        ReservationDto firstReservation = foundReservation.getFirst();
        assertNotNull(firstReservation);
        assertEquals(reservation1.getId(), firstReservation.id());
        assertEquals(reservation1.getResourceGroup().getId(), UUID.fromString(firstReservation.resourceGroup().id()));
        assertEquals(reservation1.getResourceGroup().getName(), firstReservation.resourceGroup().name());
        assertEquals(reservation1.getResourceGroup().getDescription(), firstReservation.resourceGroup().description());
        assertEquals(reservation1.getResourceGroup().getMaxRentTime(), firstReservation.resourceGroup().maxRentTime());
        assertEquals(reservation1.getTeam().getId(), firstReservation.team().getId());
        assertEquals(reservation1.getTeam().getName(), firstReservation.team().getName());
        assertEquals(reservation1.getTeam().getMaxSize(), firstReservation.team().getMaxSize());
        assertEquals(reservation1.getStartTime(), firstReservation.start());
        assertEquals(reservation1.getEndTime(), firstReservation.end());

        ReservationDto secondReservation = foundReservation.getLast();
        assertNotNull(secondReservation);
        assertEquals(reservation3.getId(), secondReservation.id());
        assertEquals(reservation3.getResourceGroup().getId(), UUID.fromString(secondReservation.resourceGroup().id()));
        assertEquals(reservation3.getResourceGroup().getName(), secondReservation.resourceGroup().name());
        assertEquals(reservation3.getResourceGroup().getDescription(), secondReservation.resourceGroup().description());
        assertEquals(reservation3.getResourceGroup().getMaxRentTime(), secondReservation.resourceGroup().maxRentTime());
        assertEquals(reservation3.getTeam().getId(), secondReservation.team().getId());
        assertEquals(reservation3.getTeam().getName(), secondReservation.team().getName());
        assertEquals(reservation3.getTeam().getMaxSize(), secondReservation.team().getMaxSize());
        assertEquals(reservation3.getStartTime(), secondReservation.start());
        assertEquals(reservation3.getEndTime(), secondReservation.end());

        verify(courseService, times(1)).getCourse(Mockito.eq(course.getId()));
        verify(teamService, times(1)).getTeamByCourseAndUser(Mockito.eq(course), Mockito.eq(userId1));

        verify(reservationService, times(1))
                .findReservationsForStatefulPod(Mockito.eq(podStateful1), Mockito.eq(team1), Mockito.eq(pageable));
    }

    @Test
    @WithMockUser(username = "5da2cdeb-38da-4a27-bf8d-6b33ae49726f", authorities = "student")
    public void Given_ExistingCourseAndStatelessPodIdentifiersArePassed_When_GetPreviousReservations_Then_ReturnsListOfFoundReservationsSuccessfully() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        when(courseService.getCourse(Mockito.eq(course.getId()))).thenReturn(course);
        when(teamService.getTeamByCourseAndUser(Mockito.eq(course), Mockito.eq(userId1))).thenReturn(team1);

        when(reservationService.findReservationsForStatelessPod(
                Mockito.eq(podStateless1), Mockito.eq(team1), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(reservation2, reservation4), pageable, 2));

        MvcResult result = mockMvc.perform(get("/reservations/course/{courseId}/pods/{podId}/previous",
                        course.getId(), podStateless1.getId())
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        PageDto<ReservationDto> reservationPage = mapper.readValue(json, new TypeReference<>() {});

        assertNotNull(reservationPage);

        PageInfoDto pageInfo = reservationPage.page();
        assertNotNull(pageInfo);
        assertEquals(pageInfo.page(), 0);
        assertEquals(pageInfo.elements(), 2);
        assertEquals(pageInfo.totalPages(), 1);
        assertEquals(pageInfo.totalElements(), 2);

        List<ReservationDto> foundReservation = reservationPage.items();
        assertNotNull(foundReservation);
        assertFalse(foundReservation.isEmpty());
        assertEquals(2, foundReservation.size());

        ReservationDto firstReservation = foundReservation.getFirst();
        assertNotNull(firstReservation);
        assertEquals(reservation2.getId(), firstReservation.id());
        assertEquals(reservation2.getResourceGroup().getId(), UUID.fromString(firstReservation.resourceGroup().id()));
        assertEquals(reservation2.getResourceGroup().getName(), firstReservation.resourceGroup().name());
        assertEquals(reservation2.getResourceGroup().getDescription(), firstReservation.resourceGroup().description());
        assertEquals(reservation2.getResourceGroup().getMaxRentTime(), firstReservation.resourceGroup().maxRentTime());
        assertEquals(reservation2.getTeam().getId(), firstReservation.team().getId());
        assertEquals(reservation2.getTeam().getName(), firstReservation.team().getName());
        assertEquals(reservation2.getTeam().getMaxSize(), firstReservation.team().getMaxSize());
        assertEquals(reservation2.getStartTime(), firstReservation.start());
        assertEquals(reservation2.getEndTime(), firstReservation.end());

        ReservationDto secondReservation = foundReservation.getLast();
        assertNotNull(secondReservation);
        assertEquals(reservation4.getId(), secondReservation.id());
        assertEquals(reservation4.getResourceGroup().getId(), UUID.fromString(secondReservation.resourceGroup().id()));
        assertEquals(reservation4.getResourceGroup().getName(), secondReservation.resourceGroup().name());
        assertEquals(reservation4.getResourceGroup().getDescription(), secondReservation.resourceGroup().description());
        assertEquals(reservation4.getResourceGroup().getMaxRentTime(), secondReservation.resourceGroup().maxRentTime());
        assertEquals(reservation4.getTeam().getId(), secondReservation.team().getId());
        assertEquals(reservation4.getTeam().getName(), secondReservation.team().getName());
        assertEquals(reservation4.getTeam().getMaxSize(), secondReservation.team().getMaxSize());
        assertEquals(reservation4.getStartTime(), secondReservation.start());
        assertEquals(reservation4.getEndTime(), secondReservation.end());

        verify(courseService, times(1)).getCourse(Mockito.eq(course.getId()));
        verify(teamService, times(1)).getTeamByCourseAndUser(Mockito.eq(course), Mockito.eq(userId1));

        verify(reservationService, times(1))
                .findReservationsForStatelessPod(Mockito.eq(podStateless1), Mockito.eq(team1), Mockito.eq(pageable));
    }

    @Test
    @WithMockUser(username = "5da2cdeb-38da-4a27-bf8d-6b33ae49726f", authorities = "student")
    public void Given_NonPodIdentifiersIsPassed_When_GetPreviousReservations_Then_ReturnsEmptyListOfReservations() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;
        UUID nonExistentPodIdentifier = UUID.randomUUID();

        when(courseService.getCourse(Mockito.eq(course.getId()))).thenReturn(course);
        when(teamService.getTeamByCourseAndUser(Mockito.eq(course), Mockito.eq(userId1))).thenReturn(team1);

        mockMvc.perform(get("/reservations/course/{courseId}/pods/{podId}/previous",
                        course.getId(), nonExistentPodIdentifier)
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(courseService, times(1)).getCourse(Mockito.eq(course.getId()));
        verify(teamService, times(1)).getTeamByCourseAndUser(Mockito.eq(course), Mockito.eq(userId1));
    }

    @Test
    @WithMockUser(username = "5da2cdeb-38da-4a27-bf8d-6b33ae49726f", authorities = "student")
    public void Given_NonExistentCourseIdentifierIsPassed_When_GetPreviousReservations_Then_Returns404NotFound() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;
        UUID nonExistentCourseIdentifier = UUID.randomUUID();

        when(courseService.getCourse(Mockito.eq(nonExistentCourseIdentifier)))
                .thenThrow(CourseNotFoundException.class);

        mockMvc.perform(get("/reservations/course/{courseId}/pods/{podId}/previous",
                        nonExistentCourseIdentifier, podStateful1.getId())
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(courseService, times(1))
                .getCourse(Mockito.eq(nonExistentCourseIdentifier));
    }

    @Test
    @WithMockUser(username = "f758db9b-3227-4b40-b709-52ea13f814a4", authorities = "student")
    public void Given_TeamCouldNotBeFoundForCurrentlyAuthenticatedUser_When_GetPreviousReservations_Then_Returns404NotFound() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;

        when(courseService.getCourse(Mockito.eq(course.getId()))).thenReturn(course);
        when(teamService.getTeamByCourseAndUser(Mockito.eq(course), Mockito.eq(studentId)))
                .thenThrow(TeamNotFoundException.class);

        mockMvc.perform(get("/reservations/course/{courseId}/pods/{podId}/previous",
                        course.getId(), podStateful1.getId())
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(courseService, times(1)).getCourse(Mockito.eq(course.getId()));
        verify(teamService, times(1))
                .getTeamByCourseAndUser(Mockito.eq(course), Mockito.eq(studentId));
    }

    @Test
    @WithMockUser(username = "5da2cdeb-38da-4a27-bf8d-6b33ae49726f", authorities = "student")
    public void Given_EmptyListOfReservations_When_GetPreviousReservations_Then_Returns204NoContent() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        when(courseService.getCourse(Mockito.eq(course.getId()))).thenReturn(course);
        when(teamService.getTeamByCourseAndUser(Mockito.eq(course), Mockito.eq(userId1))).thenReturn(team1);

        when(reservationService.findReservationsForStatelessPod(
                Mockito.eq(podStateless1), Mockito.eq(team1), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        mockMvc.perform(get("/reservations/course/{courseId}/pods/{podId}/previous",
                        course.getId(), podStateless1.getId())
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(courseService, times(1)).getCourse(Mockito.eq(course.getId()));
        verify(teamService, times(1)).getTeamByCourseAndUser(Mockito.eq(course), Mockito.eq(userId1));

        verify(reservationService, times(1))
                .findReservationsForStatelessPod(Mockito.eq(podStateless1), Mockito.eq(team1), Mockito.eq(pageable));
    }

    /* GetRgReservationsInGivenCourse method tests */

    @Test
    @WithMockUser(username = "5da2cdeb-38da-4a27-bf8d-6b33ae49726f", authorities = "student")
    public void Given_ExistingCourseAndResourceGroupIdentifiersArePassedAndSomeReservationsExistAsUserInCourse_When_GetRgReservationsInGivenCourse_Then_ReturnsListOfFoundReservations() throws Exception {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(6);

        when(courseService.getCourse(Mockito.eq(course.getId()))).thenReturn(course);
        when(resourceGroupService.getResourceGroup(Mockito.eq(resourceGroup1.getId()))).thenReturn(resourceGroup1);
        when(reservationService.findRgReservations(Mockito.eq(resourceGroup1), Mockito.eq(course), Mockito.eq(start), Mockito.eq(end)))
                .thenReturn(List.of(reservation1, reservation3));

        when(userRepository.findById(userId1)).thenReturn(Optional.of(user1));

        MvcResult result = mockMvc.perform(get("/reservations/courses/{courseId}/resource-groups/{rgId}/period",
                        course.getId(), resourceGroup1.getId())
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        List<ReservationDto> foundReservations = mapper.readValue(json, new TypeReference<>() {});

        assertNotNull(foundReservations);
        assertFalse(foundReservations.isEmpty());
        assertEquals(2, foundReservations.size());

        ReservationDto firstReservation = foundReservations.getFirst();
        assertNotNull(firstReservation);
        assertEquals(reservation1.getId(), firstReservation.id());
        assertEquals(reservation1.getResourceGroup().getId(), UUID.fromString(firstReservation.resourceGroup().id()));
        assertEquals(reservation1.getResourceGroup().getName(), firstReservation.resourceGroup().name());
        assertEquals(reservation1.getResourceGroup().getDescription(), firstReservation.resourceGroup().description());
        assertEquals(reservation1.getResourceGroup().getMaxRentTime(), firstReservation.resourceGroup().maxRentTime());
        assertEquals(reservation1.getTeam().getId(), firstReservation.team().getId());
        assertEquals(reservation1.getTeam().getName(), firstReservation.team().getName());
        assertEquals(reservation1.getTeam().getMaxSize(), firstReservation.team().getMaxSize());
        assertEquals(reservation1.getStartTime(), firstReservation.start());
        assertEquals(reservation1.getEndTime(), firstReservation.end());

        ReservationDto secondReservation = foundReservations.getLast();
        assertNotNull(secondReservation);
        assertEquals(reservation3.getId(), secondReservation.id());
        assertEquals(reservation3.getResourceGroup().getId(), UUID.fromString(secondReservation.resourceGroup().id()));
        assertEquals(reservation3.getResourceGroup().getName(), secondReservation.resourceGroup().name());
        assertEquals(reservation3.getResourceGroup().getDescription(), secondReservation.resourceGroup().description());
        assertEquals(reservation3.getResourceGroup().getMaxRentTime(), secondReservation.resourceGroup().maxRentTime());
        assertEquals(reservation3.getTeam().getId(), secondReservation.team().getId());
        assertEquals(reservation3.getTeam().getName(), secondReservation.team().getName());
        assertEquals(reservation3.getTeam().getMaxSize(), secondReservation.team().getMaxSize());
        assertEquals(reservation3.getStartTime(), secondReservation.start());
        assertEquals(reservation3.getEndTime(), secondReservation.end());

        verify(courseService, times(1)).getCourse(Mockito.eq(course.getId()));
        verify(resourceGroupService, times(1)).getResourceGroup(Mockito.eq(resourceGroup1.getId()));
        verify(reservationService, times(1)).findRgReservations(Mockito.eq(resourceGroup1),
                Mockito.eq(course), Mockito.eq(start), Mockito.eq(end));

        verify(userRepository, times(1)).findById(Mockito.eq(userId1));
    }

    @Test
    @WithMockUser(username = "f758db9b-3227-4b40-b709-52ea13f814a4", authorities = "student")
    public void Given_ExistingCourseAndResourceGroupIdentifiersArePassedAndNoReservationsExist_When_GetRgReservationsInGivenCourse_Then_ReturnsEmptyListOfReservations() throws Exception {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(6);

        when(courseService.getCourse(Mockito.eq(course.getId()))).thenReturn(course);
        when(resourceGroupService.getResourceGroup(Mockito.eq(resourceGroup1.getId()))).thenReturn(resourceGroup1);
        when(reservationService.findRgReservations(Mockito.eq(resourceGroup1), Mockito.eq(course), Mockito.eq(start), Mockito.eq(end)))
                .thenReturn(List.of());

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));

        mockMvc.perform(get("/reservations/courses/{courseId}/resource-groups/{rgId}/period",
                        course.getId(), resourceGroup1.getId())
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(courseService, times(1)).getCourse(Mockito.eq(course.getId()));
        verify(resourceGroupService, times(1)).getResourceGroup(Mockito.eq(resourceGroup1.getId()));
        verify(reservationService, times(1)).findRgReservations(Mockito.eq(resourceGroup1),
                Mockito.eq(course), Mockito.eq(start), Mockito.eq(end));

        verify(userRepository, times(1)).findById(Mockito.eq(studentId));
    }

    @Test
    @WithMockUser(username = "5da2cdeb-38da-4a27-bf8d-6b33ae49726f", authorities = "student")
    public void Given_NonExistentCourseIdentifierIsPassed_When_GetRgReservationsInGivenCourse_Then_Returns404NotFound() throws Exception {
        UUID nonExistentCourseIdentifier = UUID.randomUUID();
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(6);

        when(courseService.getCourse(Mockito.eq(nonExistentCourseIdentifier)))
                .thenThrow(CourseNotFoundException.class);

        mockMvc.perform(get("/reservations/courses/{courseId}/resource-groups/{rgId}/period",
                        nonExistentCourseIdentifier, resourceGroup1.getId())
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(courseService, times(1)).getCourse(Mockito.eq(nonExistentCourseIdentifier));
    }

    @Test
    @WithMockUser(username = "5da2cdeb-38da-4a27-bf8d-6b33ae49726f", authorities = "student")
    public void Given_NonExistentResourceGroupIdentifierIsPassed_When_GetRgReservationsInGivenCourse_Then_Returns404NotFound() throws Exception {
        UUID nonExistentResourceGroupIdentifier = UUID.randomUUID();
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(6);

        when(courseService.getCourse(Mockito.eq(course.getId()))).thenReturn(course);
        when(resourceGroupService.getResourceGroup(Mockito.eq(nonExistentResourceGroupIdentifier)))
                .thenThrow(ResourceGroupNotFoundException.class);

        mockMvc.perform(get("/reservations/courses/{courseId}/resource-groups/{rgId}/period",
                        course.getId(), nonExistentResourceGroupIdentifier)
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(courseService, times(1)).getCourse(Mockito.eq(course.getId()));
        verify(resourceGroupService, times(1))
                .getResourceGroup(Mockito.eq(nonExistentResourceGroupIdentifier));
    }

    @Test
    @WithMockUser(username = "63c7b8c8-6a46-4784-9843-1096442dafd2", authorities = "administrator")
    public void Given_ExistingCourseAndResourceGroupIdentifiersArePassedAndSomeReservationsExistAsAdministrator_When_GetRgReservationsInGivenCourse_Then_ReturnsListOfFoundReservations() throws Exception {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(6);

        when(courseService.getCourse(Mockito.eq(course.getId()))).thenReturn(course);
        when(resourceGroupService.getResourceGroup(Mockito.eq(resourceGroup1.getId()))).thenReturn(resourceGroup1);
        when(reservationService.findRgReservations(Mockito.eq(resourceGroup1), Mockito.eq(course), Mockito.eq(start), Mockito.eq(end)))
                .thenReturn(List.of(reservation1, reservation3));

        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));

        MvcResult result = mockMvc.perform(get("/reservations/courses/{courseId}/resource-groups/{rgId}/period",
                        course.getId(), resourceGroup1.getId())
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        List<ReservationDto> foundReservations = mapper.readValue(json, new TypeReference<>() {});

        assertNotNull(foundReservations);
        assertFalse(foundReservations.isEmpty());
        assertEquals(2, foundReservations.size());

        ReservationDto firstReservation = foundReservations.getFirst();
        assertNotNull(firstReservation);
        assertEquals(reservation1.getId(), firstReservation.id());
        assertEquals(reservation1.getResourceGroup().getId(), UUID.fromString(firstReservation.resourceGroup().id()));
        assertEquals(reservation1.getResourceGroup().getName(), firstReservation.resourceGroup().name());
        assertEquals(reservation1.getResourceGroup().getDescription(), firstReservation.resourceGroup().description());
        assertEquals(reservation1.getResourceGroup().getMaxRentTime(), firstReservation.resourceGroup().maxRentTime());
        assertEquals(reservation1.getTeam().getId(), firstReservation.team().getId());
        assertEquals(reservation1.getTeam().getName(), firstReservation.team().getName());
        assertEquals(reservation1.getTeam().getMaxSize(), firstReservation.team().getMaxSize());
        assertEquals(reservation1.getStartTime(), firstReservation.start());
        assertEquals(reservation1.getEndTime(), firstReservation.end());

        ReservationDto secondReservation = foundReservations.getLast();
        assertNotNull(secondReservation);
        assertEquals(reservation3.getId(), secondReservation.id());
        assertEquals(reservation3.getResourceGroup().getId(), UUID.fromString(secondReservation.resourceGroup().id()));
        assertEquals(reservation3.getResourceGroup().getName(), secondReservation.resourceGroup().name());
        assertEquals(reservation3.getResourceGroup().getDescription(), secondReservation.resourceGroup().description());
        assertEquals(reservation3.getResourceGroup().getMaxRentTime(), secondReservation.resourceGroup().maxRentTime());
        assertEquals(reservation3.getTeam().getId(), secondReservation.team().getId());
        assertEquals(reservation3.getTeam().getName(), secondReservation.team().getName());
        assertEquals(reservation3.getTeam().getMaxSize(), secondReservation.team().getMaxSize());
        assertEquals(reservation3.getStartTime(), secondReservation.start());
        assertEquals(reservation3.getEndTime(), secondReservation.end());

        verify(courseService, times(1)).getCourse(Mockito.eq(course.getId()));
        verify(resourceGroupService, times(1)).getResourceGroup(Mockito.eq(resourceGroup1.getId()));
        verify(reservationService, times(1)).findRgReservations(Mockito.eq(resourceGroup1),
                Mockito.eq(course), Mockito.eq(start), Mockito.eq(end));

        verify(userRepository, times(1)).findById(Mockito.eq(adminId));
    }

    @Test
    @WithMockUser(username = "e989c375-5eed-4ec6-b4b3-8ace83ed99fe", authorities = "teacher")
    public void Given_ExistingCourseAndResourceGroupIdentifiersArePassedAndSomeReservationsExistAsTeacherInCourse_When_GetRgReservationsInGivenCourse_Then_ReturnsListOfFoundReservations() throws Exception {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(6);

        when(courseService.getCourse(Mockito.eq(course.getId()))).thenReturn(course);
        when(resourceGroupService.getResourceGroup(Mockito.eq(resourceGroup1.getId()))).thenReturn(resourceGroup1);
        when(reservationService.findRgReservations(Mockito.eq(resourceGroup1), Mockito.eq(course), Mockito.eq(start), Mockito.eq(end)))
                .thenReturn(List.of(reservation1, reservation3));

        when(userRepository.findById(teacherId1)).thenReturn(Optional.of(teacher1));

        MvcResult result = mockMvc.perform(get("/reservations/courses/{courseId}/resource-groups/{rgId}/period",
                        course.getId(), resourceGroup1.getId())
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        List<ReservationDto> foundReservations = mapper.readValue(json, new TypeReference<>() {});

        assertNotNull(foundReservations);
        assertFalse(foundReservations.isEmpty());
        assertEquals(2, foundReservations.size());

        ReservationDto firstReservation = foundReservations.getFirst();
        assertNotNull(firstReservation);
        assertEquals(reservation1.getId(), firstReservation.id());
        assertEquals(reservation1.getResourceGroup().getId(), UUID.fromString(firstReservation.resourceGroup().id()));
        assertEquals(reservation1.getResourceGroup().getName(), firstReservation.resourceGroup().name());
        assertEquals(reservation1.getResourceGroup().getDescription(), firstReservation.resourceGroup().description());
        assertEquals(reservation1.getResourceGroup().getMaxRentTime(), firstReservation.resourceGroup().maxRentTime());
        assertEquals(reservation1.getTeam().getId(), firstReservation.team().getId());
        assertEquals(reservation1.getTeam().getName(), firstReservation.team().getName());
        assertEquals(reservation1.getTeam().getMaxSize(), firstReservation.team().getMaxSize());
        assertEquals(reservation1.getStartTime(), firstReservation.start());
        assertEquals(reservation1.getEndTime(), firstReservation.end());

        ReservationDto secondReservation = foundReservations.getLast();
        assertNotNull(secondReservation);
        assertEquals(reservation3.getId(), secondReservation.id());
        assertEquals(reservation3.getResourceGroup().getId(), UUID.fromString(secondReservation.resourceGroup().id()));
        assertEquals(reservation3.getResourceGroup().getName(), secondReservation.resourceGroup().name());
        assertEquals(reservation3.getResourceGroup().getDescription(), secondReservation.resourceGroup().description());
        assertEquals(reservation3.getResourceGroup().getMaxRentTime(), secondReservation.resourceGroup().maxRentTime());
        assertEquals(reservation3.getTeam().getId(), secondReservation.team().getId());
        assertEquals(reservation3.getTeam().getName(), secondReservation.team().getName());
        assertEquals(reservation3.getTeam().getMaxSize(), secondReservation.team().getMaxSize());
        assertEquals(reservation3.getStartTime(), secondReservation.start());
        assertEquals(reservation3.getEndTime(), secondReservation.end());

        verify(courseService, times(1)).getCourse(Mockito.eq(course.getId()));
        verify(resourceGroupService, times(1)).getResourceGroup(Mockito.eq(resourceGroup1.getId()));
        verify(reservationService, times(1)).findRgReservations(Mockito.eq(resourceGroup1),
                Mockito.eq(course), Mockito.eq(start), Mockito.eq(end));

        verify(userRepository, times(1)).findById(Mockito.eq(teacherId1));
    }

    @Test
    @WithMockUser(username = "f1ff980e-d8e8-497d-b9f8-b7cb72a27356", authorities = "teacher")
    public void Given_ExistingCourseAndResourceGroupIdentifiersArePassedAndSomeReservationsExistAsTeacherNotInCourse_When_GetRgReservationsInGivenCourse_Then_ReturnsEmptyListReservations() throws Exception {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(6);

        when(courseService.getCourse(Mockito.eq(course.getId()))).thenReturn(course);
        when(resourceGroupService.getResourceGroup(Mockito.eq(resourceGroup1.getId()))).thenReturn(resourceGroup1);
        when(reservationService.findRgReservations(Mockito.eq(resourceGroup1), Mockito.eq(course), Mockito.eq(start), Mockito.eq(end)))
                .thenReturn(List.of(reservation1, reservation3));

        when(userRepository.findById(teacherId2)).thenReturn(Optional.of(teacher2));

        mockMvc.perform(get("/reservations/courses/{courseId}/resource-groups/{rgId}/period",
                        course.getId(), resourceGroup1.getId())
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(courseService, times(1)).getCourse(Mockito.eq(course.getId()));
        verify(resourceGroupService, times(1)).getResourceGroup(Mockito.eq(resourceGroup1.getId()));
        verify(reservationService, times(1)).findRgReservations(Mockito.eq(resourceGroup1),
                Mockito.eq(course), Mockito.eq(start), Mockito.eq(end));

        verify(userRepository, times(1)).findById(Mockito.eq(teacherId2));
    }

    @Test
    @WithMockUser(username = "f758db9b-3227-4b40-b709-52ea13f814a4", authorities = "student")
    public void Given_ExistingCourseAndResourceGroupIdentifiersArePassedAndSomeReservationsExistAsStudentNotInCourse_When_GetRgReservationsInGivenCourse_Then_ReturnsEmptyListReservations() throws Exception {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(6);

        when(courseService.getCourse(Mockito.eq(course.getId()))).thenReturn(course);
        when(resourceGroupService.getResourceGroup(Mockito.eq(resourceGroup1.getId()))).thenReturn(resourceGroup1);
        when(reservationService.findRgReservations(Mockito.eq(resourceGroup1), Mockito.eq(course), Mockito.eq(start), Mockito.eq(end)))
                .thenReturn(List.of(reservation1, reservation3));

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));

        mockMvc.perform(get("/reservations/courses/{courseId}/resource-groups/{rgId}/period",
                        course.getId(), resourceGroup1.getId())
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(courseService, times(1)).getCourse(Mockito.eq(course.getId()));
        verify(resourceGroupService, times(1)).getResourceGroup(Mockito.eq(resourceGroup1.getId()));
        verify(reservationService, times(1)).findRgReservations(Mockito.eq(resourceGroup1),
                Mockito.eq(course), Mockito.eq(start), Mockito.eq(end));

        verify(userRepository, times(1)).findById(Mockito.eq(studentId));
    }

    @Test
    @WithMockUser(username = "5da2cdeb-38da-4a27-bf8d-6b33ae49726f", authorities = "student")
    public void Given_ExistingCourseAndResourceGroupIdentifiersArePassedAndNoReservationsExistAsStudentInCourse_When_GetRgReservationsInGivenCourse_Then_ReturnsEmptyListReservations() throws Exception {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(6);

        when(courseService.getCourse(Mockito.eq(course.getId()))).thenReturn(course);
        when(resourceGroupService.getResourceGroup(Mockito.eq(resourceGroup1.getId()))).thenReturn(resourceGroup1);
        when(reservationService.findRgReservations(Mockito.eq(resourceGroup1), Mockito.eq(course), Mockito.eq(start), Mockito.eq(end)))
                .thenReturn(List.of());

        when(userRepository.findById(userId1)).thenReturn(Optional.of(user1));

        mockMvc.perform(get("/reservations/courses/{courseId}/resource-groups/{rgId}/period",
                        course.getId(), resourceGroup1.getId())
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(courseService, times(1)).getCourse(Mockito.eq(course.getId()));
        verify(resourceGroupService, times(1)).getResourceGroup(Mockito.eq(resourceGroup1.getId()));
        verify(reservationService, times(1)).findRgReservations(Mockito.eq(resourceGroup1),
                Mockito.eq(course), Mockito.eq(start), Mockito.eq(end));

        verify(userRepository, times(1)).findById(Mockito.eq(userId1));
    }

    @Test
    @WithMockUser(username = "f758db9b-3227-4b40-b709-52ea13f814a4", authorities = "student")
    public void Given_UserCouldNotFound_When_GetRgReservationsInGivenCourse_Then_Returns404NotFound() throws Exception {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(6);

        when(courseService.getCourse(Mockito.eq(course.getId()))).thenReturn(course);
        when(resourceGroupService.getResourceGroup(Mockito.eq(resourceGroup1.getId()))).thenReturn(resourceGroup1);
        when(reservationService.findRgReservations(Mockito.eq(resourceGroup1), Mockito.eq(course), Mockito.eq(start), Mockito.eq(end)))
                .thenReturn(List.of(reservation1, reservation3));

        when(userRepository.findById(studentId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/reservations/courses/{courseId}/resource-groups/{rgId}/period",
                        course.getId(), resourceGroup1.getId())
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(courseService, times(1)).getCourse(Mockito.eq(course.getId()));
        verify(resourceGroupService, times(1)).getResourceGroup(Mockito.eq(resourceGroup1.getId()));
        verify(reservationService, times(1)).findRgReservations(Mockito.eq(resourceGroup1),
                Mockito.eq(course), Mockito.eq(start), Mockito.eq(end));

        verify(userRepository, times(1)).findById(Mockito.eq(studentId));
    }

    /* GetRgPoolReservationsInGivenCourse method tests */

    @Test
    @WithMockUser(username = "5da2cdeb-38da-4a27-bf8d-6b33ae49726f", authorities = "student")
    public void Given_ExistingCourseAndResourceGroupPoolIdentifiersArePassedAndSomeReservationsExist_When_GetRgPoolReservationsInGivenCourse_Then_ReturnsListOfFoundReservations() throws Exception {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(6);

        when(courseService.getCourse(Mockito.eq(course.getId()))).thenReturn(course);
        when(resourceGroupPoolService.getResourceGroupPool(Mockito.eq(resourceGroupPool1.getId()))).thenReturn(resourceGroupPool1);
        when(reservationService.findRgPoolReservations(
                Mockito.eq(resourceGroupPool1), Mockito.eq(course), Mockito.eq(start), Mockito.eq(end)))
                .thenReturn(List.of(reservation2, reservation4));

        when(userRepository.findById(Mockito.eq(userId1))).thenReturn(Optional.of(user1));

        MvcResult result = mockMvc.perform(get("/reservations/courses/{courseId}/resource-group-pools/{rgPoolId}/period",
                        course.getId(), resourceGroupPool1.getId())
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        List<ReservationDto> foundReservations = mapper.readValue(json, new TypeReference<>() {});

        assertNotNull(foundReservations);
        assertFalse(foundReservations.isEmpty());
        assertEquals(2, foundReservations.size());

        ReservationDto firstReservation = foundReservations.getFirst();
        assertNotNull(firstReservation);
        assertEquals(reservation2.getId(), firstReservation.id());
        assertEquals(reservation2.getResourceGroup().getId(), UUID.fromString(firstReservation.resourceGroup().id()));
        assertEquals(reservation2.getResourceGroup().getName(), firstReservation.resourceGroup().name());
        assertEquals(reservation2.getResourceGroup().getDescription(), firstReservation.resourceGroup().description());
        assertEquals(reservation2.getResourceGroup().getMaxRentTime(), firstReservation.resourceGroup().maxRentTime());
        assertEquals(reservation2.getTeam().getId(), firstReservation.team().getId());
        assertEquals(reservation2.getTeam().getName(), firstReservation.team().getName());
        assertEquals(reservation2.getTeam().getMaxSize(), firstReservation.team().getMaxSize());
        assertEquals(reservation2.getStartTime(), firstReservation.start());
        assertEquals(reservation2.getEndTime(), firstReservation.end());

        ReservationDto secondReservation = foundReservations.getLast();
        assertNotNull(secondReservation);
        assertEquals(reservation4.getId(), secondReservation.id());
        assertEquals(reservation4.getResourceGroup().getId(), UUID.fromString(secondReservation.resourceGroup().id()));
        assertEquals(reservation4.getResourceGroup().getName(), secondReservation.resourceGroup().name());
        assertEquals(reservation4.getResourceGroup().getDescription(), secondReservation.resourceGroup().description());
        assertEquals(reservation4.getResourceGroup().getMaxRentTime(), secondReservation.resourceGroup().maxRentTime());
        assertEquals(reservation4.getTeam().getId(), secondReservation.team().getId());
        assertEquals(reservation4.getTeam().getName(), secondReservation.team().getName());
        assertEquals(reservation4.getTeam().getMaxSize(), secondReservation.team().getMaxSize());
        assertEquals(reservation4.getStartTime(), secondReservation.start());
        assertEquals(reservation4.getEndTime(), secondReservation.end());

        verify(courseService, times(1)).getCourse(Mockito.eq(course.getId()));
        verify(resourceGroupPoolService, times(1)).getResourceGroupPool(Mockito.eq(resourceGroupPool1.getId()));
        verify(reservationService, times(1)).findRgPoolReservations(
                Mockito.eq(resourceGroupPool1), Mockito.eq(course), Mockito.eq(start), Mockito.eq(end));

        verify(userRepository, times(1)).findById(Mockito.eq(userId1));
    }

    @Test
    @WithMockUser(username = "5da2cdeb-38da-4a27-bf8d-6b33ae49726f", authorities = "student")
    public void Given_ExistingCourseAndResourceGroupPoolIdentifiersArePassedAndNoReservationsExist_When_GetRgPoolReservationsInGivenCourse_Then_ReturnsEmptyListOfReservations() throws Exception {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(6);

        when(courseService.getCourse(Mockito.eq(course.getId()))).thenReturn(course);
        when(resourceGroupPoolService.getResourceGroupPool(Mockito.eq(resourceGroupPool1.getId()))).thenReturn(resourceGroupPool1);
        when(reservationService.findRgPoolReservations(
                Mockito.eq(resourceGroupPool1), Mockito.eq(course), Mockito.eq(start), Mockito.eq(end)))
                .thenReturn(List.of());

        when(userRepository.findById(Mockito.eq(userId1))).thenReturn(Optional.of(user1));

        mockMvc.perform(get("/reservations/courses/{courseId}/resource-group-pools/{rgPoolId}/period",
                        course.getId(), resourceGroupPool1.getId())
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(courseService, times(1)).getCourse(Mockito.eq(course.getId()));
        verify(resourceGroupPoolService, times(1)).getResourceGroupPool(Mockito.eq(resourceGroupPool1.getId()));
        verify(reservationService, times(1)).findRgPoolReservations(
                Mockito.eq(resourceGroupPool1), Mockito.eq(course), Mockito.eq(start), Mockito.eq(end));

        verify(userRepository, times(1)).findById(Mockito.eq(userId1));
    }

    @Test
    @WithMockUser(username = "5da2cdeb-38da-4a27-bf8d-6b33ae49726f", authorities = "student")
    public void Given_NonExistentCourseIdentifierIsPassedAndNoReservationsExist_When_GetRgPoolReservationsInGivenCourse_Then_Returns404NotFound() throws Exception {
        UUID nonExistentCourseIdentifier = UUID.randomUUID();
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(6);

        when(courseService.getCourse(Mockito.eq(nonExistentCourseIdentifier)))
                .thenThrow(CourseNotFoundException.class);

        when(userRepository.findById(Mockito.eq(userId1))).thenReturn(Optional.of(user1));

        mockMvc.perform(get("/reservations/courses/{courseId}/resource-group-pools/{rgPoolId}/period",
                        nonExistentCourseIdentifier, resourceGroupPool1.getId())
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(courseService, times(1))
                .getCourse(Mockito.eq(nonExistentCourseIdentifier));
    }

    @Test
    @WithMockUser(username = "5da2cdeb-38da-4a27-bf8d-6b33ae49726f", authorities = "student")
    public void Given_NonExistentResourceGroupPoolIdentifierIsPassedAndNoReservationsExist_When_GetRgPoolReservationsInGivenCourse_Then_Returns404NotFound() throws Exception {
        UUID nonExistentResourceGroupIdentifier = UUID.randomUUID();
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(6);

        when(courseService.getCourse(Mockito.eq(course.getId()))).thenReturn(course);
        when(resourceGroupPoolService.getResourceGroupPool(Mockito.eq(nonExistentResourceGroupIdentifier)))
                .thenThrow(ResourceGroupNotFoundException.class);

        mockMvc.perform(get("/reservations/courses/{courseId}/resource-group-pools/{rgPoolId}/period",
                        course.getId(), nonExistentResourceGroupIdentifier)
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(courseService, times(1)).getCourse(Mockito.eq(course.getId()));
        verify(resourceGroupPoolService, times(1))
                .getResourceGroupPool(Mockito.eq(nonExistentResourceGroupIdentifier));
    }

    @Test
    @WithMockUser(username = "f758db9b-3227-4b40-b709-52ea13f814a4", authorities = "student")
    public void Given_UserCouldNotBeFound_When_GetRgPoolReservationsInGivenCourse_Then_Returns404NotFound() throws Exception {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(6);

        when(courseService.getCourse(Mockito.eq(course.getId()))).thenReturn(course);
        when(resourceGroupPoolService.getResourceGroupPool(Mockito.eq(resourceGroupPool1.getId()))).thenReturn(resourceGroupPool1);
        when(reservationService.findRgPoolReservations(
                Mockito.eq(resourceGroupPool1), Mockito.eq(course), Mockito.eq(start), Mockito.eq(end)))
                .thenReturn(List.of());

        when(userRepository.findById(Mockito.eq(studentId))).thenReturn(Optional.empty());

        mockMvc.perform(get("/reservations/courses/{courseId}/resource-group-pools/{rgPoolId}/period",
                        course.getId(), resourceGroupPool1.getId())
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(courseService, times(1)).getCourse(Mockito.eq(course.getId()));
        verify(resourceGroupPoolService, times(1)).getResourceGroupPool(resourceGroupPool1.getId());
        verify(reservationService, times(1)).findRgPoolReservations(
                Mockito.eq(resourceGroupPool1), Mockito.eq(course), Mockito.eq(start), Mockito.eq(end));

        verify(userRepository, times(1)).findById(Mockito.eq(studentId));
    }

    @Test
    @WithMockUser(username = "63c7b8c8-6a46-4784-9843-1096442dafd2", authorities = "administrator")
    public void Given_NonExistentResourceGroupPoolIdentifierIsPassedAndSomeReservationsExistAsAdministrator_When_GetRgPoolReservationsInGivenCourse_Then_Returns404NotFound() throws Exception {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(6);

        when(courseService.getCourse(Mockito.eq(course.getId()))).thenReturn(course);
        when(resourceGroupPoolService.getResourceGroupPool(Mockito.eq(resourceGroupPool1.getId()))).thenReturn(resourceGroupPool1);
        when(reservationService.findRgPoolReservations(
                Mockito.eq(resourceGroupPool1), Mockito.eq(course), Mockito.eq(start), Mockito.eq(end)))
                .thenReturn(List.of(reservation2, reservation4));

        when(userRepository.findById(Mockito.eq(adminId))).thenReturn(Optional.of(admin));

        MvcResult result = mockMvc.perform(get("/reservations/courses/{courseId}/resource-group-pools/{rgPoolId}/period",
                        course.getId(), resourceGroupPool1.getId())
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        List<ReservationDto> foundReservations = mapper.readValue(json, new TypeReference<>() {});

        assertNotNull(foundReservations);
        assertFalse(foundReservations.isEmpty());
        assertEquals(2, foundReservations.size());

        ReservationDto firstReservation = foundReservations.getFirst();
        assertNotNull(firstReservation);
        assertEquals(reservation2.getId(), firstReservation.id());
        assertEquals(reservation2.getResourceGroup().getId(), UUID.fromString(firstReservation.resourceGroup().id()));
        assertEquals(reservation2.getResourceGroup().getName(), firstReservation.resourceGroup().name());
        assertEquals(reservation2.getResourceGroup().getDescription(), firstReservation.resourceGroup().description());
        assertEquals(reservation2.getResourceGroup().getMaxRentTime(), firstReservation.resourceGroup().maxRentTime());
        assertEquals(reservation2.getTeam().getId(), firstReservation.team().getId());
        assertEquals(reservation2.getTeam().getName(), firstReservation.team().getName());
        assertEquals(reservation2.getTeam().getMaxSize(), firstReservation.team().getMaxSize());
        assertEquals(reservation2.getStartTime(), firstReservation.start());
        assertEquals(reservation2.getEndTime(), firstReservation.end());

        ReservationDto secondReservation = foundReservations.getLast();
        assertNotNull(secondReservation);
        assertEquals(reservation4.getId(), secondReservation.id());
        assertEquals(reservation4.getResourceGroup().getId(), UUID.fromString(secondReservation.resourceGroup().id()));
        assertEquals(reservation4.getResourceGroup().getName(), secondReservation.resourceGroup().name());
        assertEquals(reservation4.getResourceGroup().getDescription(), secondReservation.resourceGroup().description());
        assertEquals(reservation4.getResourceGroup().getMaxRentTime(), secondReservation.resourceGroup().maxRentTime());
        assertEquals(reservation4.getTeam().getId(), secondReservation.team().getId());
        assertEquals(reservation4.getTeam().getName(), secondReservation.team().getName());
        assertEquals(reservation4.getTeam().getMaxSize(), secondReservation.team().getMaxSize());
        assertEquals(reservation4.getStartTime(), secondReservation.start());
        assertEquals(reservation4.getEndTime(), secondReservation.end());

        verify(courseService, times(1)).getCourse(Mockito.eq(course.getId()));
        verify(resourceGroupPoolService, times(1)).getResourceGroupPool(resourceGroupPool1.getId());
        verify(reservationService, times(1)).findRgPoolReservations(
                Mockito.eq(resourceGroupPool1), Mockito.eq(course), Mockito.eq(start), Mockito.eq(end));

        verify(userRepository, times(1)).findById(Mockito.eq(adminId));
    }

    @Test
    @WithMockUser(username = "e989c375-5eed-4ec6-b4b3-8ace83ed99fe", authorities = "teacher")
    public void Given_NonExistentResourceGroupPoolIdentifierIsPassedAndSomeReservationsExistAsTeacherInCourse_When_GetRgPoolReservationsInGivenCourse_Then_Returns404NotFound() throws Exception {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(6);

        when(courseService.getCourse(Mockito.eq(course.getId()))).thenReturn(course);
        when(resourceGroupPoolService.getResourceGroupPool(Mockito.eq(resourceGroupPool1.getId()))).thenReturn(resourceGroupPool1);
        when(reservationService.findRgPoolReservations(
                Mockito.eq(resourceGroupPool1), Mockito.eq(course), Mockito.eq(start), Mockito.eq(end)))
                .thenReturn(List.of(reservation2, reservation4));

        when(userRepository.findById(Mockito.eq(teacherId1))).thenReturn(Optional.of(teacher1));

        MvcResult result = mockMvc.perform(get("/reservations/courses/{courseId}/resource-group-pools/{rgPoolId}/period",
                        course.getId(), resourceGroupPool1.getId())
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        List<ReservationDto> foundReservations = mapper.readValue(json, new TypeReference<>() {});

        assertNotNull(foundReservations);
        assertFalse(foundReservations.isEmpty());
        assertEquals(2, foundReservations.size());

        ReservationDto firstReservation = foundReservations.getFirst();
        assertNotNull(firstReservation);
        assertEquals(reservation2.getId(), firstReservation.id());
        assertEquals(reservation2.getResourceGroup().getId(), UUID.fromString(firstReservation.resourceGroup().id()));
        assertEquals(reservation2.getResourceGroup().getName(), firstReservation.resourceGroup().name());
        assertEquals(reservation2.getResourceGroup().getDescription(), firstReservation.resourceGroup().description());
        assertEquals(reservation2.getResourceGroup().getMaxRentTime(), firstReservation.resourceGroup().maxRentTime());
        assertEquals(reservation2.getTeam().getId(), firstReservation.team().getId());
        assertEquals(reservation2.getTeam().getName(), firstReservation.team().getName());
        assertEquals(reservation2.getTeam().getMaxSize(), firstReservation.team().getMaxSize());
        assertEquals(reservation2.getStartTime(), firstReservation.start());
        assertEquals(reservation2.getEndTime(), firstReservation.end());

        ReservationDto secondReservation = foundReservations.getLast();
        assertNotNull(secondReservation);
        assertEquals(reservation4.getId(), secondReservation.id());
        assertEquals(reservation4.getResourceGroup().getId(), UUID.fromString(secondReservation.resourceGroup().id()));
        assertEquals(reservation4.getResourceGroup().getName(), secondReservation.resourceGroup().name());
        assertEquals(reservation4.getResourceGroup().getDescription(), secondReservation.resourceGroup().description());
        assertEquals(reservation4.getResourceGroup().getMaxRentTime(), secondReservation.resourceGroup().maxRentTime());
        assertEquals(reservation4.getTeam().getId(), secondReservation.team().getId());
        assertEquals(reservation4.getTeam().getName(), secondReservation.team().getName());
        assertEquals(reservation4.getTeam().getMaxSize(), secondReservation.team().getMaxSize());
        assertEquals(reservation4.getStartTime(), secondReservation.start());
        assertEquals(reservation4.getEndTime(), secondReservation.end());

        verify(courseService, times(1)).getCourse(Mockito.eq(course.getId()));
        verify(resourceGroupPoolService, times(1)).getResourceGroupPool(resourceGroupPool1.getId());
        verify(reservationService, times(1)).findRgPoolReservations(
                Mockito.eq(resourceGroupPool1), Mockito.eq(course), Mockito.eq(start), Mockito.eq(end));

        verify(userRepository, times(1)).findById(Mockito.eq(teacherId1));
    }

    @Test
    @WithMockUser(username = "f1ff980e-d8e8-497d-b9f8-b7cb72a27356", authorities = "teacher")
    public void Given_NonExistentResourceGroupPoolIdentifierIsPassedAndSomeReservationsExistAsTeacherNotInCourse_When_GetRgPoolReservationsInGivenCourse_Then_Returns404NotFound() throws Exception {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(6);

        when(courseService.getCourse(Mockito.eq(course.getId()))).thenReturn(course);
        when(resourceGroupPoolService.getResourceGroupPool(Mockito.eq(resourceGroupPool1.getId()))).thenReturn(resourceGroupPool1);
        when(reservationService.findRgPoolReservations(
                Mockito.eq(resourceGroupPool1), Mockito.eq(course), Mockito.eq(start), Mockito.eq(end)))
                .thenReturn(List.of(reservation2, reservation4));

        when(userRepository.findById(Mockito.eq(teacherId2))).thenReturn(Optional.of(teacher2));

        mockMvc.perform(get("/reservations/courses/{courseId}/resource-group-pools/{rgPoolId}/period",
                        course.getId(), resourceGroupPool1.getId())
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(courseService, times(1)).getCourse(Mockito.eq(course.getId()));
        verify(resourceGroupPoolService, times(1)).getResourceGroupPool(resourceGroupPool1.getId());
        verify(reservationService, times(1)).findRgPoolReservations(
                Mockito.eq(resourceGroupPool1), Mockito.eq(course), Mockito.eq(start), Mockito.eq(end));

        verify(userRepository, times(1)).findById(Mockito.eq(teacherId2));
    }

    @Test
    @WithMockUser(username = "f758db9b-3227-4b40-b709-52ea13f814a4", authorities = "student")
    public void Given_NonExistentResourceGroupPoolIdentifierIsPassedAndNoReservationsExistAsTeacherNotInCourse_When_GetRgPoolReservationsInGivenCourse_Then_Returns404NotFound() throws Exception {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(6);

        when(courseService.getCourse(Mockito.eq(course.getId()))).thenReturn(course);
        when(resourceGroupPoolService.getResourceGroupPool(Mockito.eq(resourceGroupPool1.getId()))).thenReturn(resourceGroupPool1);
        when(reservationService.findRgPoolReservations(
                Mockito.eq(resourceGroupPool1), Mockito.eq(course), Mockito.eq(start), Mockito.eq(end)))
                .thenReturn(List.of(reservation2, reservation4));

        when(userRepository.findById(Mockito.eq(studentId))).thenReturn(Optional.of(student));

        mockMvc.perform(get("/reservations/courses/{courseId}/resource-group-pools/{rgPoolId}/period",
                        course.getId(), resourceGroupPool1.getId())
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(courseService, times(1)).getCourse(Mockito.eq(course.getId()));
        verify(resourceGroupPoolService, times(1)).getResourceGroupPool(resourceGroupPool1.getId());
        verify(reservationService, times(1)).findRgPoolReservations(
                Mockito.eq(resourceGroupPool1), Mockito.eq(course), Mockito.eq(start), Mockito.eq(end));

        verify(userRepository, times(1)).findById(Mockito.eq(studentId));
    }

    /* GetActiveReservations method tests */

    @Test
    @WithMockUser(username = "5da2cdeb-38da-4a27-bf8d-6b33ae49726f", authorities = "student")
    public void Given_ExistingCourseIdentifierIsPassedAndTeamCouldBeFoundForCurrentUserAndSomeActiveReservationsExist_When_GetActiveReservations_Then_ReturnsListOfActiveReservations() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        when(courseService.getCourse(Mockito.eq(course.getId()))).thenReturn(course);
        when(teamService.getTeamByCourseAndUser(Mockito.eq(course), Mockito.eq(userId1))).thenReturn(team1);

        when(reservationService.findActiveReservations(Mockito.eq(team1.getId()), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(reservation1, reservation3), pageable, 2));

        MvcResult result = mockMvc.perform(get("/reservations/active/courses/{courseId}", course.getId())
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        PageDto<ReservationDto> reservationPage = mapper.readValue(json, new TypeReference<>() {});

        assertNotNull(reservationPage);

        PageInfoDto pageInfo = reservationPage.page();

        assertEquals(pageInfo.page(), 0);
        assertEquals(pageInfo.elements(), 2);
        assertEquals(pageInfo.totalPages(), 1);
        assertEquals(pageInfo.totalElements(), 2);

        List<ReservationDto> foundReservations = reservationPage.items();

        assertNotNull(foundReservations);
        assertFalse(foundReservations.isEmpty());
        assertEquals(2, foundReservations.size());

        ReservationDto firstReservation = foundReservations.getFirst();
        assertNotNull(firstReservation);
        assertEquals(reservation1.getId(), firstReservation.id());
        assertEquals(reservation1.getResourceGroup().getId(), UUID.fromString(firstReservation.resourceGroup().id()));
        assertEquals(reservation1.getResourceGroup().getName(), firstReservation.resourceGroup().name());
        assertEquals(reservation1.getResourceGroup().getDescription(), firstReservation.resourceGroup().description());
        assertEquals(reservation1.getResourceGroup().getMaxRentTime(), firstReservation.resourceGroup().maxRentTime());
        assertEquals(reservation1.getTeam().getId(), firstReservation.team().getId());
        assertEquals(reservation1.getTeam().getName(), firstReservation.team().getName());
        assertEquals(reservation1.getTeam().getMaxSize(), firstReservation.team().getMaxSize());
        assertEquals(reservation1.getStartTime(), firstReservation.start());
        assertEquals(reservation1.getEndTime(), firstReservation.end());

        ReservationDto secondReservation = foundReservations.getLast();
        assertNotNull(secondReservation);
        assertEquals(reservation3.getId(), secondReservation.id());
        assertEquals(reservation3.getResourceGroup().getId(), UUID.fromString(secondReservation.resourceGroup().id()));
        assertEquals(reservation3.getResourceGroup().getName(), secondReservation.resourceGroup().name());
        assertEquals(reservation3.getResourceGroup().getDescription(), secondReservation.resourceGroup().description());
        assertEquals(reservation3.getResourceGroup().getMaxRentTime(), secondReservation.resourceGroup().maxRentTime());
        assertEquals(reservation3.getTeam().getId(), secondReservation.team().getId());
        assertEquals(reservation3.getTeam().getName(), secondReservation.team().getName());
        assertEquals(reservation3.getTeam().getMaxSize(), secondReservation.team().getMaxSize());
        assertEquals(reservation3.getStartTime(), secondReservation.start());
        assertEquals(reservation3.getEndTime(), secondReservation.end());

        verify(courseService, times(1)).getCourse(Mockito.eq(course.getId()));
        verify(teamService, times(1)).getTeamByCourseAndUser(Mockito.eq(course), Mockito.eq(userId1));

        verify(reservationService, times(1))
                .findActiveReservations(Mockito.eq(team1.getId()), Mockito.eq(pageable));
    }

    @Test
    @WithMockUser(username = "5da2cdeb-38da-4a27-bf8d-6b33ae49726f", authorities = "student")
    public void Given_ExistingCourseIdentifierIsPassedAndTeamCouldBeFoundForCurrentUserAndNoActiveReservationsExist_When_GetActiveReservations_Then_ReturnsEmptyListOfReservations() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        when(courseService.getCourse(Mockito.eq(course.getId()))).thenReturn(course);
        when(teamService.getTeamByCourseAndUser(Mockito.eq(course), Mockito.eq(userId1))).thenReturn(team1);

        when(reservationService.findActiveReservations(Mockito.eq(team1.getId()), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        mockMvc.perform(get("/reservations/active/courses/{courseId}", course.getId())
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(courseService, times(1)).getCourse(Mockito.eq(course.getId()));
        verify(teamService, times(1)).getTeamByCourseAndUser(Mockito.eq(course), Mockito.eq(userId1));

        verify(reservationService, times(1))
                .findActiveReservations(Mockito.eq(team1.getId()), Mockito.eq(pageable));
    }

    @Test
    @WithMockUser(username = "5da2cdeb-38da-4a27-bf8d-6b33ae49726f", authorities = "student")
    public void Given_NonExistentCourseIdentifierIsPassed_When_GetActiveReservations_Then_Returns404NotFound() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;
        UUID nonExistentCourseIdentifier = UUID.randomUUID();

        when(courseService.getCourse(Mockito.eq(nonExistentCourseIdentifier)))
                .thenThrow(CourseNotFoundException.class);

        mockMvc.perform(get("/reservations/active/courses/{courseId}", nonExistentCourseIdentifier)
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(courseService, times(1)).getCourse(Mockito.eq(nonExistentCourseIdentifier));
    }

    @Test
    @WithMockUser(username = "5da2cdeb-38da-4a27-bf8d-6b33ae49726f", authorities = "student")
    public void Given_TeamCouldNotBeFoundForCurrentlyAuthenticatedUser_When_GetActiveReservations_Then_Returns404NotFound() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;

        when(courseService.getCourse(Mockito.eq(course.getId()))).thenReturn(course);
        when(teamService.getTeamByCourseAndUser(Mockito.eq(course), Mockito.eq(userId1)))
                .thenThrow(TeamNotFoundException.class);

        mockMvc.perform(get("/reservations/active/courses/{courseId}", course.getId())
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(courseService, times(1)).getCourse(Mockito.eq(course.getId()));
        verify(teamService, times(1)).getTeamByCourseAndUser(Mockito.eq(course), Mockito.eq(userId1));
    }

    /* GetHistoricReservations method tests */

    @Test
    @WithMockUser(username = "5da2cdeb-38da-4a27-bf8d-6b33ae49726f", authorities = "student")
    public void Given_ExistingCourseIdentifierIsPassedAndTeamCouldBeFoundForCurrentUserAndSomeHistoricReservationsExist_When_GetHistoricReservations_Then_ReturnsListOfHistoricalReservations() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        when(courseService.getCourse(Mockito.eq(course.getId()))).thenReturn(course);
        when(teamService.getTeamByCourseAndUser(Mockito.eq(course), Mockito.eq(userId1))).thenReturn(team1);

        when(reservationService.findHistoricalReservations(Mockito.eq(team1.getId()), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(reservation1, reservation3), pageable, 2));

        MvcResult result = mockMvc.perform(get("/reservations/historic/courses/{courseId}", course.getId())
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        PageDto<ReservationDto> reservationPage = mapper.readValue(json, new TypeReference<>() {});

        assertNotNull(reservationPage);

        PageInfoDto pageInfo = reservationPage.page();

        assertEquals(pageInfo.page(), 0);
        assertEquals(pageInfo.elements(), 2);
        assertEquals(pageInfo.totalPages(), 1);
        assertEquals(pageInfo.totalElements(), 2);

        List<ReservationDto> foundReservations = reservationPage.items();

        assertNotNull(foundReservations);
        assertFalse(foundReservations.isEmpty());
        assertEquals(2, foundReservations.size());

        ReservationDto firstReservation = foundReservations.getFirst();
        assertNotNull(firstReservation);
        assertEquals(reservation1.getId(), firstReservation.id());
        assertEquals(reservation1.getResourceGroup().getId(), UUID.fromString(firstReservation.resourceGroup().id()));
        assertEquals(reservation1.getResourceGroup().getName(), firstReservation.resourceGroup().name());
        assertEquals(reservation1.getResourceGroup().getDescription(), firstReservation.resourceGroup().description());
        assertEquals(reservation1.getResourceGroup().getMaxRentTime(), firstReservation.resourceGroup().maxRentTime());
        assertEquals(reservation1.getTeam().getId(), firstReservation.team().getId());
        assertEquals(reservation1.getTeam().getName(), firstReservation.team().getName());
        assertEquals(reservation1.getTeam().getMaxSize(), firstReservation.team().getMaxSize());
        assertEquals(reservation1.getStartTime(), firstReservation.start());
        assertEquals(reservation1.getEndTime(), firstReservation.end());

        ReservationDto secondReservation = foundReservations.getLast();
        assertNotNull(secondReservation);
        assertEquals(reservation3.getId(), secondReservation.id());
        assertEquals(reservation3.getResourceGroup().getId(), UUID.fromString(secondReservation.resourceGroup().id()));
        assertEquals(reservation3.getResourceGroup().getName(), secondReservation.resourceGroup().name());
        assertEquals(reservation3.getResourceGroup().getDescription(), secondReservation.resourceGroup().description());
        assertEquals(reservation3.getResourceGroup().getMaxRentTime(), secondReservation.resourceGroup().maxRentTime());
        assertEquals(reservation3.getTeam().getId(), secondReservation.team().getId());
        assertEquals(reservation3.getTeam().getName(), secondReservation.team().getName());
        assertEquals(reservation3.getTeam().getMaxSize(), secondReservation.team().getMaxSize());
        assertEquals(reservation3.getStartTime(), secondReservation.start());
        assertEquals(reservation3.getEndTime(), secondReservation.end());

        verify(courseService, times(1)).getCourse(Mockito.eq(course.getId()));
        verify(teamService, times(1)).getTeamByCourseAndUser(Mockito.eq(course), Mockito.eq(userId1));

        verify(reservationService, times(1))
                .findHistoricalReservations(Mockito.eq(team1.getId()), Mockito.eq(pageable));
    }

    @Test
    @WithMockUser(username = "5da2cdeb-38da-4a27-bf8d-6b33ae49726f", authorities = "student")
    public void Given_ExistingCourseIdentifierIsPassedAndTeamCouldBeFoundForCurrentUserAndNoHistoricReservationsExist_When_GetHistoricReservations_Then_ReturnsEmptyListOfReservations() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        when(courseService.getCourse(Mockito.eq(course.getId()))).thenReturn(course);
        when(teamService.getTeamByCourseAndUser(Mockito.eq(course), Mockito.eq(userId1))).thenReturn(team1);

        when(reservationService.findHistoricalReservations(Mockito.eq(team1.getId()), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        mockMvc.perform(get("/reservations/historic/courses/{courseId}", course.getId())
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(courseService, times(1)).getCourse(Mockito.eq(course.getId()));
        verify(teamService, times(1)).getTeamByCourseAndUser(Mockito.eq(course), Mockito.eq(userId1));

        verify(reservationService, times(1))
                .findHistoricalReservations(Mockito.eq(team1.getId()), Mockito.eq(pageable));
    }

    @Test
    @WithMockUser(username = "5da2cdeb-38da-4a27-bf8d-6b33ae49726f", authorities = "student")
    public void Given_NonExistentCourseIdentifierIsPassed_When_GetHistoricReservations_Then_Returns404NotFound() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;
        UUID nonExistentCourseIdentifier = UUID.randomUUID();

        when(courseService.getCourse(Mockito.eq(nonExistentCourseIdentifier))).thenThrow(CourseNotFoundException.class);

        mockMvc.perform(get("/reservations/historic/courses/{courseId}", nonExistentCourseIdentifier)
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(courseService, times(1)).getCourse(Mockito.eq(nonExistentCourseIdentifier));
    }

    @Test
    @WithMockUser(username = "5da2cdeb-38da-4a27-bf8d-6b33ae49726f", authorities = "student")
    public void Given_TeamCouldNotBeFoundForCurrentlyAuthenticatedUser_When_GetHistoricReservations_Then_Returns404NotFound() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        when(courseService.getCourse(Mockito.eq(course.getId()))).thenReturn(course);
        when(teamService.getTeamByCourseAndUser(Mockito.eq(course), Mockito.eq(userId1)))
                .thenThrow(TeamNotFoundException.class);

        when(reservationService.findHistoricalReservations(Mockito.eq(team1.getId()), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        mockMvc.perform(get("/reservations/historic/courses/{courseId}", course.getId())
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(courseService, times(1)).getCourse(Mockito.eq(course.getId()));
        verify(teamService, times(1)).getTeamByCourseAndUser(Mockito.eq(course), Mockito.eq(userId1));
    }

    /* GetActiveReservationsForTeam method tests */

    @Test
    @WithMockUser(username = "e989c375-5eed-4ec6-b4b3-8ace83ed99fe", authorities = "teacher")
    public void Given_ExistingTeamIdentifierIsPassedAndSomeActiveReservationsExist_When_GetActiveReservationsForTeam_Then_ReturnsListOfActiveReservations() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        when(userRepository.findById(Mockito.eq(teacherId1))).thenReturn(Optional.of(teacher1));
        when(teamService.getTeamById(Mockito.eq(team1.getId()))).thenReturn(team1);
        when(reservationService.findActiveReservations(Mockito.eq(team1.getId()), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(reservation1, reservation3), pageable, 2));

        MvcResult result = mockMvc.perform(get("/reservations/active/teams/{teamId}", team1.getId())
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        PageDto<ReservationDto> reservationPage = mapper.readValue(json, new TypeReference<>() {});

        assertNotNull(reservationPage);

        PageInfoDto pageInfo = reservationPage.page();

        assertEquals(pageInfo.page(), 0);
        assertEquals(pageInfo.elements(), 2);
        assertEquals(pageInfo.totalPages(), 1);
        assertEquals(pageInfo.totalElements(), 2);

        List<ReservationDto> foundReservations = reservationPage.items();

        assertNotNull(foundReservations);
        assertFalse(foundReservations.isEmpty());
        assertEquals(2, foundReservations.size());

        ReservationDto firstReservation = foundReservations.getFirst();
        assertNotNull(firstReservation);
        assertEquals(reservation1.getId(), firstReservation.id());
        assertEquals(reservation1.getResourceGroup().getId(), UUID.fromString(firstReservation.resourceGroup().id()));
        assertEquals(reservation1.getResourceGroup().getName(), firstReservation.resourceGroup().name());
        assertEquals(reservation1.getResourceGroup().getDescription(), firstReservation.resourceGroup().description());
        assertEquals(reservation1.getResourceGroup().getMaxRentTime(), firstReservation.resourceGroup().maxRentTime());
        assertEquals(reservation1.getTeam().getId(), firstReservation.team().getId());
        assertEquals(reservation1.getTeam().getName(), firstReservation.team().getName());
        assertEquals(reservation1.getTeam().getMaxSize(), firstReservation.team().getMaxSize());
        assertEquals(reservation1.getStartTime(), firstReservation.start());
        assertEquals(reservation1.getEndTime(), firstReservation.end());

        ReservationDto secondReservation = foundReservations.getLast();
        assertNotNull(secondReservation);
        assertEquals(reservation3.getId(), secondReservation.id());
        assertEquals(reservation3.getResourceGroup().getId(), UUID.fromString(secondReservation.resourceGroup().id()));
        assertEquals(reservation3.getResourceGroup().getName(), secondReservation.resourceGroup().name());
        assertEquals(reservation3.getResourceGroup().getDescription(), secondReservation.resourceGroup().description());
        assertEquals(reservation3.getResourceGroup().getMaxRentTime(), secondReservation.resourceGroup().maxRentTime());
        assertEquals(reservation3.getTeam().getId(), secondReservation.team().getId());
        assertEquals(reservation3.getTeam().getName(), secondReservation.team().getName());
        assertEquals(reservation3.getTeam().getMaxSize(), secondReservation.team().getMaxSize());
        assertEquals(reservation3.getStartTime(), secondReservation.start());
        assertEquals(reservation3.getEndTime(), secondReservation.end());

        verify(userRepository, times(1)).findById(Mockito.eq(teacherId1));
        verify(teamService, times(1)).getTeamById(Mockito.eq(team1.getId()));
        verify(reservationService, times(1)).findActiveReservations(Mockito.eq(team1.getId()), Mockito.eq(pageable));
    }

    @Test
    @WithMockUser(username = "e989c375-5eed-4ec6-b4b3-8ace83ed99fe", authorities = "teacher")
    public void Given_ExistingTeamIdentifierIsPassedAndNoActiveReservationsExist_When_GetActiveReservationsForTeam_Then_ReturnsEmptyListOfReservations() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        when(userRepository.findById(Mockito.eq(teacherId1))).thenReturn(Optional.of(teacher1));
        when(teamService.getTeamById(Mockito.eq(team1.getId()))).thenReturn(team1);
        when(reservationService.findActiveReservations(Mockito.eq(team1.getId()), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        mockMvc.perform(get("/reservations/active/teams/{teamId}", team1.getId())
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(userRepository, times(1)).findById(Mockito.eq(teacherId1));
        verify(teamService, times(1)).getTeamById(Mockito.eq(team1.getId()));
        verify(reservationService, times(1)).findActiveReservations(Mockito.eq(team1.getId()), Mockito.eq(pageable));
    }

    @Test
    @WithMockUser(username = "f758db9b-3227-4b40-b709-52ea13f814a4", authorities = "teacher")
    public void Given_CurrentlyAuthenticatedUserCouldNotBeFound_When_GetActiveReservationsForTeam_Then_Returns404NotFound() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;

        when(userRepository.findById(Mockito.eq(studentId))).thenReturn(Optional.empty());

        mockMvc.perform(get("/reservations/active/teams/{teamId}", team1.getId())
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(userRepository, times(1)).findById(Mockito.eq(studentId));
    }

    @Test
    @WithMockUser(username = "e989c375-5eed-4ec6-b4b3-8ace83ed99fe", authorities = "teacher")
    public void Given_NonExistentTeamIdentifierIsPassed_When_GetActiveReservationsForTeam_Then_Returns404NotFound() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;
        UUID nonExistentTeamIdentifier = UUID.randomUUID();

        when(userRepository.findById(Mockito.eq(teacherId1))).thenReturn(Optional.of(teacher1));
        when(teamService.getTeamById(Mockito.eq(nonExistentTeamIdentifier))).thenThrow(TeamNotFoundException.class);

        mockMvc.perform(get("/reservations/active/teams/{teamId}", nonExistentTeamIdentifier)
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(userRepository, times(1)).findById(Mockito.eq(teacherId1));
        verify(teamService, times(1)).getTeamById(Mockito.eq(nonExistentTeamIdentifier));
    }

    @Test
    @WithMockUser(username = "63c7b8c8-6a46-4784-9843-1096442dafd2", authorities = "administrator")
    public void Given_ExistingTeamIdentifierIsPassedAndSomeActiveReservationsExistAsAdministrator_When_GetActiveReservationsForTeam_Then_ReturnsListOfActiveReservations() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        when(userRepository.findById(Mockito.eq(adminId))).thenReturn(Optional.of(admin));
        when(teamService.getTeamById(Mockito.eq(team1.getId()))).thenReturn(team1);
        when(reservationService.findActiveReservations(Mockito.eq(team1.getId()), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(reservation1, reservation3), pageable, 2));

        MvcResult result = mockMvc.perform(get("/reservations/active/teams/{teamId}", team1.getId())
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        PageDto<ReservationDto> reservationPage = mapper.readValue(json, new TypeReference<>() {});

        assertNotNull(reservationPage);

        PageInfoDto pageInfo = reservationPage.page();

        assertEquals(pageInfo.page(), 0);
        assertEquals(pageInfo.elements(), 2);
        assertEquals(pageInfo.totalPages(), 1);
        assertEquals(pageInfo.totalElements(), 2);

        List<ReservationDto> foundReservations = reservationPage.items();

        assertNotNull(foundReservations);
        assertFalse(foundReservations.isEmpty());
        assertEquals(2, foundReservations.size());

        ReservationDto firstReservation = foundReservations.getFirst();
        assertNotNull(firstReservation);
        assertEquals(reservation1.getId(), firstReservation.id());
        assertEquals(reservation1.getResourceGroup().getId(), UUID.fromString(firstReservation.resourceGroup().id()));
        assertEquals(reservation1.getResourceGroup().getName(), firstReservation.resourceGroup().name());
        assertEquals(reservation1.getResourceGroup().getDescription(), firstReservation.resourceGroup().description());
        assertEquals(reservation1.getResourceGroup().getMaxRentTime(), firstReservation.resourceGroup().maxRentTime());
        assertEquals(reservation1.getTeam().getId(), firstReservation.team().getId());
        assertEquals(reservation1.getTeam().getName(), firstReservation.team().getName());
        assertEquals(reservation1.getTeam().getMaxSize(), firstReservation.team().getMaxSize());
        assertEquals(reservation1.getStartTime(), firstReservation.start());
        assertEquals(reservation1.getEndTime(), firstReservation.end());

        ReservationDto secondReservation = foundReservations.getLast();
        assertNotNull(secondReservation);
        assertEquals(reservation3.getId(), secondReservation.id());
        assertEquals(reservation3.getResourceGroup().getId(), UUID.fromString(secondReservation.resourceGroup().id()));
        assertEquals(reservation3.getResourceGroup().getName(), secondReservation.resourceGroup().name());
        assertEquals(reservation3.getResourceGroup().getDescription(), secondReservation.resourceGroup().description());
        assertEquals(reservation3.getResourceGroup().getMaxRentTime(), secondReservation.resourceGroup().maxRentTime());
        assertEquals(reservation3.getTeam().getId(), secondReservation.team().getId());
        assertEquals(reservation3.getTeam().getName(), secondReservation.team().getName());
        assertEquals(reservation3.getTeam().getMaxSize(), secondReservation.team().getMaxSize());
        assertEquals(reservation3.getStartTime(), secondReservation.start());
        assertEquals(reservation3.getEndTime(), secondReservation.end());

        verify(userRepository, times(1)).findById(Mockito.eq(adminId));
        verify(teamService, times(1)).getTeamById(Mockito.eq(team1.getId()));
        verify(reservationService, times(1)).findActiveReservations(Mockito.eq(team1.getId()), Mockito.eq(pageable));
    }

    @Test
    @WithMockUser(username = "63c7b8c8-6a46-4784-9843-1096442dafd2", authorities = "administrator")
    public void Given_ExistingTeamIdentifierIsPassedAndNoActiveReservationsExistAsAdministrator_When_GetActiveReservationsForTeam_Then_ReturnsEmptyListOfReservations() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        when(userRepository.findById(Mockito.eq(adminId))).thenReturn(Optional.of(admin));
        when(teamService.getTeamById(Mockito.eq(team1.getId()))).thenReturn(team1);
        when(reservationService.findActiveReservations(Mockito.eq(team1.getId()), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        mockMvc.perform(get("/reservations/active/teams/{teamId}", team1.getId())
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(userRepository, times(1)).findById(Mockito.eq(adminId));
        verify(teamService, times(1)).getTeamById(Mockito.eq(team1.getId()));
        verify(reservationService, times(1)).findActiveReservations(Mockito.eq(team1.getId()), Mockito.eq(pageable));
    }

    @Test
    @WithMockUser(username = "e989c375-5eed-4ec6-b4b3-8ace83ed99fe", authorities = "teacher")
    public void Given_ExistingTeamIdentifierIsPassedAndNoActiveReservationsExistAsTeacherInCourse_When_GetActiveReservationsForTeam_Then_ReturnsEmptyListReservations() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        when(userRepository.findById(Mockito.eq(teacherId1))).thenReturn(Optional.of(teacher1));
        when(teamService.getTeamById(Mockito.eq(team1.getId()))).thenReturn(team1);
        when(reservationService.findActiveReservations(Mockito.eq(team1.getId()), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 2));

        mockMvc.perform(get("/reservations/active/teams/{teamId}", team1.getId())
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(userRepository, times(1)).findById(Mockito.eq(teacherId1));
        verify(teamService, times(1)).getTeamById(Mockito.eq(team1.getId()));
        verify(reservationService, times(1)).findActiveReservations(Mockito.eq(team1.getId()), Mockito.eq(pageable));
    }

    @Test
    @WithMockUser(username = "f1ff980e-d8e8-497d-b9f8-b7cb72a27356", authorities = "teacher")
    public void Given_ExistingTeamIdentifierIsPassedAndSomeActiveReservationsExistAsTeacherNotInCourse_When_GetActiveReservationsForTeam_Then_ReturnsListOfActiveReservations() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        when(userRepository.findById(Mockito.eq(teacherId2))).thenReturn(Optional.of(teacher2));
        when(teamService.getTeamById(Mockito.eq(team1.getId()))).thenReturn(team1);
        when(reservationService.findActiveReservations(Mockito.eq(team1.getId()), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(reservation1, reservation3), pageable, 2));

        mockMvc.perform(get("/reservations/active/teams/{teamId}", team1.getId())
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(userRepository, times(1)).findById(Mockito.eq(teacherId2));
        verify(teamService, times(1)).getTeamById(Mockito.eq(team1.getId()));
        verify(reservationService, times(1)).findActiveReservations(Mockito.eq(team1.getId()), Mockito.eq(pageable));
    }

    @Test
    @WithMockUser(username = "f1ff980e-d8e8-497d-b9f8-b7cb72a27356", authorities = "teacher")
    public void Given_ExistingTeamIdentifierIsPassedAndNoActiveReservationsExistAsTeacherNotInCourse_When_GetActiveReservationsForTeam_Then_ReturnsEmptyListOfReservations() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        when(userRepository.findById(Mockito.eq(teacherId2))).thenReturn(Optional.of(teacher2));
        when(teamService.getTeamById(Mockito.eq(team1.getId()))).thenReturn(team1);
        when(reservationService.findActiveReservations(Mockito.eq(team1.getId()), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 2));

        mockMvc.perform(get("/reservations/active/teams/{teamId}", team1.getId())
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(userRepository, times(1)).findById(Mockito.eq(teacherId2));
        verify(teamService, times(1)).getTeamById(Mockito.eq(team1.getId()));
        verify(reservationService, times(1)).findActiveReservations(Mockito.eq(team1.getId()), Mockito.eq(pageable));
    }

    /* GetHistoricReservationsForTeam method tests */

    @Test
    @WithMockUser(username = "e989c375-5eed-4ec6-b4b3-8ace83ed99fe", authorities = "teacher")
    public void Given_ExistingTeamIdentifierIsPassedAndSomeHistoricReservationsExist_When_GetHistoricReservationsForTeam_Then_ReturnsListOfHistoricReservations() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        when(userRepository.findById(Mockito.eq(teacherId1))).thenReturn(Optional.of(teacher1));
        when(teamService.getTeamById(Mockito.eq(team1.getId()))).thenReturn(team1);
        when(reservationService.findHistoricalReservations(Mockito.eq(team1.getId()), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(reservation1, reservation3), pageable, 2));

        MvcResult result = mockMvc.perform(get("/reservations/historic/teams/{teamId}", team1.getId())
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        PageDto<ReservationDto> reservationPage = mapper.readValue(json, new TypeReference<>() {});

        assertNotNull(reservationPage);

        PageInfoDto pageInfo = reservationPage.page();

        assertEquals(pageInfo.page(), 0);
        assertEquals(pageInfo.elements(), 2);
        assertEquals(pageInfo.totalPages(), 1);
        assertEquals(pageInfo.totalElements(), 2);

        List<ReservationDto> foundReservations = reservationPage.items();

        assertNotNull(foundReservations);
        assertFalse(foundReservations.isEmpty());
        assertEquals(2, foundReservations.size());

        ReservationDto firstReservation = foundReservations.getFirst();
        assertNotNull(firstReservation);
        assertEquals(reservation1.getId(), firstReservation.id());
        assertEquals(reservation1.getResourceGroup().getId(), UUID.fromString(firstReservation.resourceGroup().id()));
        assertEquals(reservation1.getResourceGroup().getName(), firstReservation.resourceGroup().name());
        assertEquals(reservation1.getResourceGroup().getDescription(), firstReservation.resourceGroup().description());
        assertEquals(reservation1.getResourceGroup().getMaxRentTime(), firstReservation.resourceGroup().maxRentTime());
        assertEquals(reservation1.getTeam().getId(), firstReservation.team().getId());
        assertEquals(reservation1.getTeam().getName(), firstReservation.team().getName());
        assertEquals(reservation1.getTeam().getMaxSize(), firstReservation.team().getMaxSize());
        assertEquals(reservation1.getStartTime(), firstReservation.start());
        assertEquals(reservation1.getEndTime(), firstReservation.end());

        ReservationDto secondReservation = foundReservations.getLast();
        assertNotNull(secondReservation);
        assertEquals(reservation3.getId(), secondReservation.id());
        assertEquals(reservation3.getResourceGroup().getId(), UUID.fromString(secondReservation.resourceGroup().id()));
        assertEquals(reservation3.getResourceGroup().getName(), secondReservation.resourceGroup().name());
        assertEquals(reservation3.getResourceGroup().getDescription(), secondReservation.resourceGroup().description());
        assertEquals(reservation3.getResourceGroup().getMaxRentTime(), secondReservation.resourceGroup().maxRentTime());
        assertEquals(reservation3.getTeam().getId(), secondReservation.team().getId());
        assertEquals(reservation3.getTeam().getName(), secondReservation.team().getName());
        assertEquals(reservation3.getTeam().getMaxSize(), secondReservation.team().getMaxSize());
        assertEquals(reservation3.getStartTime(), secondReservation.start());
        assertEquals(reservation3.getEndTime(), secondReservation.end());

        verify(userRepository, times(1)).findById(Mockito.eq(teacherId1));
        verify(teamService, times(1)).getTeamById(Mockito.eq(team1.getId()));
        verify(reservationService, times(1)).findHistoricalReservations(Mockito.eq(team1.getId()), Mockito.eq(pageable));
    }

    @Test
    @WithMockUser(username = "e989c375-5eed-4ec6-b4b3-8ace83ed99fe", authorities = "student")
    public void Given_ExistingTeamIdentifierIsPassedAndNoHistoricReservationsExist_When_GetHistoricReservationsForTeam_Then_ReturnsEmptyListOfReservations() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        when(userRepository.findById(Mockito.eq(teacherId1))).thenReturn(Optional.of(teacher1));
        when(teamService.getTeamById(Mockito.eq(team1.getId()))).thenReturn(team1);
        when(reservationService.findHistoricalReservations(Mockito.eq(team1.getId()), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        mockMvc.perform(get("/reservations/historic/teams/{teamId}", team1.getId())
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(userRepository, times(1)).findById(Mockito.eq(teacherId1));
        verify(teamService, times(1)).getTeamById(Mockito.eq(team1.getId()));
        verify(reservationService, times(1)).findHistoricalReservations(Mockito.eq(team1.getId()), Mockito.eq(pageable));
    }

    @Test
    @WithMockUser(username = "e989c375-5eed-4ec6-b4b3-8ace83ed99fe", authorities = "student")
    public void Given_CurrentlyAuthenticatedUserCouldNotBeFound_When_GetHistoricReservationsForTeam_Then_Returns404NotFound() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        when(userRepository.findById(Mockito.eq(teacherId1))).thenReturn(Optional.empty());
        mockMvc.perform(get("/reservations/historic/teams/{teamId}", team1.getId())
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(userRepository, times(1)).findById(Mockito.eq(teacherId1));
    }

    @Test
    @WithMockUser(username = "e989c375-5eed-4ec6-b4b3-8ace83ed99fe", authorities = "student")
    public void Given_NonExistentTeamIdentifierIsPassed_When_GetHistoricReservationsForTeam_Then_Returns404NotFound() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;
        UUID nonExistentTeamIdentifier = UUID.randomUUID();

        when(userRepository.findById(Mockito.eq(teacherId1))).thenReturn(Optional.of(teacher1));
        when(teamService.getTeamById(Mockito.eq(nonExistentTeamIdentifier))).thenThrow(TeamNotFoundException.class);

        mockMvc.perform(get("/reservations/historic/teams/{teamId}", nonExistentTeamIdentifier)
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(userRepository, times(1)).findById(Mockito.eq(teacherId1));
        verify(teamService, times(1)).getTeamById(Mockito.eq(nonExistentTeamIdentifier));
    }

    @Test
    @WithMockUser(username = "63c7b8c8-6a46-4784-9843-1096442dafd2", authorities = "administrator")
    public void Given_ExistingTeamIdentifierIsPassedAndSomeHistoricReservationsExistAsAdministrator_When_GetHistoricReservationsForTeam_Then_ReturnsListOfHistoricReservations() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        when(userRepository.findById(Mockito.eq(adminId))).thenReturn(Optional.of(admin));
        when(teamService.getTeamById(Mockito.eq(team1.getId()))).thenReturn(team1);
        when(reservationService.findHistoricalReservations(Mockito.eq(team1.getId()), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(reservation1, reservation3), pageable, 2));

        MvcResult result = mockMvc.perform(get("/reservations/historic/teams/{teamId}", team1.getId())
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        PageDto<ReservationDto> reservationPage = mapper.readValue(json, new TypeReference<>() {});

        assertNotNull(reservationPage);

        PageInfoDto pageInfo = reservationPage.page();

        assertEquals(pageInfo.page(), 0);
        assertEquals(pageInfo.elements(), 2);
        assertEquals(pageInfo.totalPages(), 1);
        assertEquals(pageInfo.totalElements(), 2);

        List<ReservationDto> foundReservations = reservationPage.items();

        assertNotNull(foundReservations);
        assertFalse(foundReservations.isEmpty());
        assertEquals(2, foundReservations.size());

        ReservationDto firstReservation = foundReservations.getFirst();
        assertNotNull(firstReservation);
        assertEquals(reservation1.getId(), firstReservation.id());
        assertEquals(reservation1.getResourceGroup().getId(), UUID.fromString(firstReservation.resourceGroup().id()));
        assertEquals(reservation1.getResourceGroup().getName(), firstReservation.resourceGroup().name());
        assertEquals(reservation1.getResourceGroup().getDescription(), firstReservation.resourceGroup().description());
        assertEquals(reservation1.getResourceGroup().getMaxRentTime(), firstReservation.resourceGroup().maxRentTime());
        assertEquals(reservation1.getTeam().getId(), firstReservation.team().getId());
        assertEquals(reservation1.getTeam().getName(), firstReservation.team().getName());
        assertEquals(reservation1.getTeam().getMaxSize(), firstReservation.team().getMaxSize());
        assertEquals(reservation1.getStartTime(), firstReservation.start());
        assertEquals(reservation1.getEndTime(), firstReservation.end());

        ReservationDto secondReservation = foundReservations.getLast();
        assertNotNull(secondReservation);
        assertEquals(reservation3.getId(), secondReservation.id());
        assertEquals(reservation3.getResourceGroup().getId(), UUID.fromString(secondReservation.resourceGroup().id()));
        assertEquals(reservation3.getResourceGroup().getName(), secondReservation.resourceGroup().name());
        assertEquals(reservation3.getResourceGroup().getDescription(), secondReservation.resourceGroup().description());
        assertEquals(reservation3.getResourceGroup().getMaxRentTime(), secondReservation.resourceGroup().maxRentTime());
        assertEquals(reservation3.getTeam().getId(), secondReservation.team().getId());
        assertEquals(reservation3.getTeam().getName(), secondReservation.team().getName());
        assertEquals(reservation3.getTeam().getMaxSize(), secondReservation.team().getMaxSize());
        assertEquals(reservation3.getStartTime(), secondReservation.start());
        assertEquals(reservation3.getEndTime(), secondReservation.end());

        verify(userRepository, times(1)).findById(Mockito.eq(adminId));
        verify(teamService, times(1)).getTeamById(Mockito.eq(team1.getId()));
        verify(reservationService, times(1)).findHistoricalReservations(Mockito.eq(team1.getId()), Mockito.eq(pageable));
    }

    @Test
    @WithMockUser(username = "e989c375-5eed-4ec6-b4b3-8ace83ed99fe", authorities = "teacher")
    public void Given_ExistingTeamIdentifierIsPassedAndNoHistoricReservationsExistAsTeacherInCourse_When_GetHistoricReservationsForTeam_Then_ReturnsEmptyListOfReservations() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        when(userRepository.findById(Mockito.eq(teacherId1))).thenReturn(Optional.of(teacher1));
        when(teamService.getTeamById(Mockito.eq(team1.getId()))).thenReturn(team1);
        when(reservationService.findHistoricalReservations(Mockito.eq(team1.getId()), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        mockMvc.perform(get("/reservations/historic/teams/{teamId}", team1.getId())
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(userRepository, times(1)).findById(Mockito.eq(teacherId1));
        verify(teamService, times(1)).getTeamById(Mockito.eq(team1.getId()));
        verify(reservationService, times(1)).findHistoricalReservations(Mockito.eq(team1.getId()), Mockito.eq(pageable));
    }

    @Test
    @WithMockUser(username = "f1ff980e-d8e8-497d-b9f8-b7cb72a27356", authorities = "teacher")
    public void Given_ExistingTeamIdentifierIsPassedAndSomeHistoricReservationsExistAsTeacherNotInCourse_When_GetHistoricReservationsForTeam_Then_ReturnsEmptyListOfReservations() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        when(userRepository.findById(Mockito.eq(teacherId2))).thenReturn(Optional.of(teacher2));
        when(teamService.getTeamById(Mockito.eq(team1.getId()))).thenReturn(team1);
        when(reservationService.findHistoricalReservations(Mockito.eq(team1.getId()), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(reservation1, reservation3), pageable, 2));

        mockMvc.perform(get("/reservations/historic/teams/{teamId}", team1.getId())
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(userRepository, times(1)).findById(Mockito.eq(teacherId2));
        verify(teamService, times(1)).getTeamById(Mockito.eq(team1.getId()));
        verify(reservationService, times(1)).findHistoricalReservations(Mockito.eq(team1.getId()), Mockito.eq(pageable));
    }

    @Test
    @WithMockUser(username = "f1ff980e-d8e8-497d-b9f8-b7cb72a27356", authorities = "teacher")
    public void Given_ExistingTeamIdentifierIsPassedAndNoHistoricReservationsExistAsTeacherNotInCourse_When_GetHistoricReservationsForTeam_Then_ReturnsEmptyListOfReservations() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        when(userRepository.findById(Mockito.eq(teacherId2))).thenReturn(Optional.of(teacher2));
        when(teamService.getTeamById(Mockito.eq(team1.getId()))).thenReturn(team1);
        when(reservationService.findHistoricalReservations(Mockito.eq(team1.getId()), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        mockMvc.perform(get("/reservations/historic/teams/{teamId}", team1.getId())
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(userRepository, times(1)).findById(Mockito.eq(teacherId2));
        verify(teamService, times(1)).getTeamById(Mockito.eq(team1.getId()));
        verify(reservationService, times(1)).findHistoricalReservations(Mockito.eq(team1.getId()), Mockito.eq(pageable));
    }

    /* GetWindowLength method test */

    @Test
    @WithMockUser(username = "5da2cdeb-38da-4a27-bf8d-6b33ae49726f", authorities = "student")
    public void Given_CurrentlyLoggedInUserIsAuthenticated_When_GetWindowLength_Then_ReturnsWindowLength() throws Exception {
        MvcResult result = mockMvc.perform(get("/reservations/window-length"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        int windowLength = mapper.readValue(json, Integer.class);
        assertEquals(windowLength, 15);
    }

    /* FinishReservation method tests */

    @Test
    @WithMockUser(username = "5da2cdeb-38da-4a27-bf8d-6b33ae49726f", authorities = "student")
    public void Given_ExistingReservationIdentifierIsPassed_When_FinishReservation_Then_Returns204NoContent() throws Exception {
        when(reservationService.findReservationById(Mockito.eq(reservation1.getId()))).thenReturn(Optional.of(reservation1));
        when(userRepository.findById(Mockito.eq(userId1))).thenReturn(Optional.of(user1));
        doNothing().when(reservationService).finishReservation(Mockito.eq(reservation1));

        mockMvc.perform(post("/reservations/{reservationId}/cancel", reservation1.getId())
                        .secure(true)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(reservationService, times(1)).findReservationById(Mockito.eq(reservation1.getId()));
        verify(userRepository, times(1)).findById(Mockito.eq(userId1));
        verify(reservationService, times(1)).finishReservation(Mockito.eq(reservation1));
    }

    @Test
    @WithMockUser(username = "5da2cdeb-38da-4a27-bf8d-6b33ae49726f", authorities = "student")
    public void Given_CurrentlyLoggedInUserCouldNotBeFound_When_FinishReservation_Then_Returns404NotFound() throws Exception {
        when(reservationService.findReservationById(Mockito.eq(reservation1.getId()))).thenReturn(Optional.of(reservation1));
        when(userRepository.findById(Mockito.eq(userId1))).thenReturn(Optional.empty());

        mockMvc.perform(post("/reservations/{reservationId}/cancel", reservation1.getId())
                        .secure(true)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(reservationService, times(1)).findReservationById(Mockito.eq(reservation1.getId()));
        verify(userRepository, times(1)).findById(Mockito.eq(userId1));
    }

    @Test
    @WithMockUser(username = "5da2cdeb-38da-4a27-bf8d-6b33ae49726f", authorities = "student")
    public void Given_NonExistentReservationIdentifierIsPassed_When_FinishReservation_Then_Returns404NotFound() throws Exception {
        UUID nonExistentReservationId = UUID.randomUUID();

        when(reservationService.findReservationById(Mockito.eq(nonExistentReservationId))).thenReturn(Optional.empty());

        mockMvc.perform(post("/reservations/{reservationId}/cancel", nonExistentReservationId)
                        .secure(true)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(reservationService, times(1)).findReservationById(Mockito.eq(nonExistentReservationId));
    }

    @Test
    @WithMockUser(username = "63c7b8c8-6a46-4784-9843-1096442dafd2", authorities = "administrator")
    public void Given_ExistingReservationIdentifierIsPassedAsAdministrator_When_FinishReservation_Then_Returns204NoContent() throws Exception {
        when(reservationService.findReservationById(Mockito.eq(reservation1.getId()))).thenReturn(Optional.of(reservation1));
        when(userRepository.findById(Mockito.eq(adminId))).thenReturn(Optional.of(admin));
        doNothing().when(reservationService).finishReservation(Mockito.eq(reservation1));

        mockMvc.perform(post("/reservations/{reservationId}/cancel", reservation1.getId())
                        .secure(true)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(reservationService, times(1)).findReservationById(Mockito.eq(reservation1.getId()));
        verify(userRepository, times(1)).findById(Mockito.eq(adminId));
        verify(reservationService, times(1)).finishReservation(Mockito.eq(reservation1));
    }

    @Test
    @WithMockUser(username = "e989c375-5eed-4ec6-b4b3-8ace83ed99fe", authorities = "teacher")
    public void Given_ExistingReservationIdentifierIsPassedAsTeacherInCourse_When_FinishReservation_Then_Returns204NoContent() throws Exception {
        when(reservationService.findReservationById(Mockito.eq(reservation1.getId()))).thenReturn(Optional.of(reservation1));
        when(userRepository.findById(Mockito.eq(teacherId1))).thenReturn(Optional.of(teacher1));
        doNothing().when(reservationService).finishReservation(Mockito.eq(reservation1));

        mockMvc.perform(post("/reservations/{reservationId}/cancel", reservation1.getId())
                        .secure(true)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(reservationService, times(1)).findReservationById(Mockito.eq(reservation1.getId()));
        verify(userRepository, times(1)).findById(Mockito.eq(teacherId1));
        verify(reservationService, times(1)).finishReservation(Mockito.eq(reservation1));
    }

    @Test
    @WithMockUser(username = "f1ff980e-d8e8-497d-b9f8-b7cb72a27356", authorities = "teacher")
    public void Given_ExistingReservationIdentifierIsPassedAsTeacherNotInCourse_When_FinishReservation_Then_Returns404NotFound() throws Exception {
        when(reservationService.findReservationById(Mockito.eq(reservation1.getId()))).thenReturn(Optional.of(reservation1));
        when(userRepository.findById(Mockito.eq(teacherId2))).thenReturn(Optional.of(teacher2));

        mockMvc.perform(post("/reservations/{reservationId}/cancel", reservation1.getId())
                        .secure(true)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(reservationService, times(1)).findReservationById(Mockito.eq(reservation1.getId()));
        verify(userRepository, times(1)).findById(Mockito.eq(teacherId2));
    }

    @Test
    @WithMockUser(username = "a1de3736-ea14-4e45-b856-338a4c1d67f9", authorities = "student")
    public void Given_ExistingReservationIdentifierIsPassedAsStudentNotInTeam_When_FinishReservation_Then_Returns404NotFound() throws Exception {
        when(reservationService.findReservationById(Mockito.eq(reservation1.getId()))).thenReturn(Optional.of(reservation1));
        when(userRepository.findById(Mockito.eq(userId3))).thenReturn(Optional.of(user3));

        mockMvc.perform(post("/reservations/{reservationId}/cancel", reservation1.getId())
                        .secure(true)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(reservationService, times(1)).findReservationById(Mockito.eq(reservation1.getId()));
        verify(userRepository, times(1)).findById(Mockito.eq(userId3));
    }
}
