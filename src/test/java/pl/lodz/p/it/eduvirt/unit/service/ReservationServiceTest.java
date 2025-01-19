package pl.lodz.p.it.eduvirt.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.sdk4.types.Cluster;
import org.ovirt.engine.sdk4.types.Host;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import pl.lodz.p.it.eduvirt.dto.reservation.CreateReservationDto;
import pl.lodz.p.it.eduvirt.entity.*;
import pl.lodz.p.it.eduvirt.exceptions.*;
import pl.lodz.p.it.eduvirt.repository.*;
import pl.lodz.p.it.eduvirt.service.OVirtClusterService;
import pl.lodz.p.it.eduvirt.service.impl.ReservationServiceImpl;
import pl.lodz.p.it.eduvirt.util.BankerAlgorithm;
import pl.lodz.p.it.eduvirt.util.MetricUtil;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    /* Repositories */

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private CourseMetricRepository courseMetricRepository;

    @Mock
    private ClusterMetricRepository clusterMetricRepository;

    @Mock
    private MaintenanceIntervalRepository maintenanceIntervalRepository;

    /* Other services */

    @Mock
    private OVirtClusterService clusterService;

    /* Utils */

    @Mock
    private BankerAlgorithm bankerAlgorithm;

    @Mock
    private MetricUtil metricUtil;

    /* Mock injections */

    @InjectMocks
    private ReservationServiceImpl reservationService;

    /* Initialization */

    private Course course;

    /* Teams */

    private Team team1;
    private Team team2;

    private UUID userId1;
    private UUID userId2;
    private UUID userId3;
    private UUID userId4;

    private UUID userWithoutAccess;

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

    /* Metrics */

    private final String cpuCount = "cpu_count";
    private final String memorySize = "memory_size";
    private final String networkCount = "network_count";

    private Metric cpuCountMetric;
    private Metric memorySizeMetric;
    private Metric networkCountMetric;

    private ClusterMetric clusterCpuCount;
    private ClusterMetric clusterMemorySize;
    private ClusterMetric clusterNetworkCount;
    private final List<ClusterMetric> clusterMetrics = new LinkedList<>();
    private final Map<String, Object> clusterMetricMap = new HashMap<>();

    private CourseMetric courseCpuCount;
    private CourseMetric courseMemorySize;
    private CourseMetric courseNetworkCount;
    private final List<CourseMetric> courseMetrics = new LinkedList<>();
    private final Map<String, Object> courseMetricMap = new HashMap<>();

    private final int windowLength = 15;

    @BeforeEach
    public void setUp() throws Exception {
        ReflectionTestUtils.setField(reservationService, "windowLength", 15);
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

        userId1 = UUID.fromString("b1e0ce89-ca06-4e8a-afaf-a1fcfe806cdb");
        userId2 = UUID.fromString("11328f39-f9d8-4a5d-a001-0aee8ec7387e");
        userId3 = UUID.fromString("fad863d0-c1ae-457d-b1cd-506aca0e20a4");
        userId4 = UUID.fromString("26d7e7cc-e0b3-4da6-9d03-766e1ee7a6d1");

        userWithoutAccess = UUID.fromString("5c0d34af-5c97-479a-9551-21d263e51a95");

        user1 = new User(userId1, UUID.randomUUID(), "example1@example.com", "UserName1", "FirstName1", "LastName1");
        user2 = new User(userId2, UUID.randomUUID(), "example2@example.com", "UserName2", "FirstName2", "LastName2");
        user3 = new User(userId3, UUID.randomUUID(), "example3@example.com", "UserName3", "FirstName3", "LastName3");
        user4 = new User(userId4, UUID.randomUUID(), "example4@example.com", "UserName4", "FirstName4", "LastName4");

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
        team1.getUsers().addAll(listOfUsers1);

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
        resourceGroupPool1.setMaxRentTime(12);
        resourceGroupPool1.setMaxRent(6);
        resourceGroupPool1.setGracePeriod(12);

        resourceGroupPool1.setResourceGroups(List.of(resourceGroup3));

        resourceGroupPool2 = new ResourceGroupPool();
        resourceGroupPool2.setName("Course-RGPool2");
        resourceGroupPool2.setMaxRentTime(12);
        resourceGroupPool2.setMaxRent(6);
        resourceGroupPool2.setGracePeriod(12);

        resourceGroupPool2.setResourceGroups(List.of(resourceGroup4));

        id.setAccessible(true);
        id.set(resourceGroupPool1, UUID.randomUUID());
        id.set(resourceGroupPool2, UUID.randomUUID());
        id.setAccessible(false);

        /* PODS */

        Integer maxRentTime = 12;

        podStateful1 = new PodStateful(resourceGroup1, team1, course, maxRentTime);
        podStateful2 = new PodStateful(resourceGroup2, team2, course, maxRentTime);

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
        reservation1 = new Reservation(resourceGroup1, team1, currentTime.plusHours(1), currentTime.plusHours(7), true, 10);
        reservation2 = new Reservation(resourceGroup2, team2, currentTime.plusHours(1), currentTime.plusHours(7), true, 10);
        reservation3 = new Reservation(resourceGroup3, team1, currentTime.plusHours(1), currentTime.plusHours(7), true, 10);
        reservation4 = new Reservation(resourceGroup4, team2, currentTime.plusHours(1), currentTime.plusHours(7), true, 10);

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

        /* Metrics */

        cpuCountMetric = new Metric(cpuCount, Metric.MetricCategory.COUNTABLE);
        memorySizeMetric = new Metric(memorySize, Metric.MetricCategory.MEMORY);
        networkCountMetric = new Metric(networkCount, Metric.MetricCategory.COUNTABLE);

        id.setAccessible(true);
        id.set(cpuCountMetric, UUID.randomUUID());
        id.set(memorySizeMetric, UUID.randomUUID());
        id.set(networkCountMetric, UUID.randomUUID());
        id.setAccessible(false);

        clusterCpuCount = new ClusterMetric(existingClusterId, cpuCountMetric, 200.0);
        clusterMemorySize = new ClusterMetric(existingClusterId, memorySizeMetric, 10737418240.0);
        clusterNetworkCount = new ClusterMetric(existingClusterId, networkCountMetric, 50.0);

        clusterMetrics.add(clusterCpuCount);
        clusterMetrics.add(clusterMemorySize);
        clusterMetrics.add(clusterNetworkCount);

        courseCpuCount = new CourseMetric(course, cpuCountMetric, 50.0);
        courseMemorySize = new CourseMetric(course, memorySizeMetric, 50.0);
        courseNetworkCount = new CourseMetric(course, networkCountMetric, 50.0);

        courseMetrics.add(courseCpuCount);
        courseMetrics.add(courseMemorySize);
        courseMetrics.add(courseNetworkCount);

        clusterMetrics.forEach(clusterMetric -> clusterMetricMap.put(clusterMetric.getMetric().getName(), clusterMetric.getValue()));
        courseMetrics.forEach(courseMetric -> courseMetricMap.put(courseMetric.getMetric().getName(), courseMetric.getValue()));

        id.setAccessible(true);
        id.set(clusterCpuCount, UUID.randomUUID());
        id.set(clusterMemorySize, UUID.randomUUID());
        id.set(clusterNetworkCount, UUID.randomUUID());
        id.setAccessible(false);

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

    /* Tests */

    /* CreateReservationForStatefulPod method tests */

    @Test
    public void Given_AllDataInReservationCreateDtoIsValid_When_CreateReservationForStatefulPod_Then_CreatesNewReservationSuccessfully() {
        Cluster cluster = mock(Cluster.class);
        Host host1 = mock(Host.class);
        Host host2 = mock(Host.class);
        List<Host> hosts = List.of(host1, host2);

        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        CreateReservationDto newCreateDto = new CreateReservationDto(
                currentTime.plusHours(2), currentTime.plusHours(8),
                reservation1.getAutomaticStartup(),
                reservation1.getNotificationTime()
        );

        Reservation reservation = new Reservation(
                resourceGroup1, team1,
                newCreateDto.start(),
                newCreateDto.end(),
                reservation1.getAutomaticStartup(),
                reservation1.getNotificationTime()
        );

        when(clusterService.findClusterById(Mockito.eq(existingClusterId))).thenReturn(cluster);
        when(clusterService.findAllHostsInCluster(Mockito.eq(cluster))).thenReturn(hosts);

        when(reservationRepository.findAllRgReservationsForGivenTeam(Mockito.eq(resourceGroup1), Mockito.eq(team1)))
                .thenReturn(List.of());
        /*
         * TODO:
         *      Add missing checks for max reservation count and grace time (which could not be)
         *      check as of now (that is 2025-01-12T16:41:00).
         */

        when(maintenanceIntervalRepository.findAllIntervalsInGivenTimePeriod(
                Mockito.eq(existingClusterId), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end())))
                .thenReturn(List.of());
        when(reservationRepository.findRgReservations(
                Mockito.eq(resourceGroup1), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end())))
                .thenReturn(List.of());

        List<Reservation> courseReservationList = List.of(reservation1);
        when(courseMetricRepository.findAllByCourse(Mockito.eq(course))).thenReturn(courseMetrics);
        when(reservationRepository.findCourseReservations(Mockito.eq(course), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end())))
                .thenReturn(courseReservationList);

        when(bankerAlgorithm.process(Mockito.any(), Mockito.eq(courseReservationList),
                Mockito.eq(podStateful1.getResourceGroup()), Mockito.eq(cluster), Mockito.eq(hosts))).thenReturn(true);

        List<Reservation> clusterReservationList = List.of(reservation1, reservation2);
        when(clusterMetricRepository.findAllByClusterId(Mockito.eq(course.getClusterId()))).thenReturn(clusterMetrics);
        when(reservationRepository.findClusterReservations(Mockito.eq(course.getClusterId()), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end())))
                .thenReturn(clusterReservationList);

        when(bankerAlgorithm.process(Mockito.any(), Mockito.eq(clusterReservationList),
                Mockito.eq(podStateful1.getResourceGroup()), Mockito.eq(cluster), Mockito.eq(hosts))).thenReturn(true);

        when(reservationRepository.saveAndFlush(reservation)).thenReturn(reservation);

        reservationService.createReservationForStatefulPod(team1, podStateful1, newCreateDto);

        verify(clusterService, times(1)).findClusterById(Mockito.eq(existingClusterId));
        verify(clusterService, times(1)).findAllHostsInCluster(Mockito.eq(cluster));

        verify(reservationRepository, times(1))
                .findAllRgReservationsForGivenTeam(Mockito.eq(resourceGroup1), Mockito.eq(team1));
        /*
         * TODO:
         *      Add missing checks for max reservation count and grace time (which could not be)
         *      check as of now (that is 2025-01-12T16:41:00).
         */
        verify(maintenanceIntervalRepository, times(1)).findAllIntervalsInGivenTimePeriod(
                Mockito.eq(existingClusterId), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end()));
        verify(reservationRepository, times(1)).findRgReservations(
                Mockito.eq(resourceGroup1), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end()));

        verify(courseMetricRepository, times(1)).findAllByCourse(Mockito.eq(course));
        verify(reservationRepository, times(1))
                .findCourseReservations(Mockito.eq(course), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end()));

        verify(bankerAlgorithm, times(1)).process(Mockito.any(), Mockito.eq(courseReservationList),
                Mockito.eq(podStateful1.getResourceGroup()), Mockito.eq(cluster), Mockito.eq(hosts));

        verify(clusterMetricRepository, times(1)).findAllByClusterId(Mockito.eq(course.getClusterId()));
        verify(reservationRepository, times(1))
                .findClusterReservations(Mockito.eq(course.getClusterId()), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end()));

        verify(bankerAlgorithm, times(1)).process(Mockito.any(), Mockito.eq(clusterReservationList),
                Mockito.eq(podStateful1.getResourceGroup()), Mockito.eq(cluster), Mockito.eq(hosts));

        verify(reservationRepository, times(1)).saveAndFlush(reservation);
    }

    @Test
    public void Given_AllDataInReservationCreateDtoIsValidAndLimitsAreEqualTo0_When_CreateReservationForStatefulPod_Then_CreatesNewReservationSuccessfully() {
        Cluster cluster = mock(Cluster.class);
        Host host1 = mock(Host.class);
        Host host2 = mock(Host.class);
        List<Host> hosts = List.of(host1, host2);

        resourceGroup1.setMaxRentTime(0);

        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        CreateReservationDto newCreateDto = new CreateReservationDto(
                currentTime.plusHours(2), currentTime.plusHours(8),
                reservation1.getAutomaticStartup(),
                reservation1.getNotificationTime()
        );

        Reservation reservation = new Reservation(
                resourceGroup1, team1,
                newCreateDto.start(),
                newCreateDto.end(),
                reservation1.getAutomaticStartup(),
                reservation1.getNotificationTime()
        );

        when(clusterService.findClusterById(Mockito.eq(existingClusterId))).thenReturn(cluster);
        when(clusterService.findAllHostsInCluster(Mockito.eq(cluster))).thenReturn(hosts);

        when(reservationRepository.findAllRgReservationsForGivenTeam(Mockito.eq(resourceGroup1), Mockito.eq(team1)))
                .thenReturn(List.of());

        when(maintenanceIntervalRepository.findAllIntervalsInGivenTimePeriod(
                Mockito.eq(existingClusterId), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end())))
                .thenReturn(List.of());
        when(reservationRepository.findRgReservations(
                Mockito.eq(resourceGroup1), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end())))
                .thenReturn(List.of());

        List<Reservation> courseReservationList = List.of(reservation1);
        when(courseMetricRepository.findAllByCourse(Mockito.eq(course))).thenReturn(courseMetrics);
        when(reservationRepository.findCourseReservations(Mockito.eq(course), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end())))
                .thenReturn(courseReservationList);

        when(bankerAlgorithm.process(Mockito.any(), Mockito.eq(courseReservationList),
                Mockito.eq(podStateful1.getResourceGroup()), Mockito.eq(cluster), Mockito.eq(hosts))).thenReturn(true);

        List<Reservation> clusterReservationList = List.of(reservation1, reservation2);
        when(clusterMetricRepository.findAllByClusterId(Mockito.eq(course.getClusterId()))).thenReturn(clusterMetrics);
        when(reservationRepository.findClusterReservations(Mockito.eq(course.getClusterId()), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end())))
                .thenReturn(clusterReservationList);

        when(bankerAlgorithm.process(Mockito.any(), Mockito.eq(clusterReservationList),
                Mockito.eq(podStateful1.getResourceGroup()), Mockito.eq(cluster), Mockito.eq(hosts))).thenReturn(true);

        when(reservationRepository.saveAndFlush(reservation)).thenReturn(reservation);

        reservationService.createReservationForStatefulPod(team1, podStateful1, newCreateDto);

        verify(clusterService, times(1)).findClusterById(Mockito.eq(existingClusterId));
        verify(clusterService, times(1)).findAllHostsInCluster(Mockito.eq(cluster));

        verify(reservationRepository, times(1))
                .findAllRgReservationsForGivenTeam(Mockito.eq(resourceGroup1), Mockito.eq(team1));

        verify(maintenanceIntervalRepository, times(1)).findAllIntervalsInGivenTimePeriod(
                Mockito.eq(existingClusterId), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end()));
        verify(reservationRepository, times(1)).findRgReservations(
                Mockito.eq(resourceGroup1), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end()));

        verify(courseMetricRepository, times(1)).findAllByCourse(Mockito.eq(course));
        verify(reservationRepository, times(1))
                .findCourseReservations(Mockito.eq(course), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end()));

        verify(bankerAlgorithm, times(1)).process(Mockito.any(), Mockito.eq(courseReservationList),
                Mockito.eq(podStateful1.getResourceGroup()), Mockito.eq(cluster), Mockito.eq(hosts));

        verify(clusterMetricRepository, times(1)).findAllByClusterId(Mockito.eq(course.getClusterId()));
        verify(reservationRepository, times(1))
                .findClusterReservations(Mockito.eq(course.getClusterId()), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end()));

        verify(bankerAlgorithm, times(1)).process(Mockito.any(), Mockito.eq(clusterReservationList),
                Mockito.eq(podStateful1.getResourceGroup()), Mockito.eq(cluster), Mockito.eq(hosts));

        verify(reservationRepository, times(1)).saveAndFlush(reservation);
    }

    @Test
    public void Given_NonExistentClusterIdentifierIsExtractedFromCourse_When_CreateReservationForStatefulPod_Then_ThrowsException() {
        course.setClusterId(nonExistentClusterId);

        when(clusterService.findClusterById(Mockito.eq(nonExistentClusterId)))
                .thenThrow(new ClusterNotFoundException(nonExistentClusterId));

        assertThrows(ClusterNotFoundException.class,
                () -> reservationService.createReservationForStatefulPod(team1, podStateful1, createDto1));

        verify(clusterService, times(1))
                .findClusterById(Mockito.eq(nonExistentClusterId));
    }

    @Test
    public void Given_NonExistentClusterIsProvidedWhenFetchingHosts_When_CreateReservationForStatefulPod_Then_ThrowsException() {
        Cluster cluster = mock(Cluster.class);

        when(clusterService.findClusterById(Mockito.eq(existingClusterId))).thenReturn(cluster);
        when(clusterService.findAllHostsInCluster(Mockito.eq(cluster)))
                .thenThrow(new HostNotFoundException("No host could be found for cluster %s".formatted(existingClusterId)));

        assertThrows(HostNotFoundException.class,
                () -> reservationService.createReservationForStatefulPod(team1, podStateful1, createDto1));

        verify(clusterService, times(1)).findClusterById(Mockito.eq(existingClusterId));
        verify(clusterService, times(1)).findAllHostsInCluster(Mockito.eq(cluster));
    }

    @Test
    public void Given_ReservationStartIsInThePast_When_CreateReservationForStatefulPod_Then_ThrowsException() {
        Cluster cluster = mock(Cluster.class);
        Host host1 = mock(Host.class);
        Host host2 = mock(Host.class);
        List<Host> hosts = List.of(host1, host2);

        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        CreateReservationDto newCreateDto = new CreateReservationDto(
                currentTime.minusHours(4), currentTime.minusHours(1),
                reservation1.getAutomaticStartup(),
                reservation1.getNotificationTime()
        );

        when(clusterService.findClusterById(Mockito.eq(existingClusterId))).thenReturn(cluster);
        when(clusterService.findAllHostsInCluster(Mockito.eq(cluster))).thenReturn(hosts);

        assertThrows(ReservationStartInPastException.class,
                () -> reservationService.createReservationForStatefulPod(team1, podStateful1, newCreateDto));

        verify(clusterService, times(1)).findClusterById(Mockito.eq(existingClusterId));
        verify(clusterService, times(1)).findAllHostsInCluster(Mockito.eq(cluster));
    }

    @Test
    public void Given_ReservationStartIsAfterTheEnd_When_CreateReservationForStatefulPod_Then_ThrowsException() {
        Cluster cluster = mock(Cluster.class);
        Host host1 = mock(Host.class);
        Host host2 = mock(Host.class);
        List<Host> hosts = List.of(host1, host2);

        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        CreateReservationDto newCreateDto = new CreateReservationDto(
                currentTime.plusHours(2), currentTime,
                reservation1.getAutomaticStartup(),
                reservation1.getNotificationTime()
        );

        when(clusterService.findClusterById(Mockito.eq(existingClusterId))).thenReturn(cluster);
        when(clusterService.findAllHostsInCluster(Mockito.eq(cluster))).thenReturn(hosts);

        assertThrows(ReservationEndBeforeStartException.class,
                () -> reservationService.createReservationForStatefulPod(team1, podStateful1, newCreateDto));

        verify(clusterService, times(1)).findClusterById(Mockito.eq(existingClusterId));
        verify(clusterService, times(1)).findAllHostsInCluster(Mockito.eq(cluster));
    }

    @Test
    public void Given_ReservationLengthIsShorterThan2WindowLengths_When_CreateReservationForStatefulPod_Then_ThrowsException() {
        Cluster cluster = mock(Cluster.class);
        Host host1 = mock(Host.class);
        Host host2 = mock(Host.class);
        List<Host> hosts = List.of(host1, host2);

        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        CreateReservationDto newCreateDto = new CreateReservationDto(
                currentTime.plusHours(2), currentTime.plusHours(2).plusMinutes(2 * windowLength).minusSeconds(1),
                reservation1.getAutomaticStartup(), 5
        );

        when(clusterService.findClusterById(Mockito.eq(existingClusterId))).thenReturn(cluster);
        when(clusterService.findAllHostsInCluster(Mockito.eq(cluster))).thenReturn(hosts);

        assertThrows(ReservationTooShortException.class,
                () -> reservationService.createReservationForStatefulPod(team1, podStateful1, newCreateDto));

        verify(clusterService, times(1)).findClusterById(Mockito.eq(existingClusterId));
        verify(clusterService, times(1)).findAllHostsInCluster(Mockito.eq(cluster));
    }

    @Test
    public void Given_ReservationLengthIsLongerThanResourceGroupMaxRentTime_When_CreateReservationForStatefulPod_Then_ThrowsException() {
        Cluster cluster = mock(Cluster.class);
        Host host1 = mock(Host.class);
        Host host2 = mock(Host.class);
        List<Host> hosts = List.of(host1, host2);

        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        CreateReservationDto newCreateDto = new CreateReservationDto(
                currentTime.plusHours(2),
                currentTime.plusHours(2 + podStateful1.getResourceGroup().getMaxRentTime()).plusMinutes(windowLength).plusSeconds(1),
                reservation1.getAutomaticStartup(),
                reservation1.getNotificationTime()
        );

        when(clusterService.findClusterById(Mockito.eq(existingClusterId))).thenReturn(cluster);
        when(clusterService.findAllHostsInCluster(Mockito.eq(cluster))).thenReturn(hosts);

        assertThrows(ReservationMaxLengthExceededException.class,
                () -> reservationService.createReservationForStatefulPod(team1, podStateful1, newCreateDto));

        verify(clusterService, times(1)).findClusterById(Mockito.eq(existingClusterId));
        verify(clusterService, times(1)).findAllHostsInCluster(Mockito.eq(cluster));
    }

    @Test
    public void Given_ReservationCountIsGreaterThanResourceGroupMaxRentCount_When_CreateReservationForStatefulPod_Then_ThrowsException() {
        Cluster cluster = mock(Cluster.class);
        Host host1 = mock(Host.class);
        Host host2 = mock(Host.class);
        List<Host> hosts = List.of(host1, host2);

        resourceGroup1.setMaxRentTime(0);
        podStateful1.setMaxRent(1);

        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        CreateReservationDto newCreateDto = new CreateReservationDto(
                currentTime.plusHours(2), currentTime.plusHours(8),
                reservation1.getAutomaticStartup(),
                reservation1.getNotificationTime()
        );

        when(clusterService.findClusterById(Mockito.eq(existingClusterId))).thenReturn(cluster);
        when(clusterService.findAllHostsInCluster(Mockito.eq(cluster))).thenReturn(hosts);

        when(reservationRepository.findAllRgReservationsForGivenTeam(Mockito.eq(resourceGroup1), Mockito.eq(team1)))
                .thenReturn(List.of(reservation1, reservation3));

        assertThrows(ResourceGroupReservationCountExceededException.class,
                () -> reservationService.createReservationForStatefulPod(team1, podStateful1, newCreateDto));

        verify(clusterService, times(1)).findClusterById(Mockito.eq(existingClusterId));
        verify(clusterService, times(1)).findAllHostsInCluster(Mockito.eq(cluster));

        verify(reservationRepository, times(1))
                .findAllRgReservationsForGivenTeam(Mockito.eq(resourceGroup1), Mockito.eq(team1));
    }

    @Test
    public void Given_MaintenanceIntervalsExistDuringReservationTimeWindow_When_CreateReservationForStatefulPod_Then_ThrowsException() {
        Cluster cluster = mock(Cluster.class);
        Host host1 = mock(Host.class);
        Host host2 = mock(Host.class);
        List<Host> hosts = List.of(host1, host2);

        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        CreateReservationDto newCreateDto = new CreateReservationDto(
                currentTime.plusHours(2), currentTime.plusHours(8),
                reservation1.getAutomaticStartup(),
                reservation1.getNotificationTime()
        );

        when(clusterService.findClusterById(Mockito.eq(existingClusterId))).thenReturn(cluster);
        when(clusterService.findAllHostsInCluster(Mockito.eq(cluster))).thenReturn(hosts);

        when(reservationRepository.findAllRgReservationsForGivenTeam(Mockito.eq(resourceGroup1), Mockito.eq(team1)))
                .thenReturn(List.of());

        when(maintenanceIntervalRepository.findAllIntervalsInGivenTimePeriod(
                Mockito.eq(existingClusterId), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end())))
                .thenReturn(List.of(maintenanceInterval1, maintenanceInterval2));

        assertThrows(ReservationCreationException.class,
                () -> reservationService.createReservationForStatefulPod(team1, podStateful1, newCreateDto));

        verify(clusterService, times(1)).findClusterById(Mockito.eq(existingClusterId));
        verify(clusterService, times(1)).findAllHostsInCluster(Mockito.eq(cluster));

        verify(reservationRepository, times(1))
                .findAllRgReservationsForGivenTeam(Mockito.eq(resourceGroup1), Mockito.eq(team1));

        verify(maintenanceIntervalRepository, times(1)).findAllIntervalsInGivenTimePeriod(
                Mockito.eq(existingClusterId), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end()));
    }

    @Test
    public void Given_ReservationResourceGroupIsAlreadyReserved_When_CreateReservationForStatefulPod_Then_ThrowsException() {
        Cluster cluster = mock(Cluster.class);
        Host host1 = mock(Host.class);
        Host host2 = mock(Host.class);
        List<Host> hosts = List.of(host1, host2);

        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        CreateReservationDto newCreateDto = new CreateReservationDto(
                currentTime.plusHours(2), currentTime.plusHours(8),
                reservation1.getAutomaticStartup(),
                reservation1.getNotificationTime()
        );

        when(clusterService.findClusterById(Mockito.eq(existingClusterId))).thenReturn(cluster);
        when(clusterService.findAllHostsInCluster(Mockito.eq(cluster))).thenReturn(hosts);

        when(reservationRepository.findAllRgReservationsForGivenTeam(Mockito.eq(resourceGroup1), Mockito.eq(team1)))
                .thenReturn(List.of());

        when(maintenanceIntervalRepository.findAllIntervalsInGivenTimePeriod(
                Mockito.eq(existingClusterId), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end())))
                .thenReturn(List.of());
        when(reservationRepository.findRgReservations(
                Mockito.eq(resourceGroup1), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end())))
                .thenReturn(List.of(reservation1));

        assertThrows(ResourceGroupAlreadyReservedException.class,
                () -> reservationService.createReservationForStatefulPod(team1, podStateful1, newCreateDto));

        verify(clusterService, times(1)).findClusterById(Mockito.eq(existingClusterId));
        verify(clusterService, times(1)).findAllHostsInCluster(Mockito.eq(cluster));

        verify(reservationRepository, times(1))
                .findAllRgReservationsForGivenTeam(Mockito.eq(resourceGroup1), Mockito.eq(team1));

        verify(maintenanceIntervalRepository, times(1)).findAllIntervalsInGivenTimePeriod(
                Mockito.eq(existingClusterId), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end()));
        verify(reservationRepository, times(1)).findRgReservations(
                Mockito.eq(resourceGroup1), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end()));
    }

    @Test
    public void Given_CourseHasInsufficientResourcesForGivenReservation_When_CreateReservationForStatefulPod_Then_ThrowsException() {
        Cluster cluster = mock(Cluster.class);
        Host host1 = mock(Host.class);
        Host host2 = mock(Host.class);
        List<Host> hosts = List.of(host1, host2);

        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        CreateReservationDto newCreateDto = new CreateReservationDto(
                currentTime.plusHours(2), currentTime.plusHours(8),
                reservation1.getAutomaticStartup(),
                reservation1.getNotificationTime()
        );

        when(clusterService.findClusterById(Mockito.eq(existingClusterId))).thenReturn(cluster);
        when(clusterService.findAllHostsInCluster(Mockito.eq(cluster))).thenReturn(hosts);

        when(reservationRepository.findAllRgReservationsForGivenTeam(Mockito.eq(resourceGroup1), Mockito.eq(team1)))
                .thenReturn(List.of());

        when(maintenanceIntervalRepository.findAllIntervalsInGivenTimePeriod(
                Mockito.eq(existingClusterId), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end())))
                .thenReturn(List.of());
        when(reservationRepository.findRgReservations(
                Mockito.eq(resourceGroup1), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end())))
                .thenReturn(List.of());

        List<Reservation> courseReservationList = List.of(reservation1);
        when(courseMetricRepository.findAllByCourse(Mockito.eq(course))).thenReturn(courseMetrics);
        when(reservationRepository.findCourseReservations(Mockito.eq(course), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end())))
                .thenReturn(courseReservationList);

        when(bankerAlgorithm.process(Mockito.any(), Mockito.eq(courseReservationList),
                Mockito.eq(podStateful1.getResourceGroup()), Mockito.eq(cluster), Mockito.eq(hosts))).thenReturn(false);

        assertThrows(CourseInsufficientResourcesException.class,
                () -> reservationService.createReservationForStatefulPod(team1, podStateful1, newCreateDto));

        verify(clusterService, times(1)).findClusterById(Mockito.eq(existingClusterId));
        verify(clusterService, times(1)).findAllHostsInCluster(Mockito.eq(cluster));

        verify(reservationRepository, times(1))
                .findAllRgReservationsForGivenTeam(Mockito.eq(resourceGroup1), Mockito.eq(team1));

        verify(maintenanceIntervalRepository, times(1)).findAllIntervalsInGivenTimePeriod(
                Mockito.eq(existingClusterId), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end()));
        verify(reservationRepository, times(1)).findRgReservations(
                Mockito.eq(resourceGroup1), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end()));

        verify(courseMetricRepository, times(1)).findAllByCourse(Mockito.eq(course));
        verify(reservationRepository, times(1))
                .findCourseReservations(Mockito.eq(course), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end()));

        verify(bankerAlgorithm, times(1)).process(Mockito.any(), Mockito.eq(courseReservationList),
                Mockito.eq(podStateful1.getResourceGroup()), Mockito.eq(cluster), Mockito.eq(hosts));
    }

    @Test
    public void Given_ClusterHasInsufficientResourcesForGivenReservation_When_CreateReservationForStatefulPod_Then_ThrowsException() {
        Cluster cluster = mock(Cluster.class);
        Host host1 = mock(Host.class);
        Host host2 = mock(Host.class);
        List<Host> hosts = List.of(host1, host2);

        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        CreateReservationDto newCreateDto = new CreateReservationDto(
                currentTime.plusHours(2), currentTime.plusHours(8),
                reservation1.getAutomaticStartup(),
                reservation1.getNotificationTime()
        );

        when(cluster.id()).thenReturn(UUID.randomUUID().toString());

        when(clusterService.findClusterById(Mockito.eq(existingClusterId))).thenReturn(cluster);
        when(clusterService.findAllHostsInCluster(Mockito.eq(cluster))).thenReturn(hosts);

        when(reservationRepository.findAllRgReservationsForGivenTeam(Mockito.eq(resourceGroup1), Mockito.eq(team1)))
                .thenReturn(List.of());

        when(maintenanceIntervalRepository.findAllIntervalsInGivenTimePeriod(
                Mockito.eq(existingClusterId), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end())))
                .thenReturn(List.of());
        when(reservationRepository.findRgReservations(
                Mockito.eq(resourceGroup1), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end())))
                .thenReturn(List.of());

        List<Reservation> courseReservationList = List.of(reservation1);
        when(courseMetricRepository.findAllByCourse(Mockito.eq(course))).thenReturn(courseMetrics);
        when(reservationRepository.findCourseReservations(Mockito.eq(course), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end())))
                .thenReturn(courseReservationList);

        when(bankerAlgorithm.process(Mockito.any(), Mockito.eq(courseReservationList),
                Mockito.eq(podStateful1.getResourceGroup()), Mockito.eq(cluster), Mockito.eq(hosts))).thenReturn(true);

        List<Reservation> clusterReservationList = List.of(reservation1, reservation2);
        when(clusterMetricRepository.findAllByClusterId(Mockito.eq(course.getClusterId()))).thenReturn(clusterMetrics);
        when(reservationRepository.findClusterReservations(Mockito.eq(course.getClusterId()), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end())))
                .thenReturn(clusterReservationList);

        when(bankerAlgorithm.process(Mockito.any(), Mockito.eq(clusterReservationList),
                Mockito.eq(podStateful1.getResourceGroup()), Mockito.eq(cluster), Mockito.eq(hosts))).thenReturn(false);

        assertThrows(ClusterInsufficientResourcesException.class,
                () -> reservationService.createReservationForStatefulPod(team1, podStateful1, newCreateDto));

        verify(clusterService, times(1)).findClusterById(Mockito.eq(existingClusterId));
        verify(clusterService, times(1)).findAllHostsInCluster(Mockito.eq(cluster));

        verify(reservationRepository, times(1))
                .findAllRgReservationsForGivenTeam(Mockito.eq(resourceGroup1), Mockito.eq(team1));

        verify(maintenanceIntervalRepository, times(1)).findAllIntervalsInGivenTimePeriod(
                Mockito.eq(existingClusterId), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end()));
        verify(reservationRepository, times(1)).findRgReservations(
                Mockito.eq(resourceGroup1), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end()));

        verify(courseMetricRepository, times(1)).findAllByCourse(Mockito.eq(course));
        verify(reservationRepository, times(1))
                .findCourseReservations(Mockito.eq(course), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end()));

        verify(bankerAlgorithm, times(1)).process(Mockito.any(), Mockito.eq(courseReservationList),
                Mockito.eq(podStateful1.getResourceGroup()), Mockito.eq(cluster), Mockito.eq(hosts));

        verify(clusterMetricRepository, times(1)).findAllByClusterId(Mockito.eq(course.getClusterId()));
        verify(reservationRepository, times(1))
                .findClusterReservations(Mockito.eq(course.getClusterId()), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end()));

        verify(bankerAlgorithm, times(1)).process(Mockito.any(), Mockito.eq(clusterReservationList),
                Mockito.eq(podStateful1.getResourceGroup()), Mockito.eq(cluster), Mockito.eq(hosts));
    }

    /* CreateReservationForStatelessPod method tests */

    @Test
    public void Given_AllDataInReservationCreateDtoIsValid_When_CreateReservationForStatelessPod_Then_CreatesNewReservationSuccessfully() {
        Cluster cluster = mock(Cluster.class);
        Host host1 = mock(Host.class);
        Host host2 = mock(Host.class);
        List<Host> hosts = List.of(host1, host2);

        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        CreateReservationDto newCreateDto = new CreateReservationDto(
                currentTime.plusHours(2),
                currentTime.plusHours(6),
                reservation1.getAutomaticStartup(),
                reservation1.getNotificationTime()
        );

        Reservation reservation = new Reservation(
                resourceGroup3, team1, newCreateDto.start(), newCreateDto.end(),
                reservation1.getAutomaticStartup(), reservation1.getNotificationTime()
        );

        when(clusterService.findClusterById(Mockito.eq(course.getClusterId()))).thenReturn(cluster);
        when(clusterService.findAllHostsInCluster(Mockito.eq(cluster))).thenReturn(hosts);

        when(reservationRepository.findAllRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1))).thenReturn(List.of());

        when(reservationRepository.findRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1),
                Mockito.eq(newCreateDto.start().minusHours(podStateless1.getResourceGroupPool().getGracePeriod())),
                Mockito.eq(newCreateDto.start())))
                .thenReturn(List.of());

        when(reservationRepository.findRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1), Mockito.eq(newCreateDto.end()),
                Mockito.eq(newCreateDto.end().plusHours(podStateless1.getResourceGroupPool().getGracePeriod()))))
                .thenReturn(List.of());

        when(maintenanceIntervalRepository.findAllIntervalsInGivenTimePeriod(
                Mockito.eq(course.getClusterId()), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end())))
                .thenReturn(List.of());

        List<Reservation> courseReservationList = List.of(reservation1);
        when(courseMetricRepository.findAllByCourse(Mockito.eq(course))).thenReturn(courseMetrics);
        when(reservationRepository.findCourseReservations(Mockito.eq(course), Mockito.eq(newCreateDto.start()),
                Mockito.eq(newCreateDto.end()))).thenReturn(courseReservationList);

        // when(metricUtil.extractCourseMetricValues(Mockito.eq(courseMetrics))).thenReturn(courseMetricMap);
        when(bankerAlgorithm.process(Mockito.any(), Mockito.eq(courseReservationList), Mockito.eq(resourceGroup3),
                Mockito.eq(cluster), Mockito.eq(hosts))).thenReturn(true);

        List<Reservation> clusterReservationList = List.of(reservation1, reservation2);
        when(clusterMetricRepository.findAllByClusterId(Mockito.eq(course.getClusterId()))).thenReturn(clusterMetrics);
        when(reservationRepository.findClusterReservations(Mockito.eq(course.getClusterId()), Mockito.eq(newCreateDto.start()),
                Mockito.eq(newCreateDto.end()))).thenReturn(clusterReservationList);

        // when(metricUtil.extractClusterMetricValues(Mockito.eq(clusterMetrics))).thenReturn(clusterMetricMap);
        when(bankerAlgorithm.process(Mockito.any(), Mockito.eq(clusterReservationList), Mockito.eq(resourceGroup3),
                Mockito.eq(cluster), Mockito.eq(hosts))).thenReturn(true);

        when(reservationRepository.findRgReservations(Mockito.eq(resourceGroup3), Mockito.eq(newCreateDto.start()),
                Mockito.eq(newCreateDto.end()))).thenReturn(List.of());

        when(reservationRepository.saveAndFlush(Mockito.eq(reservation))).thenReturn(reservation);

        reservationService.createReservationForStatelessPod(team1, podStateless1, newCreateDto);

        verify(clusterService, times(1)).findClusterById(Mockito.eq(course.getClusterId()));
        verify(clusterService, times(1)).findAllHostsInCluster(Mockito.eq(cluster));

        verify(reservationRepository, times(1)).findAllRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1));

        verify(reservationRepository, times(1)).findRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1),
                Mockito.eq(newCreateDto.start().minusHours(podStateless1.getResourceGroupPool().getGracePeriod())),
                Mockito.eq(newCreateDto.start()));

        verify(reservationRepository, times(1)).findRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1), Mockito.eq(newCreateDto.end()),
                Mockito.eq(newCreateDto.end().plusHours(podStateless1.getResourceGroupPool().getGracePeriod())));

        verify(maintenanceIntervalRepository, times(1)).findAllIntervalsInGivenTimePeriod(
                Mockito.eq(course.getClusterId()), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end()));

        verify(courseMetricRepository, times(1)).findAllByCourse(Mockito.eq(course));
        verify(reservationRepository, times(1)).findCourseReservations(
                Mockito.eq(course), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end()));

        // verify(metricUtil, times(1)).extractCourseMetricValues(Mockito.eq(courseMetrics));
        verify(bankerAlgorithm, times(1)).process(Mockito.any(),
                Mockito.eq(courseReservationList), Mockito.eq(resourceGroup3), Mockito.eq(cluster), Mockito.eq(hosts));

        verify(clusterMetricRepository, times(1)).findAllByClusterId(Mockito.eq(course.getClusterId()));
        verify(reservationRepository, times(1)).findClusterReservations(
                Mockito.eq(course.getClusterId()), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end()));

        // verify(metricUtil, times(1)).extractClusterMetricValues(Mockito.eq(clusterMetrics));
        verify(bankerAlgorithm, times(1)).process(Mockito.any(),
                Mockito.eq(clusterReservationList), Mockito.eq(resourceGroup3), Mockito.eq(cluster), Mockito.eq(hosts));

        verify(reservationRepository, times(1)).findRgReservations(Mockito.eq(resourceGroup3),
                Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end()));

        verify(reservationRepository, times(1)).saveAndFlush(Mockito.eq(reservation));
    }

    @Test
    public void Given_AllDataInReservationCreateDtoIsValidAndLimitsAreEqualsTo0_When_CreateReservationForStatelessPod_Then_CreatesNewReservationSuccessfully() {
        Cluster cluster = mock(Cluster.class);
        Host host1 = mock(Host.class);
        Host host2 = mock(Host.class);
        List<Host> hosts = List.of(host1, host2);

        resourceGroupPool1.setMaxRentTime(0);
        resourceGroupPool1.setMaxRent(0);
        resourceGroupPool1.setGracePeriod(0);

        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        CreateReservationDto newCreateDto = new CreateReservationDto(
                currentTime.plusHours(2),
                currentTime.plusHours(6),
                reservation1.getAutomaticStartup(),
                reservation1.getNotificationTime()
        );

        Reservation reservation = new Reservation(
                resourceGroup3, team1, newCreateDto.start(), newCreateDto.end(),
                reservation1.getAutomaticStartup(), reservation1.getNotificationTime()
        );

        when(clusterService.findClusterById(Mockito.eq(course.getClusterId()))).thenReturn(cluster);
        when(clusterService.findAllHostsInCluster(Mockito.eq(cluster))).thenReturn(hosts);

        when(reservationRepository.findAllRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1))).thenReturn(List.of());

        when(reservationRepository.findRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1),
                Mockito.eq(newCreateDto.start().minusHours(podStateless1.getResourceGroupPool().getGracePeriod())),
                Mockito.eq(newCreateDto.start())))
                .thenReturn(List.of());

        when(reservationRepository.findRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1), Mockito.eq(newCreateDto.end()),
                Mockito.eq(newCreateDto.end().plusHours(podStateless1.getResourceGroupPool().getGracePeriod()))))
                .thenReturn(List.of());

        when(maintenanceIntervalRepository.findAllIntervalsInGivenTimePeriod(
                Mockito.eq(course.getClusterId()), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end())))
                .thenReturn(List.of());

        List<Reservation> courseReservationList = List.of(reservation1);
        when(courseMetricRepository.findAllByCourse(Mockito.eq(course))).thenReturn(courseMetrics);
        when(reservationRepository.findCourseReservations(Mockito.eq(course), Mockito.eq(newCreateDto.start()),
                Mockito.eq(newCreateDto.end()))).thenReturn(courseReservationList);

        // when(metricUtil.extractCourseMetricValues(Mockito.eq(courseMetrics))).thenReturn(courseMetricMap);
        when(bankerAlgorithm.process(Mockito.any(),
                Mockito.eq(courseReservationList), Mockito.eq(resourceGroup3), Mockito.eq(cluster), Mockito.eq(hosts)))
                .thenReturn(true);

        List<Reservation> clusterReservationList = List.of(reservation1, reservation2);
        when(clusterMetricRepository.findAllByClusterId(Mockito.eq(course.getClusterId()))).thenReturn(clusterMetrics);
        when(reservationRepository.findClusterReservations(Mockito.eq(course.getClusterId()), Mockito.eq(newCreateDto.start()),
                Mockito.eq(newCreateDto.end()))).thenReturn(clusterReservationList);

        // when(metricUtil.extractClusterMetricValues(Mockito.eq(clusterMetrics))).thenReturn(clusterMetricMap);
        when(bankerAlgorithm.process(Mockito.any(),
                Mockito.eq(clusterReservationList), Mockito.eq(resourceGroup3), Mockito.eq(cluster), Mockito.eq(hosts)))
                .thenReturn(true);

        when(reservationRepository.findRgReservations(Mockito.eq(resourceGroup3), Mockito.eq(newCreateDto.start()),
                Mockito.eq(newCreateDto.end()))).thenReturn(List.of());

        when(reservationRepository.saveAndFlush(Mockito.eq(reservation))).thenReturn(reservation);

        reservationService.createReservationForStatelessPod(team1, podStateless1, newCreateDto);

        verify(clusterService, times(1)).findClusterById(Mockito.eq(course.getClusterId()));
        verify(clusterService, times(1)).findAllHostsInCluster(Mockito.eq(cluster));

        verify(reservationRepository, times(1)).findAllRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1));

        verify(reservationRepository, times(1)).findRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1),
                Mockito.eq(newCreateDto.start().minusHours(podStateless1.getResourceGroupPool().getGracePeriod())),
                Mockito.eq(newCreateDto.start()));

        verify(reservationRepository, times(1)).findRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1), Mockito.eq(newCreateDto.end()),
                Mockito.eq(newCreateDto.end().plusHours(podStateless1.getResourceGroupPool().getGracePeriod())));

        verify(maintenanceIntervalRepository, times(1)).findAllIntervalsInGivenTimePeriod(
                Mockito.eq(course.getClusterId()), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end()));

        verify(courseMetricRepository, times(1)).findAllByCourse(Mockito.eq(course));
        verify(reservationRepository, times(1)).findCourseReservations(
                Mockito.eq(course), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end()));

        // verify(metricUtil, times(1)).extractCourseMetricValues(Mockito.eq(courseMetrics));
        verify(bankerAlgorithm, times(1)).process(Mockito.any(),
                Mockito.eq(courseReservationList), Mockito.eq(resourceGroup3), Mockito.eq(cluster), Mockito.eq(hosts));

        verify(clusterMetricRepository, times(1)).findAllByClusterId(Mockito.eq(course.getClusterId()));
        verify(reservationRepository, times(1)).findClusterReservations(
                Mockito.eq(course.getClusterId()), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end()));

        // verify(metricUtil, times(1)).extractClusterMetricValues(Mockito.eq(clusterMetrics));
        verify(bankerAlgorithm, times(1)).process(Mockito.any(),
                Mockito.eq(clusterReservationList), Mockito.eq(resourceGroup3), Mockito.eq(cluster), Mockito.eq(hosts));

        verify(reservationRepository, times(1)).findRgReservations(Mockito.eq(resourceGroup3),
                Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end()));

        verify(reservationRepository, times(1)).saveAndFlush(Mockito.eq(reservation));
    }

    @Test
    public void Given_NonExistentClusterIdentifierIsExtractedFromCourse_When_CreateReservationForStatelessPod_Then_ThrowsException() {
        course.setClusterId(nonExistentClusterId);

        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        CreateReservationDto newCreateDto = new CreateReservationDto(
                currentTime.plusHours(2), currentTime.plusHours(8),
                reservation1.getAutomaticStartup(),
                reservation1.getNotificationTime()
        );

        when(clusterService.findClusterById(Mockito.eq(nonExistentClusterId)))
                .thenThrow(new ClusterNotFoundException(nonExistentClusterId));

        assertThrows(ClusterNotFoundException.class,
                () -> reservationService.createReservationForStatelessPod(team1, podStateless1, newCreateDto));

        verify(clusterService, times(1)).findClusterById(Mockito.eq(nonExistentClusterId));
    }

    @Test
    public void Given_NonExistentClusterIsProvidedWhenFetchingHosts_When_CreateReservationForStatelessPod_Then_ThrowsException() {
        Cluster cluster = mock(Cluster.class);
        Host host1 = mock(Host.class);
        Host host2 = mock(Host.class);
        List<Host> hosts = List.of(host1, host2);

        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        CreateReservationDto newCreateDto = new CreateReservationDto(
                currentTime.plusHours(2), currentTime.plusHours(8),
                reservation1.getAutomaticStartup(),
                reservation1.getNotificationTime()
        );

        when(clusterService.findClusterById(Mockito.eq(course.getClusterId()))).thenReturn(cluster);
        when(clusterService.findAllHostsInCluster(Mockito.eq(cluster))).thenThrow(
                new HostNotFoundException("No host could be found for cluster %s".formatted(course.getClusterId())));

        assertThrows(HostNotFoundException.class,
                () -> reservationService.createReservationForStatelessPod(team1, podStateless1, newCreateDto));

        verify(clusterService, times(1)).findClusterById(Mockito.eq(course.getClusterId()));
        verify(clusterService, times(1)).findAllHostsInCluster(Mockito.eq(cluster));
    }

    @Test
    public void Given_ReservationStartIsInThePast_When_CreateReservationForStatelessPod_Then_ThrowsException() {
        Cluster cluster = mock(Cluster.class);
        Host host1 = mock(Host.class);
        Host host2 = mock(Host.class);
        List<Host> hosts = List.of(host1, host2);

        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        CreateReservationDto newCreateDto = new CreateReservationDto(
                currentTime.minusHours(4), currentTime.plusHours(2),
                reservation1.getAutomaticStartup(),
                reservation1.getNotificationTime()
        );

        when(clusterService.findClusterById(Mockito.eq(course.getClusterId()))).thenReturn(cluster);
        when(clusterService.findAllHostsInCluster(Mockito.eq(cluster))).thenReturn(hosts);

        assertThrows(ReservationStartInPastException.class,
                () -> reservationService.createReservationForStatelessPod(team1, podStateless1, newCreateDto));

        verify(clusterService, times(1)).findClusterById(Mockito.eq(course.getClusterId()));
        verify(clusterService, times(1)).findAllHostsInCluster(Mockito.eq(cluster));
    }

    @Test
    public void Given_ReservationStartIsAfterTheEnd_When_CreateReservationForStatelessPod_Then_ThrowsException() {
        Cluster cluster = mock(Cluster.class);
        Host host1 = mock(Host.class);
        Host host2 = mock(Host.class);
        List<Host> hosts = List.of(host1, host2);

        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        CreateReservationDto newCreateDto = new CreateReservationDto(
                currentTime.plusHours(2), currentTime.minusHours(2),
                reservation1.getAutomaticStartup(),
                reservation1.getNotificationTime()
        );

        when(clusterService.findClusterById(Mockito.eq(course.getClusterId()))).thenReturn(cluster);
        when(clusterService.findAllHostsInCluster(Mockito.eq(cluster))).thenReturn(hosts);

        assertThrows(ReservationEndBeforeStartException.class,
                () -> reservationService.createReservationForStatelessPod(team1, podStateless1, newCreateDto));

        verify(clusterService, times(1)).findClusterById(Mockito.eq(course.getClusterId()));
        verify(clusterService, times(1)).findAllHostsInCluster(Mockito.eq(cluster));
    }

    @Test
    public void Given_ReservationLengthIsShorterThan2WindowLengths_When_CreateReservationForStatelessPod_Then_ThrowsException() {
        Cluster cluster = mock(Cluster.class);
        Host host1 = mock(Host.class);
        Host host2 = mock(Host.class);
        List<Host> hosts = List.of(host1, host2);

        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        CreateReservationDto newCreateDto = new CreateReservationDto(
                currentTime.plusHours(2), currentTime.plusHours(2).plusMinutes(2 * windowLength).minusSeconds(1),
                reservation1.getAutomaticStartup(), 5
        );

        when(clusterService.findClusterById(Mockito.eq(course.getClusterId()))).thenReturn(cluster);
        when(clusterService.findAllHostsInCluster(Mockito.eq(cluster))).thenReturn(hosts);

        assertThrows(ReservationTooShortException.class,
                () -> reservationService.createReservationForStatelessPod(team1, podStateless1, newCreateDto));

        verify(clusterService, times(1)).findClusterById(Mockito.eq(course.getClusterId()));
        verify(clusterService, times(1)).findAllHostsInCluster(Mockito.eq(cluster));
    }

    @Test
    public void Given_ReservationLengthIsLongerThanResourceGroupPoolMaxRentTime_When_CreateReservationForStatelessPod_Then_ThrowsException() {
        Cluster cluster = mock(Cluster.class);
        Host host1 = mock(Host.class);
        Host host2 = mock(Host.class);
        List<Host> hosts = List.of(host1, host2);

        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        CreateReservationDto newCreateDto = new CreateReservationDto(
                currentTime.plusHours(2),
                currentTime.plusHours(2 + podStateless1.getResourceGroupPool().getMaxRentTime()).plusMinutes(windowLength).plusSeconds(1),
                reservation1.getAutomaticStartup(),
                reservation1.getNotificationTime()
        );

        when(clusterService.findClusterById(Mockito.eq(course.getClusterId()))).thenReturn(cluster);
        when(clusterService.findAllHostsInCluster(Mockito.eq(cluster))).thenReturn(hosts);

        assertThrows(ReservationMaxLengthExceededException.class,
                () -> reservationService.createReservationForStatelessPod(team1, podStateless1, newCreateDto));

        verify(clusterService, times(1)).findClusterById(Mockito.eq(course.getClusterId()));
        verify(clusterService, times(1)).findAllHostsInCluster(Mockito.eq(cluster));
    }

    @Test
    public void Given_ReservationCountIsGreaterThanResourceGroupPoolMaxRentCount_When_CreateReservationForStatelessPod_Then_ThrowsException() {
        Cluster cluster = mock(Cluster.class);
        Host host1 = mock(Host.class);
        Host host2 = mock(Host.class);
        List<Host> hosts = List.of(host1, host2);

        resourceGroupPool1.setMaxRent(1);

        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        CreateReservationDto newCreateDto = new CreateReservationDto(
                currentTime.plusHours(2),
                currentTime.plusHours(6),
                reservation1.getAutomaticStartup(),
                reservation1.getNotificationTime()
        );

        when(clusterService.findClusterById(Mockito.eq(course.getClusterId()))).thenReturn(cluster);
        when(clusterService.findAllHostsInCluster(Mockito.eq(cluster))).thenReturn(hosts);

        when(reservationRepository.findAllRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1))).thenReturn(List.of(reservation1));

        assertThrows(ResourceGroupReservationCountExceededException.class,
                () -> reservationService.createReservationForStatelessPod(team1, podStateless1, newCreateDto));

        verify(clusterService, times(1)).findClusterById(Mockito.eq(course.getClusterId()));
        verify(clusterService, times(1)).findAllHostsInCluster(Mockito.eq(cluster));

        verify(reservationRepository, times(1)).findAllRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1));
    }

    @Test
    public void Given_ReservationGracePeriodDidNotFinishSinceTheLastReservation_When_CreateReservationForStatelessPod_Then_ThrowsException() {
        Cluster cluster = mock(Cluster.class);
        Host host1 = mock(Host.class);
        Host host2 = mock(Host.class);
        List<Host> hosts = List.of(host1, host2);

        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        CreateReservationDto newCreateDto = new CreateReservationDto(
                currentTime.plusHours(2),
                currentTime.plusHours(6),
                reservation1.getAutomaticStartup(),
                reservation1.getNotificationTime()
        );

        when(clusterService.findClusterById(Mockito.eq(course.getClusterId()))).thenReturn(cluster);
        when(clusterService.findAllHostsInCluster(Mockito.eq(cluster))).thenReturn(hosts);

        when(reservationRepository.findAllRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1))).thenReturn(List.of());

        when(reservationRepository.findRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1),
                Mockito.eq(newCreateDto.start().minusHours(podStateless1.getResourceGroupPool().getGracePeriod())),
                Mockito.eq(newCreateDto.start())))
                .thenReturn(List.of(reservation1));

        when(reservationRepository.findRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1), Mockito.eq(newCreateDto.end()),
                Mockito.eq(newCreateDto.end().plusHours(podStateless1.getResourceGroupPool().getGracePeriod()))))
                .thenReturn(List.of());

        assertThrows(ReservationGracePeriodNotFinishedException.class,
                () -> reservationService.createReservationForStatelessPod(team1, podStateless1, newCreateDto));

        verify(clusterService, times(1)).findClusterById(Mockito.eq(course.getClusterId()));
        verify(clusterService, times(1)).findAllHostsInCluster(Mockito.eq(cluster));

        verify(reservationRepository, times(1)).findAllRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1));

        verify(reservationRepository, times(1)).findRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1),
                Mockito.eq(newCreateDto.start().minusHours(podStateless1.getResourceGroupPool().getGracePeriod())),
                Mockito.eq(newCreateDto.start()));

        verify(reservationRepository, times(1)).findRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1), Mockito.eq(newCreateDto.end()),
                Mockito.eq(newCreateDto.end().plusHours(podStateless1.getResourceGroupPool().getGracePeriod())));
    }

    @Test
    public void Given_ReservationGracePeriodCouldNotFinishBeforeTheNextReservation_When_CreateReservationForStatelessPod_Then_ThrowsException() {
        Cluster cluster = mock(Cluster.class);
        Host host1 = mock(Host.class);
        Host host2 = mock(Host.class);
        List<Host> hosts = List.of(host1, host2);

        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        CreateReservationDto newCreateDto = new CreateReservationDto(
                currentTime.plusHours(2),
                currentTime.plusHours(6),
                reservation1.getAutomaticStartup(),
                reservation1.getNotificationTime()
        );

        when(clusterService.findClusterById(Mockito.eq(course.getClusterId()))).thenReturn(cluster);
        when(clusterService.findAllHostsInCluster(Mockito.eq(cluster))).thenReturn(hosts);

        when(reservationRepository.findAllRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1))).thenReturn(List.of());

        when(reservationRepository.findRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1),
                Mockito.eq(newCreateDto.start().minusHours(podStateless1.getResourceGroupPool().getGracePeriod())),
                Mockito.eq(newCreateDto.start())))
                .thenReturn(List.of());

        when(reservationRepository.findRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1), Mockito.eq(newCreateDto.end()),
                Mockito.eq(newCreateDto.end().plusHours(podStateless1.getResourceGroupPool().getGracePeriod()))))
                .thenReturn(List.of(reservation1));

        assertThrows(ReservationGracePeriodCouldNotFinishException.class,
                () -> reservationService.createReservationForStatelessPod(team1, podStateless1, newCreateDto));

        verify(clusterService, times(1)).findClusterById(Mockito.eq(course.getClusterId()));
        verify(clusterService, times(1)).findAllHostsInCluster(Mockito.eq(cluster));

        verify(reservationRepository, times(1)).findAllRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1));

        verify(reservationRepository, times(1)).findRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1),
                Mockito.eq(newCreateDto.start().minusHours(podStateless1.getResourceGroupPool().getGracePeriod())),
                Mockito.eq(newCreateDto.start()));

        verify(reservationRepository, times(1)).findRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1), Mockito.eq(newCreateDto.end()),
                Mockito.eq(newCreateDto.end().plusHours(podStateless1.getResourceGroupPool().getGracePeriod())));
    }

    @Test
    public void Given_MaintenanceIntervalsExistDuringReservationTimeWindow_When_CreateReservationForStatelessPod_Then_ThrowsException() {
        Cluster cluster = mock(Cluster.class);
        Host host1 = mock(Host.class);
        Host host2 = mock(Host.class);
        List<Host> hosts = List.of(host1, host2);

        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        CreateReservationDto newCreateDto = new CreateReservationDto(
                currentTime.plusHours(2),
                currentTime.plusHours(6),
                reservation1.getAutomaticStartup(),
                reservation1.getNotificationTime()
        );

        when(clusterService.findClusterById(Mockito.eq(course.getClusterId()))).thenReturn(cluster);
        when(clusterService.findAllHostsInCluster(Mockito.eq(cluster))).thenReturn(hosts);

        when(reservationRepository.findAllRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1))).thenReturn(List.of());

        when(reservationRepository.findRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1),
                Mockito.eq(newCreateDto.start().minusHours(podStateless1.getResourceGroupPool().getGracePeriod())),
                Mockito.eq(newCreateDto.start())))
                .thenReturn(List.of());

        when(reservationRepository.findRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1), Mockito.eq(newCreateDto.end()),
                Mockito.eq(newCreateDto.end().plusHours(podStateless1.getResourceGroupPool().getGracePeriod()))))
                .thenReturn(List.of());

        when(maintenanceIntervalRepository.findAllIntervalsInGivenTimePeriod(
                Mockito.eq(course.getClusterId()), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end())))
                .thenReturn(List.of(maintenanceInterval1, maintenanceInterval2));

        assertThrows(ReservationCreationException.class,
                () -> reservationService.createReservationForStatelessPod(team1, podStateless1, newCreateDto));

        verify(clusterService, times(1)).findClusterById(Mockito.eq(course.getClusterId()));
        verify(clusterService, times(1)).findAllHostsInCluster(Mockito.eq(cluster));

        verify(reservationRepository, times(1)).findAllRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1));

        verify(reservationRepository, times(1)).findRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1),
                Mockito.eq(newCreateDto.start().minusHours(podStateless1.getResourceGroupPool().getGracePeriod())),
                Mockito.eq(newCreateDto.start()));

        verify(reservationRepository, times(1)).findRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1), Mockito.eq(newCreateDto.end()),
                Mockito.eq(newCreateDto.end().plusHours(podStateless1.getResourceGroupPool().getGracePeriod())));

        verify(maintenanceIntervalRepository, times(1)).findAllIntervalsInGivenTimePeriod(
                Mockito.eq(course.getClusterId()), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end()));
    }

    @Test
    public void Given_CourseHasInsufficientResourcesForGivenReservation_When_CreateReservationForStatelessPod_Then_ThrowsException() {
        Cluster cluster = mock(Cluster.class);
        Host host1 = mock(Host.class);
        Host host2 = mock(Host.class);
        List<Host> hosts = List.of(host1, host2);

        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        CreateReservationDto newCreateDto = new CreateReservationDto(
                currentTime.plusHours(2),
                currentTime.plusHours(6),
                reservation1.getAutomaticStartup(),
                reservation1.getNotificationTime()
        );

        when(clusterService.findClusterById(Mockito.eq(course.getClusterId()))).thenReturn(cluster);
        when(clusterService.findAllHostsInCluster(Mockito.eq(cluster))).thenReturn(hosts);

        when(reservationRepository.findAllRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1))).thenReturn(List.of());

        when(reservationRepository.findRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1),
                Mockito.eq(newCreateDto.start().minusHours(podStateless1.getResourceGroupPool().getGracePeriod())),
                Mockito.eq(newCreateDto.start())))
                .thenReturn(List.of());

        when(reservationRepository.findRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1), Mockito.eq(newCreateDto.end()),
                Mockito.eq(newCreateDto.end().plusHours(podStateless1.getResourceGroupPool().getGracePeriod()))))
                .thenReturn(List.of());

        when(maintenanceIntervalRepository.findAllIntervalsInGivenTimePeriod(
                Mockito.eq(course.getClusterId()), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end())))
                .thenReturn(List.of());

        List<Reservation> courseReservationList = List.of(reservation1);
        when(courseMetricRepository.findAllByCourse(Mockito.eq(course))).thenReturn(courseMetrics);
        when(reservationRepository.findCourseReservations(Mockito.eq(course), Mockito.eq(newCreateDto.start()),
                Mockito.eq(newCreateDto.end()))).thenReturn(courseReservationList);

        when(bankerAlgorithm.process(Mockito.any(), Mockito.eq(courseReservationList),
                Mockito.eq(resourceGroup3), Mockito.eq(cluster), Mockito.eq(hosts))).thenReturn(false);

        assertThrows(ReservationCreationException.class,
                () -> reservationService.createReservationForStatelessPod(team1, podStateless1, newCreateDto));

        verify(clusterService, times(1)).findClusterById(Mockito.eq(course.getClusterId()));
        verify(clusterService, times(1)).findAllHostsInCluster(Mockito.eq(cluster));

        verify(reservationRepository, times(1)).findAllRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1));

        verify(reservationRepository, times(1)).findRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1),
                Mockito.eq(newCreateDto.start().minusHours(podStateless1.getResourceGroupPool().getGracePeriod())),
                Mockito.eq(newCreateDto.start()));

        verify(reservationRepository, times(1)).findRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1), Mockito.eq(newCreateDto.end()),
                Mockito.eq(newCreateDto.end().plusHours(podStateless1.getResourceGroupPool().getGracePeriod())));

        verify(maintenanceIntervalRepository, times(1)).findAllIntervalsInGivenTimePeriod(
                Mockito.eq(course.getClusterId()), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end()));

        verify(courseMetricRepository, times(1)).findAllByCourse(Mockito.eq(course));
        verify(reservationRepository, times(1)).findCourseReservations(
                Mockito.eq(course), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end()));

        verify(bankerAlgorithm, times(1)).process(Mockito.any(),
                Mockito.eq(courseReservationList), Mockito.eq(resourceGroup3), Mockito.eq(cluster), Mockito.eq(hosts));
    }

    @Test
    public void Given_ClusterHasInsufficientResourcesForGivenReservation_When_CreateReservationForStatelessPod_Then_ThrowsException() {
        Cluster cluster = mock(Cluster.class);
        Host host1 = mock(Host.class);
        Host host2 = mock(Host.class);
        List<Host> hosts = List.of(host1, host2);

        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        CreateReservationDto newCreateDto = new CreateReservationDto(
                currentTime.plusHours(2),
                currentTime.plusHours(6),
                reservation1.getAutomaticStartup(),
                reservation1.getNotificationTime()
        );

        when(clusterService.findClusterById(Mockito.eq(course.getClusterId()))).thenReturn(cluster);
        when(clusterService.findAllHostsInCluster(Mockito.eq(cluster))).thenReturn(hosts);

        when(reservationRepository.findAllRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1))).thenReturn(List.of());

        when(reservationRepository.findRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1),
                Mockito.eq(newCreateDto.start().minusHours(podStateless1.getResourceGroupPool().getGracePeriod())),
                Mockito.eq(newCreateDto.start())))
                .thenReturn(List.of());

        when(reservationRepository.findRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1), Mockito.eq(newCreateDto.end()),
                Mockito.eq(newCreateDto.end().plusHours(podStateless1.getResourceGroupPool().getGracePeriod()))))
                .thenReturn(List.of());

        when(maintenanceIntervalRepository.findAllIntervalsInGivenTimePeriod(
                Mockito.eq(course.getClusterId()), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end())))
                .thenReturn(List.of());

        List<Reservation> courseReservationList = List.of(reservation1);
        when(courseMetricRepository.findAllByCourse(Mockito.eq(course))).thenReturn(courseMetrics);
        when(reservationRepository.findCourseReservations(Mockito.eq(course), Mockito.eq(newCreateDto.start()),
                Mockito.eq(newCreateDto.end()))).thenReturn(courseReservationList);

        // when(metricUtil.extractCourseMetricValues(Mockito.eq(courseMetrics))).thenReturn(courseMetricMap);
        when(bankerAlgorithm.process(Mockito.any(),
                Mockito.eq(courseReservationList), Mockito.eq(resourceGroup3), Mockito.eq(cluster), Mockito.eq(hosts)))
                .thenReturn(true);

        List<Reservation> clusterReservationList = List.of(reservation1, reservation2);
        when(clusterMetricRepository.findAllByClusterId(Mockito.eq(course.getClusterId()))).thenReturn(clusterMetrics);
        when(reservationRepository.findClusterReservations(Mockito.eq(course.getClusterId()), Mockito.eq(newCreateDto.start()),
                Mockito.eq(newCreateDto.end()))).thenReturn(clusterReservationList);

        // when(metricUtil.extractClusterMetricValues(Mockito.eq(clusterMetrics))).thenReturn(clusterMetricMap);
        when(bankerAlgorithm.process(Mockito.any(),
                Mockito.eq(clusterReservationList), Mockito.eq(resourceGroup3), Mockito.eq(cluster), Mockito.eq(hosts)))
                .thenReturn(false);

        assertThrows(ReservationCreationException.class,
                () -> reservationService.createReservationForStatelessPod(team1, podStateless1, newCreateDto));

        verify(clusterService, times(1)).findClusterById(Mockito.eq(course.getClusterId()));
        verify(clusterService, times(1)).findAllHostsInCluster(Mockito.eq(cluster));

        verify(reservationRepository, times(1)).findAllRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1));

        verify(reservationRepository, times(1)).findRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1),
                Mockito.eq(newCreateDto.start().minusHours(podStateless1.getResourceGroupPool().getGracePeriod())),
                Mockito.eq(newCreateDto.start()));

        verify(reservationRepository, times(1)).findRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1), Mockito.eq(newCreateDto.end()),
                Mockito.eq(newCreateDto.end().plusHours(podStateless1.getResourceGroupPool().getGracePeriod())));

        verify(maintenanceIntervalRepository, times(1)).findAllIntervalsInGivenTimePeriod(
                Mockito.eq(course.getClusterId()), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end()));

        verify(courseMetricRepository, times(1)).findAllByCourse(Mockito.eq(course));
        verify(reservationRepository, times(1)).findCourseReservations(
                Mockito.eq(course), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end()));

        // verify(metricUtil, times(1)).extractCourseMetricValues(Mockito.eq(courseMetrics));
        verify(bankerAlgorithm, times(1)).process(Mockito.any(),
                Mockito.eq(courseReservationList), Mockito.eq(resourceGroup3), Mockito.eq(cluster), Mockito.eq(hosts));

        verify(clusterMetricRepository, times(1)).findAllByClusterId(Mockito.eq(course.getClusterId()));
        verify(reservationRepository, times(1)).findClusterReservations(
                Mockito.eq(course.getClusterId()), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end()));

        // verify(metricUtil, times(1)).extractClusterMetricValues(Mockito.eq(clusterMetrics));
        verify(bankerAlgorithm, times(1)).process(Mockito.any(),
                Mockito.eq(clusterReservationList), Mockito.eq(resourceGroup3), Mockito.eq(cluster), Mockito.eq(hosts));
    }

    @Test
    public void Given_ReservationResourceGroupPoolHasNoAvailableResourceGroup_When_CreateReservationForStatefulPod_Then_ThrowsException() {
        Cluster cluster = mock(Cluster.class);
        Host host1 = mock(Host.class);
        Host host2 = mock(Host.class);
        List<Host> hosts = List.of(host1, host2);

        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        CreateReservationDto newCreateDto = new CreateReservationDto(
                currentTime.plusHours(2),
                currentTime.plusHours(6),
                reservation1.getAutomaticStartup(),
                reservation1.getNotificationTime()
        );

        when(clusterService.findClusterById(Mockito.eq(course.getClusterId()))).thenReturn(cluster);
        when(clusterService.findAllHostsInCluster(Mockito.eq(cluster))).thenReturn(hosts);

        when(reservationRepository.findAllRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1))).thenReturn(List.of());

        when(reservationRepository.findRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1),
                Mockito.eq(newCreateDto.start().minusHours(podStateless1.getResourceGroupPool().getGracePeriod())),
                Mockito.eq(newCreateDto.start())))
                .thenReturn(List.of());

        when(reservationRepository.findRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1), Mockito.eq(newCreateDto.end()),
                Mockito.eq(newCreateDto.end().plusHours(podStateless1.getResourceGroupPool().getGracePeriod()))))
                .thenReturn(List.of());

        when(maintenanceIntervalRepository.findAllIntervalsInGivenTimePeriod(
                Mockito.eq(course.getClusterId()), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end())))
                .thenReturn(List.of());

        List<Reservation> courseReservationList = List.of(reservation1);
        when(courseMetricRepository.findAllByCourse(Mockito.eq(course))).thenReturn(courseMetrics);
        when(reservationRepository.findCourseReservations(Mockito.eq(course), Mockito.eq(newCreateDto.start()),
                Mockito.eq(newCreateDto.end()))).thenReturn(courseReservationList);

        // when(metricUtil.extractCourseMetricValues(Mockito.eq(courseMetrics))).thenReturn(courseMetricMap);
        when(bankerAlgorithm.process(Mockito.any(),
                Mockito.eq(courseReservationList), Mockito.eq(resourceGroup3), Mockito.eq(cluster), Mockito.eq(hosts)))
                .thenReturn(true);

        List<Reservation> clusterReservationList = List.of(reservation1, reservation2);
        when(clusterMetricRepository.findAllByClusterId(Mockito.eq(course.getClusterId()))).thenReturn(clusterMetrics);
        when(reservationRepository.findClusterReservations(Mockito.eq(course.getClusterId()), Mockito.eq(newCreateDto.start()),
                Mockito.eq(newCreateDto.end()))).thenReturn(clusterReservationList);

        // when(metricUtil.extractClusterMetricValues(Mockito.eq(clusterMetrics))).thenReturn(clusterMetricMap);
        when(bankerAlgorithm.process(Mockito.any(),
                Mockito.eq(clusterReservationList), Mockito.eq(resourceGroup3), Mockito.eq(cluster), Mockito.eq(hosts)))
                .thenReturn(true);

        when(reservationRepository.findRgReservations(Mockito.eq(resourceGroup3), Mockito.eq(newCreateDto.start()),
                Mockito.eq(newCreateDto.end()))).thenReturn(List.of(reservation1));

        assertThrows(ReservationCreationException.class,
                () -> reservationService.createReservationForStatelessPod(team1, podStateless1, newCreateDto));

        verify(clusterService, times(1)).findClusterById(Mockito.eq(course.getClusterId()));
        verify(clusterService, times(1)).findAllHostsInCluster(Mockito.eq(cluster));

        verify(reservationRepository, times(1)).findAllRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1));

        verify(reservationRepository, times(1)).findRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1),
                Mockito.eq(newCreateDto.start().minusHours(podStateless1.getResourceGroupPool().getGracePeriod())),
                Mockito.eq(newCreateDto.start()));

        verify(reservationRepository, times(1)).findRgPoolReservationsForGivenTeam(
                Mockito.eq(podStateless1.getResourceGroupPool()), Mockito.eq(team1), Mockito.eq(newCreateDto.end()),
                Mockito.eq(newCreateDto.end().plusHours(podStateless1.getResourceGroupPool().getGracePeriod())));

        verify(maintenanceIntervalRepository, times(1)).findAllIntervalsInGivenTimePeriod(
                Mockito.eq(course.getClusterId()), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end()));

        verify(courseMetricRepository, times(1)).findAllByCourse(Mockito.eq(course));
        verify(reservationRepository, times(1)).findCourseReservations(
                Mockito.eq(course), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end()));

        // verify(metricUtil, times(1)).extractCourseMetricValues(Mockito.eq(courseMetrics));
        verify(bankerAlgorithm, times(1)).process(Mockito.any(),
                Mockito.eq(courseReservationList), Mockito.eq(resourceGroup3), Mockito.eq(cluster), Mockito.eq(hosts));

        verify(clusterMetricRepository, times(1)).findAllByClusterId(Mockito.eq(course.getClusterId()));
        verify(reservationRepository, times(1)).findClusterReservations(
                Mockito.eq(course.getClusterId()), Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end()));

        // verify(metricUtil, times(1)).extractClusterMetricValues(Mockito.eq(clusterMetrics));
        verify(bankerAlgorithm, times(1)).process(Mockito.any(),
                Mockito.eq(clusterReservationList), Mockito.eq(resourceGroup3), Mockito.eq(cluster), Mockito.eq(hosts));

        verify(reservationRepository, times(1)).findRgReservations(Mockito.eq(resourceGroup3),
                Mockito.eq(newCreateDto.start()), Mockito.eq(newCreateDto.end()));
    }

    /* FindReservationById method tests */

    @Test
    public void Given_ExistingReservationIdentifierIsPassed_When_FindReservationById_Then_ReturnsOptionalWithGivenReservation() {
        when(reservationRepository.findById(Mockito.eq(reservation1.getId()))).thenReturn(Optional.of(reservation1));

        Optional<Reservation> reservationOptional = reservationService.findReservationById(reservation1.getId());

        assertNotNull(reservationOptional);
        assertTrue(reservationOptional.isPresent());

        Reservation foundReservation = reservationOptional.get();

        assertNotNull(foundReservation);
        assertEquals(reservation1, foundReservation);

        verify(reservationRepository, times(1)).findById(Mockito.eq(reservation1.getId()));
    }

    @Test
    public void Given_NonExistentReservationIdentifierIsPassed_When_FindReservationById_Then_ThrowsException() {
        UUID nonExistentReservationId = UUID.randomUUID();
        when(reservationRepository.findById(Mockito.eq(nonExistentReservationId))).thenReturn(Optional.empty());

        Optional<Reservation> reservationOptional = reservationService.findReservationById(nonExistentReservationId);

        assertNotNull(reservationOptional);
        assertTrue(reservationOptional.isEmpty());;

        verify(reservationRepository, times(1)).findById(Mockito.eq(nonExistentReservationId));
    }

    /* FindReservationsForStatelessPod method tests */

    @Test
    public void Given_SomeReservationsExistForGivenTimePeriod_When_FindReservationsForStatelessPod_Then_ReturnsPageWithFoundReservations() {
        reservation1.setResourceGroup(resourceGroup3);
        reservation3.setResourceGroup(resourceGroup3);

        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        when(reservationRepository.findAllRgPoolReservationsForGivenTeam(
                Mockito.eq(resourceGroupPool1), Mockito.eq(team1), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(reservation1, reservation3), pageable, 2));

        Page<Reservation> reservationPage = reservationService
                .findReservationsForStatelessPod(podStateless1, team1, pageable);

        assertNotNull(reservationPage);

        assertEquals(reservationPage.getNumber(), 0);
        assertEquals(reservationPage.getNumberOfElements(), 2);
        assertEquals(reservationPage.getTotalPages(), 1);
        assertEquals(reservationPage.getTotalElements(), 2);

        List<Reservation> foundReservations = reservationPage.getContent();
        assertNotNull(foundReservations);
        assertFalse(foundReservations.isEmpty());
        assertEquals(2, foundReservations.size());

        Reservation firstReservation = foundReservations.getFirst();
        assertNotNull(firstReservation);
        assertEquals(reservation1, firstReservation);

        Reservation secondReservation = foundReservations.getLast();
        assertNotNull(secondReservation);
        assertEquals(reservation3, secondReservation);

        verify(reservationRepository, times(1)).findAllRgPoolReservationsForGivenTeam(
                Mockito.eq(resourceGroupPool1), Mockito.eq(team1), Mockito.eq(pageable));
    }

    @Test
    public void Given_NoReservationsExistForGivenTimePeriod_When_FindReservationsForStatelessPod_Then_ReturnsEmptyReservationPage() {
        reservation1.setResourceGroup(resourceGroup3);
        reservation3.setResourceGroup(resourceGroup3);

        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        when(reservationRepository.findAllRgPoolReservationsForGivenTeam(
                Mockito.eq(resourceGroupPool1), Mockito.eq(team1), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        Page<Reservation> reservationPage = reservationService
                .findReservationsForStatelessPod(podStateless1, team1, pageable);

        assertNotNull(reservationPage);

        assertEquals(reservationPage.getNumber(), 0);
        assertEquals(reservationPage.getNumberOfElements(), 0);
        assertEquals(reservationPage.getTotalPages(), 0);
        assertEquals(reservationPage.getTotalElements(), 0);

        List<Reservation> foundReservations = reservationPage.getContent();
        assertNotNull(foundReservations);
        assertTrue(foundReservations.isEmpty());

        verify(reservationRepository, times(1)).findAllRgPoolReservationsForGivenTeam(
                Mockito.eq(resourceGroupPool1), Mockito.eq(team1), Mockito.eq(pageable));
    }

    /* FindReservationsForStatefulPod method tests */

    @Test
    public void Given_SomeReservationsExistForGivenTimePeriod_When_FindReservationsForStatefulPod_Then_ReturnsPageWithFoundReservations() {
        reservation1.setResourceGroup(resourceGroup1);
        reservation3.setResourceGroup(resourceGroup1);

        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        when(reservationRepository.findAllRgReservationsForGivenTeam(
                Mockito.eq(resourceGroup1), Mockito.eq(team1), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(reservation1, reservation3), pageable, 2));

        Page<Reservation> reservationPage = reservationService
                .findReservationsForStatefulPod(podStateful1, team1, pageable);

        assertNotNull(reservationPage);

        assertEquals(reservationPage.getNumber(), 0);
        assertEquals(reservationPage.getNumberOfElements(), 2);
        assertEquals(reservationPage.getTotalPages(), 1);
        assertEquals(reservationPage.getTotalElements(), 2);

        List<Reservation> foundReservations = reservationPage.getContent();
        assertNotNull(foundReservations);
        assertFalse(foundReservations.isEmpty());
        assertEquals(2, foundReservations.size());

        Reservation firstReservation = foundReservations.getFirst();
        assertNotNull(firstReservation);
        assertEquals(reservation1, firstReservation);

        Reservation secondReservation = foundReservations.getLast();
        assertNotNull(secondReservation);
        assertEquals(reservation3, secondReservation);

        verify(reservationRepository, times(1)).findAllRgReservationsForGivenTeam(
                Mockito.eq(resourceGroup1), Mockito.eq(team1), Mockito.eq(pageable));
    }

    @Test
    public void Given_NoReservationsExistForGivenTimePeriod_When_FindReservationsForStatefulPod_Then_ReturnsEmptyReservationPage() {
        reservation1.setResourceGroup(resourceGroup1);
        reservation3.setResourceGroup(resourceGroup1);

        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        when(reservationRepository.findAllRgReservationsForGivenTeam(
                Mockito.eq(resourceGroup1), Mockito.eq(team1), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        Page<Reservation> reservationPage = reservationService
                .findReservationsForStatefulPod(podStateful1, team1, pageable);

        assertNotNull(reservationPage);

        assertEquals(reservationPage.getNumber(), 0);
        assertEquals(reservationPage.getNumberOfElements(), 0);
        assertEquals(reservationPage.getTotalPages(), 0);
        assertEquals(reservationPage.getTotalElements(), 0);

        List<Reservation> foundReservations = reservationPage.getContent();
        assertNotNull(foundReservations);
        assertTrue(foundReservations.isEmpty());

        verify(reservationRepository, times(1)).findAllRgReservationsForGivenTeam(
                Mockito.eq(resourceGroup1), Mockito.eq(team1), Mockito.eq(pageable));
    }

    /* FindRgReservations method tests */

    @Test
    public void Given_SomeReservationExistForGivenResourceGroup_When_FindRgReservations_Then_ReturnsListOfFoundReservations() {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(6);

        reservation1.setResourceGroup(resourceGroup1);
        reservation3.setResourceGroup(resourceGroup1);

        when(reservationRepository.findRgReservations(Mockito.eq(resourceGroup1), Mockito.eq(start), Mockito.eq(end)))
                .thenReturn(List.of(reservation1, reservation3));

        List<Reservation> foundReservations = reservationService.findRgReservations(resourceGroup1, start, end);

        assertNotNull(foundReservations);
        assertFalse(foundReservations.isEmpty());
        assertEquals(2, foundReservations.size());

        Reservation firstReservation = foundReservations.getFirst();
        assertNotNull(firstReservation);
        assertEquals(reservation1, firstReservation);

        Reservation secondReservation = foundReservations.getLast();
        assertNotNull(secondReservation);
        assertEquals(reservation3, secondReservation);

        verify(reservationRepository, times(1))
                .findRgReservations(Mockito.eq(resourceGroup1), Mockito.eq(start), Mockito.eq(end));
    }

    @Test
    public void Given_NoReservationExistForGivenResourceGroup_When_FindRgReservations_Then_ReturnsEmptyReservationList() {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(6);

        when(reservationRepository.findRgReservations(Mockito.eq(resourceGroup1), Mockito.eq(start), Mockito.eq(end)))
                .thenReturn(List.of());

        List<Reservation> foundReservations = reservationService.findRgReservations(resourceGroup1, start, end);

        assertNotNull(foundReservations);
        assertTrue(foundReservations.isEmpty());

        verify(reservationRepository, times(1))
                .findRgReservations(Mockito.eq(resourceGroup1), Mockito.eq(start), Mockito.eq(end));
    }

    /* FindRgPoolReservations method tests */

    @Test
    public void Given_SomeReservationExistForGivenResourceGroupPool_When_FindRgPoolReservations_Then_ReturnsListOfFoundReservations() {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(6);

        reservation1.setResourceGroup(resourceGroup3);
        reservation3.setResourceGroup(resourceGroup3);

        when(reservationRepository.findRgPoolReservations(Mockito.eq(resourceGroupPool1), Mockito.eq(start), Mockito.eq(end)))
                .thenReturn(List.of(reservation1, reservation3));

        List<Reservation> foundReservations = reservationService.findRgPoolReservations(resourceGroupPool1, start, end);

        assertNotNull(foundReservations);
        assertFalse(foundReservations.isEmpty());
        assertEquals(2, foundReservations.size());

        Reservation firstReservation = foundReservations.getFirst();
        assertNotNull(firstReservation);
        assertEquals(reservation1, firstReservation);

        Reservation secondReservation = foundReservations.getLast();
        assertNotNull(secondReservation);
        assertEquals(reservation3, secondReservation);

        verify(reservationRepository, times(1))
                .findRgPoolReservations(Mockito.eq(resourceGroupPool1), Mockito.eq(start), Mockito.eq(end));
    }

    @Test
    public void Given_NoReservationExistForGivenResourceGroupPool_When_FindRgPoolReservations_Then_ReturnsListOfFoundReservations() {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(6);

        when(reservationRepository.findRgPoolReservations(Mockito.eq(resourceGroupPool1), Mockito.eq(start), Mockito.eq(end)))
                .thenReturn(List.of());

        List<Reservation> foundReservations = reservationService.findRgPoolReservations(resourceGroupPool1, start, end);

        assertNotNull(foundReservations);
        assertTrue(foundReservations.isEmpty());

        verify(reservationRepository, times(1))
                .findRgPoolReservations(Mockito.eq(resourceGroupPool1), Mockito.eq(start), Mockito.eq(end));
    }

    /* FindActiveReservations method tests */

    @Test
    public void Given_ExistingTeamIdentifierIsPassed_When_FindActiveReservations_Then_ReturnsPageWithFoundReservations() {
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        when(teamRepository.findById(Mockito.eq(team1.getId()))).thenReturn(Optional.of(team1));
        when(reservationRepository.findAllActiveReservations(Mockito.eq(team1), Mockito.any(), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(reservation1, reservation3), pageable, 2));

        Page<Reservation> reservationPage = reservationService.findActiveReservations(team1.getId(), pageable);

        assertNotNull(reservationPage);

        assertEquals(reservationPage.getNumber(), 0);
        assertEquals(reservationPage.getNumberOfElements(), 2);
        assertEquals(reservationPage.getTotalPages(), 1);
        assertEquals(reservationPage.getTotalElements(), 2);

        List<Reservation> foundReservations = reservationPage.getContent();

        assertNotNull(foundReservations);
        assertFalse(foundReservations.isEmpty());
        assertEquals(2, foundReservations.size());

        Reservation firstReservation = foundReservations.getFirst();
        assertNotNull(firstReservation);
        assertEquals(reservation1, firstReservation);

        Reservation secondReservation = foundReservations.getLast();
        assertNotNull(secondReservation);
        assertEquals(reservation3, secondReservation);

        verify(teamRepository, times(1)).findById(Mockito.eq(team1.getId()));
        verify(reservationRepository, times(1))
                .findAllActiveReservations(Mockito.eq(team1), Mockito.any(), Mockito.eq(pageable));
    }

    @Test
    public void Given_NonExistentTeamIdentifierIsPassed_When_FindActiveReservations_Then_ThrowsException() {
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        UUID nonExistentTeamId = UUID.randomUUID();

        when(teamRepository.findById(Mockito.eq(nonExistentTeamId))).thenReturn(Optional.empty());

        assertThrows(TeamNotFoundException.class,
                () -> reservationService.findActiveReservations(nonExistentTeamId, pageable));

        verify(teamRepository, times(1)).findById(Mockito.eq(nonExistentTeamId));
    }

    /* FindHistoricalReservations method tests */

    @Test
    public void Given_ExistingTeamIdentifierIsPassed_When_FindHistoricalReservations_Then_ReturnsPageWithFoundReservations() {
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        when(teamRepository.findById(Mockito.eq(team1.getId()))).thenReturn(Optional.of(team1));
        when(reservationRepository.findAllHistoricalReservations(Mockito.eq(team1), Mockito.any(), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(reservation1, reservation3), pageable, 2));

        Page<Reservation> reservationPage = reservationService.findHistoricalReservations(team1.getId(), pageable);

        assertNotNull(reservationPage);

        assertEquals(reservationPage.getNumber(), 0);
        assertEquals(reservationPage.getNumberOfElements(), 2);
        assertEquals(reservationPage.getTotalPages(), 1);
        assertEquals(reservationPage.getTotalElements(), 2);

        List<Reservation> foundReservations = reservationPage.getContent();

        assertNotNull(foundReservations);
        assertFalse(foundReservations.isEmpty());
        assertEquals(2, foundReservations.size());

        Reservation firstReservation = foundReservations.getFirst();
        assertNotNull(firstReservation);
        assertEquals(reservation1, firstReservation);

        Reservation secondReservation = foundReservations.getLast();
        assertNotNull(secondReservation);
        assertEquals(reservation3, secondReservation);

        verify(teamRepository, times(1)).findById(Mockito.eq(team1.getId()));
        verify(reservationRepository, times(1))
                .findAllHistoricalReservations(Mockito.eq(team1), Mockito.any(), Mockito.eq(pageable));
    }

    @Test
    public void Given_NonExistentTeamIdentifierIsPassed_When_FindHistoricalReservations_Then_ThrowsException() {
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        UUID nonExistentTeamId = UUID.randomUUID();

        when(teamRepository.findById(Mockito.eq(nonExistentTeamId))).thenReturn(Optional.empty());

        assertThrows(TeamNotFoundException.class,
                () -> reservationService.findHistoricalReservations(nonExistentTeamId, pageable));

        verify(teamRepository, times(1)).findById(Mockito.eq(nonExistentTeamId));
    }


    /* CheckResourceGroupAvailability method tests */

    @Test
    public void Given_ExistingClusterIdentifierIsPassedAndThatClusterHasSomeHosts_When_CheckResourceGroupAvailability_Then_ReturnsResourceGroupAvailability() {
        int windowLength = 30;
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(4);

        Cluster cluster = mock(Cluster.class);
        Host host1 = mock(Host.class);
        Host host2 = mock(Host.class);
        List<Host> hosts = List.of(host1, host2);

        when(clusterService.findClusterById(Mockito.eq(course.getClusterId()))).thenReturn(cluster);
        when(clusterService.findAllHostsInCluster(Mockito.eq(cluster))).thenReturn(hosts);

        when(courseMetricRepository.findAllByCourse(Mockito.eq(course))).thenReturn(courseMetrics);
        when(clusterMetricRepository.findAllByClusterId(Mockito.eq(course.getClusterId())))
                .thenReturn(clusterMetrics);

        LocalDateTime startTemp = currentTime;
        var index = 0;
        while (startTemp.isBefore(end)) {
            List<Reservation> courseReservations = List.of(reservation1);
            lenient().when(reservationRepository.findCourseReservations(Mockito.eq(course), Mockito.eq(startTemp),
                    Mockito.eq(startTemp.plusMinutes(windowLength)))).thenReturn(courseReservations);

            List<Reservation> clusterReservations = List.of(reservation1, reservation3);
            lenient().when(reservationRepository.findClusterReservations(Mockito.eq(course.getClusterId()), Mockito.eq(startTemp),
                    Mockito.eq(startTemp.plusMinutes(windowLength)))).thenReturn(clusterReservations);

            if (index % 2 == 0) {
                lenient().when(bankerAlgorithm.process(Mockito.any(), Mockito.eq(courseReservations), Mockito.eq(resourceGroup1),
                        Mockito.eq(cluster), Mockito.eq(hosts))).thenReturn(false);

                lenient().when(bankerAlgorithm.process(Mockito.any(), Mockito.eq(clusterReservations), Mockito.eq(resourceGroup1),
                        Mockito.eq(cluster), Mockito.eq(hosts))).thenReturn(true);

                lenient().when(reservationRepository.findRgReservations(Mockito.eq(resourceGroup1), Mockito.eq(startTemp),
                        Mockito.eq(startTemp.plusMinutes(windowLength)))).thenReturn(List.of(reservation1));
            } else {
                lenient().when(bankerAlgorithm.process(Mockito.any(), Mockito.eq(courseReservations), Mockito.eq(resourceGroup1),
                        Mockito.eq(cluster), Mockito.eq(hosts))).thenReturn(false);

                lenient().when(bankerAlgorithm.process(Mockito.any(), Mockito.eq(clusterReservations), Mockito.eq(resourceGroup1),
                        Mockito.eq(cluster), Mockito.eq(hosts))).thenReturn(false);

                lenient().when(reservationRepository.findRgReservations(Mockito.eq(resourceGroup1), Mockito.eq(startTemp),
                        Mockito.eq(startTemp.plusMinutes(windowLength)))).thenReturn(List.of());
            }

            startTemp = startTemp.plusMinutes(windowLength);
            index++;
        }

        Map<LocalDateTime, Boolean> availability = reservationService
                .checkResourceGroupAvailability(resourceGroup1, course, windowLength, start, end);

        assertNotNull(availability);

        LocalDateTime secondTimestamp = start.plusMinutes(30);
        LocalDateTime thirdTimestamp = start.plusHours(1);
        LocalDateTime forthTimestamp = start.plusHours(1).plusMinutes(30);

        assertFalse(availability.get(start));
        assertFalse(availability.get(secondTimestamp));
        assertFalse(availability.get(thirdTimestamp));
        assertFalse(availability.get(forthTimestamp));

        verify(clusterService, times(1)).findClusterById(Mockito.eq(course.getClusterId()));
        verify(clusterService, times(1)).findAllHostsInCluster(Mockito.eq(cluster));

        verify(courseMetricRepository, times(1)).findAllByCourse(Mockito.eq(course));
        verify(clusterMetricRepository, times(1)).findAllByClusterId(Mockito.eq(course.getClusterId()));

        verify(reservationRepository, times(4))
                .findCourseReservations(Mockito.eq(course), Mockito.any(), Mockito.any());
        verify(reservationRepository, times(4))
                .findClusterReservations(Mockito.eq(course.getClusterId()), Mockito.any(), Mockito.any());

        verify(reservationRepository, times(4))
                .findRgReservations(Mockito.eq(resourceGroup1), Mockito.any(), Mockito.any());
    }

    @Test
    public void Given_NonExistentClusterIdentifierIsPassed_When_CheckResourceGroupAvailability_Then_ThrowsException() {
        int windowLength = 30;
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(4);
        
        UUID nonExistentClusterId = UUID.randomUUID();
        course.setClusterId(nonExistentClusterId);
        
        when(clusterService.findClusterById(Mockito.eq(nonExistentClusterId)))
                .thenThrow(ClusterNotFoundException.class);
        
        assertThrows(ClusterNotFoundException.class, () -> reservationService
                .checkResourceGroupAvailability(resourceGroup1, course, windowLength, start, end));

        verify(clusterService, times(1))
                .findClusterById(Mockito.eq(nonExistentClusterId));
    }

    @Test
    public void Given_NonExistentClusterIsPassedToHostFetchingMethod_When_CheckResourceGroupAvailability_Then_ThrowsException() {
        int windowLength = 30;
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(4);

        Cluster cluster = mock(Cluster.class);

        when(clusterService.findClusterById(Mockito.eq(course.getClusterId()))).thenReturn(cluster);
        when(clusterService.findAllHostsInCluster(Mockito.eq(cluster))).thenThrow(
                new HostNotFoundException("No host could be found for cluster %s".formatted(course.getClusterId())));
                
        assertThrows(HostNotFoundException.class, () -> reservationService
                .checkResourceGroupAvailability(resourceGroup1, course, windowLength, start, end));

        verify(clusterService, times(1)).findClusterById(Mockito.eq(course.getClusterId()));
        verify(clusterService, times(1)).findAllHostsInCluster(Mockito.eq(cluster));
    }

    /* CheckResourceGroupPoolAvailability method tests */

    @Test
    public void Given_ExistingClusterIdentifierIsPassedAndClusterHasSomeHosts_When_CheckResourceGroupPoolAvailability_Then_ReturnsResourceGroupPoolAvailability() {
        int windowLength = 30;
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(4);

        Cluster cluster = mock(Cluster.class);
        Host host1 = mock(Host.class);
        Host host2 = mock(Host.class);
        List<Host> hosts = List.of(host1, host2);

        when(clusterService.findClusterById(Mockito.eq(course.getClusterId()))).thenReturn(cluster);
        when(clusterService.findAllHostsInCluster(Mockito.eq(cluster))).thenReturn(hosts);

        when(courseMetricRepository.findAllByCourse(Mockito.eq(course))).thenReturn(courseMetrics);
        when(clusterMetricRepository.findAllByClusterId(Mockito.eq(course.getClusterId())))
                .thenReturn(clusterMetrics);

        LocalDateTime startTemp = currentTime;
        var index = 0;
        while (startTemp.isBefore(end)) {
            List<Reservation> courseReservations = List.of(reservation1);
            lenient().when(reservationRepository.findCourseReservations(Mockito.eq(course), Mockito.eq(startTemp),
                    Mockito.eq(startTemp.plusMinutes(windowLength)))).thenReturn(courseReservations);

            List<Reservation> clusterReservations = List.of(reservation1, reservation3);
            lenient().when(reservationRepository.findClusterReservations(Mockito.eq(course.getClusterId()), Mockito.eq(startTemp),
                    Mockito.eq(startTemp.plusMinutes(windowLength)))).thenReturn(clusterReservations);

            if (index % 2 == 0) {
                lenient().when(bankerAlgorithm.process(Mockito.any(), Mockito.eq(courseReservations), Mockito.eq(resourceGroup3),
                        Mockito.eq(cluster), Mockito.eq(hosts))).thenReturn(true);

                lenient().when(bankerAlgorithm.process(Mockito.any(), Mockito.eq(clusterReservations), Mockito.eq(resourceGroup3),
                        Mockito.eq(cluster), Mockito.eq(hosts))).thenReturn(false);

                lenient().when(reservationRepository.findRgReservations(Mockito.eq(resourceGroup3), Mockito.eq(startTemp),
                        Mockito.eq(startTemp.plusMinutes(windowLength)))).thenReturn(List.of(reservation1));
            } else {
                lenient().when(bankerAlgorithm.process(Mockito.any(), Mockito.eq(courseReservations), Mockito.eq(resourceGroup3),
                        Mockito.eq(cluster), Mockito.eq(hosts))).thenReturn(true);

                lenient().when(bankerAlgorithm.process(Mockito.any(), Mockito.eq(clusterReservations), Mockito.eq(resourceGroup3),
                        Mockito.eq(cluster), Mockito.eq(hosts))).thenReturn(true);

                lenient().when(reservationRepository.findRgReservations(Mockito.eq(resourceGroup3), Mockito.eq(startTemp),
                        Mockito.eq(startTemp.plusMinutes(windowLength)))).thenReturn(List.of());
            }

            startTemp = startTemp.plusMinutes(windowLength);
            index++;
        }

        Map<LocalDateTime, Boolean> availability = reservationService
                .checkResourceGroupPoolAvailability(resourceGroupPool1, course, windowLength, start, end);

        assertNotNull(availability);

        LocalDateTime secondTimestamp = start.plusMinutes(30);
        LocalDateTime thirdTimestamp = start.plusHours(1);
        LocalDateTime forthTimestamp = start.plusHours(1).plusMinutes(30);

        assertFalse(availability.get(start));
        assertTrue(availability.get(secondTimestamp));
        assertFalse(availability.get(thirdTimestamp));
        assertTrue(availability.get(forthTimestamp));

        verify(clusterService, times(1)).findClusterById(Mockito.eq(course.getClusterId()));
        verify(clusterService, times(1)).findAllHostsInCluster(Mockito.eq(cluster));

        verify(courseMetricRepository, times(1)).findAllByCourse(Mockito.eq(course));
        verify(clusterMetricRepository, times(1)).findAllByClusterId(Mockito.eq(course.getClusterId()));

        verify(reservationRepository, times(4))
                .findCourseReservations(Mockito.eq(course), Mockito.any(), Mockito.any());
        verify(reservationRepository, times(4))
                .findClusterReservations(Mockito.eq(course.getClusterId()), Mockito.any(), Mockito.any());

        verify(reservationRepository, times(4))
                .findRgReservations(Mockito.eq(resourceGroup3), Mockito.any(), Mockito.any());
    }

    @Test
    public void Given_NonExistentClusterIdentifierIsPassed_When_CheckResourceGroupPoolAvailability_Then_ThrowsException() {
        int windowLength = 30;
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(4);

        UUID nonExistentClusterId = UUID.randomUUID();
        course.setClusterId(nonExistentClusterId);

        when(clusterService.findClusterById(Mockito.eq(nonExistentClusterId)))
                .thenThrow(ClusterNotFoundException.class);

        assertThrows(ClusterNotFoundException.class, () -> reservationService
                .checkResourceGroupPoolAvailability(resourceGroupPool1, course, windowLength, start, end));

        verify(clusterService, times(1))
                .findClusterById(Mockito.eq(nonExistentClusterId));
    }

    @Test
    public void Given_NonExistentClusterIsPassedToMethodFetchingHosts_When_CheckResourceGroupPoolAvailability_Then_ThrowsException() {
        int windowLength = 30;
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(4);

        Cluster cluster = mock(Cluster.class);

        when(clusterService.findClusterById(Mockito.eq(course.getClusterId()))).thenReturn(cluster);
        when(clusterService.findAllHostsInCluster(Mockito.eq(cluster))).thenThrow(
                new HostNotFoundException("No host could be found for cluster %s".formatted(course.getClusterId())));

        assertThrows(HostNotFoundException.class, () -> reservationService
                .checkResourceGroupPoolAvailability(resourceGroupPool1, course, windowLength, start, end));

        verify(clusterService, times(1)).findClusterById(Mockito.eq(course.getClusterId()));
        verify(clusterService, times(1)).findAllHostsInCluster(Mockito.eq(cluster));
    }

    /* FinishReservation method tests */

    @Test
    public void Given_ReservationHasAlreadyFinished_ReservationAlreadyFinished_When_FinishReservation_Then_ThrowsException() {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        reservation1.setStartTime(currentTime.minusHours(8));
        reservation1.setEndTime(currentTime.minusHours(4));

        assertThrows(ReservationAlreadyFinishedException.class,
                () -> reservationService.finishReservationAsStudent(reservation1));
    }

    @Test
    public void Given_ReservationHasAlreadyBegunButNotYetFinished_When_FinishReservation_Then_FinishesReservationEarlier() {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        reservation1.setStartTime(currentTime.minusHours(2));
        reservation1.setEndTime(currentTime.plusHours(4));

        when(reservationRepository.saveAndFlush(Mockito.eq(reservation1)))
                .thenReturn(reservation1);

        reservationService.finishReservationAsStudent(reservation1);

        assertNotEquals(currentTime.plusHours(4), reservation1.getEndTime());

        verify(reservationRepository, times(1)).
                saveAndFlush(Mockito.eq(reservation1));
    }

    @Test
    public void Given_ReservationHasNotYetBegun_When_FinishReservation_Then_RemovesReservation() {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        reservation1.setStartTime(currentTime.plusHours(2));
        reservation1.setEndTime(currentTime.plusHours(6));

        doNothing().when(reservationRepository).delete(Mockito.eq(reservation1));

        reservationService.finishReservationAsStudent(reservation1);

        verify(reservationRepository, times(1))
                .delete(Mockito.eq(reservation1));
    }

    /* FinishReservation method tests */

    @Test
    public void Given_ReservationHasAlreadyBegunButNotYetFinished_When_FinishReservationAsTeacherOrAdmin_Then_FinishesReservationEarlier() {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        reservation1.setStartTime(currentTime.minusHours(2));
        reservation1.setEndTime(currentTime.plusHours(4));

        when(reservationRepository.saveAndFlush(Mockito.eq(reservation1)))
                .thenReturn(reservation1);

        reservationService.finishReservationAsStudent(reservation1);

        assertNotEquals(currentTime.plusHours(4), reservation1.getEndTime());

        verify(reservationRepository, times(1)).
                saveAndFlush(Mockito.eq(reservation1));
    }

    @Test
    public void Given_ReservationHasNotYetBegun_When_FinishReservationAsTeacherOrAdmin_Then_RemovesReservation() {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        reservation1.setStartTime(currentTime.plusHours(2));
        reservation1.setEndTime(currentTime.plusHours(6));

        doNothing().when(reservationRepository).delete(Mockito.eq(reservation1));

        reservationService.finishReservationAsStudent(reservation1);

        verify(reservationRepository, times(1))
                .delete(Mockito.eq(reservation1));
    }

    /* StartReservation method tests */

    @Test
    public void Given_ReservationIsInPendingState_When_StartReservation_Then_StartsReservationSuccessfully() {
        reservation1.setStatus(Reservation.ReservationStatus.PENDING);
        when(reservationRepository.saveAndFlush(Mockito.eq(reservation1))).thenReturn(reservation1);
        reservationService.startReservation(reservation1);
        assertEquals(Reservation.ReservationStatus.IN_PROGRESS, reservation1.getStatus());
        verify(reservationRepository, times(1)).saveAndFlush(Mockito.eq(reservation1));
    }

    @Test
    public void Given_ReservationIsInInProgressState_When_StartReservation_Then_ThrowsException() {
        reservation1.setStatus(Reservation.ReservationStatus.IN_PROGRESS);
        assertThrows(ReservationStatusException.class,
                () -> reservationService.startReservation(reservation1));
    }

    @Test
    public void Given_ReservationIsInCompletedState_When_StartReservation_Then_ThrowsException() {
        reservation1.setStatus(Reservation.ReservationStatus.COMPLETED);
        assertThrows(ReservationStatusException.class,
                () -> reservationService.startReservation(reservation1));
    }

    /* EndReservation method tests */

    @Test
    public void Given_ReservationIsInInProgressState_When_EndReservation_Then_EndsReservationSuccessfully() {
        reservation1.setStatus(Reservation.ReservationStatus.IN_PROGRESS);
        when(reservationRepository.saveAndFlush(Mockito.eq(reservation1))).thenReturn(reservation1);
        reservationService.endReservation(reservation1);
        assertEquals(Reservation.ReservationStatus.COMPLETED, reservation1.getStatus());
        verify(reservationRepository, times(1)).saveAndFlush(Mockito.eq(reservation1));
    }

    @Test
    public void Given_ReservationIsInPendingState_When_EndReservation_Then_ThrowsException() {
        reservation1.setStatus(Reservation.ReservationStatus.PENDING);
        assertThrows(ReservationStatusException.class,
                () -> reservationService.endReservation(reservation1));
    }

    @Test
    public void Given_ReservationIsInCompletedState_When_EndReservation_Then_ThrowsException() {
        reservation1.setStatus(Reservation.ReservationStatus.COMPLETED);
        assertThrows(ReservationStatusException.class,
                () -> reservationService.endReservation(reservation1));
    }
}
