package pl.lodz.p.it.eduvirt.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.sdk4.types.Cluster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import pl.lodz.p.it.eduvirt.entity.*;
import pl.lodz.p.it.eduvirt.exceptions.MaintenanceIntervalAlreadyFinishedException;
import pl.lodz.p.it.eduvirt.exceptions.MaintenanceIntervalConflictException;
import pl.lodz.p.it.eduvirt.exceptions.MaintenanceIntervalInvalidTimeWindowException;
import pl.lodz.p.it.eduvirt.exceptions.MaintenanceIntervalNotFound;
import pl.lodz.p.it.eduvirt.repository.MaintenanceIntervalRepository;
import pl.lodz.p.it.eduvirt.repository.ReservationRepository;
import pl.lodz.p.it.eduvirt.repository.UserRepository;
import pl.lodz.p.it.eduvirt.service.impl.MaintenanceIntervalServiceImpl;
import pl.lodz.p.it.eduvirt.util.MailProvider;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaintenanceIntervalServiceTest {

    @Mock
    private MaintenanceIntervalRepository maintenanceIntervalRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MailProvider mailProvider;

    @InjectMocks
    private MaintenanceIntervalServiceImpl maintenanceIntervalService;

    @Mock
    private Cluster cluster;

    /* Initialization */

    private final UUID existingClusterId = UUID.randomUUID();

    private MaintenanceInterval maintenanceInterval1;
    private MaintenanceInterval maintenanceInterval2;
    private MaintenanceInterval maintenanceInterval3;
    private MaintenanceInterval maintenanceInterval4;

    private Course course;

    private Team team;

    private User userNo1;
    private User userNo2;
    private User userNo3;

    private ResourceGroupPool rgPoolNo1;
    private ResourceGroupPool rgPoolNo2;
    private ResourceGroupPool rgPoolNo3;

    private ResourceGroup resourceGroupNo1;
    private ResourceGroup resourceGroupNo2;
    private ResourceGroup resourceGroupNo3;

    private Reservation reservationNo1;
    private Reservation reservationNo2;
    private Reservation reservationNo3;

    @BeforeEach
    void prepareTestData() throws Exception {
        Field id = AbstractEntity.class.getDeclaredField("id");
        Field version = Updatable.class.getDeclaredField("version");

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

        version.setAccessible(true);
        version.set(maintenanceInterval1, 1L);
        version.set(maintenanceInterval2, 1L);
        version.set(maintenanceInterval3, 1L);
        version.set(maintenanceInterval4, 1L);
        version.setAccessible(false);

        course = new Course();
        course.setName("Sieciowe System Baz Danych");
        course.setDescription("Network Database Systems");
        course.setClusterId(existingClusterId);

        userNo1 = new User(UUID.randomUUID(), UUID.randomUUID(), "email1@gmail.com", "UserName1", "FirstName1", "LastName1");
        userNo2 = new User(UUID.randomUUID(), UUID.randomUUID(), "email2@gmail.com", "UserName2", "FirstName2", "LastName2");
        userNo3 = new User(UUID.randomUUID(), UUID.randomUUID(), "email3@gmail.com", "UserName3", "FirstName3", "LastName3");

        List<User> listOfUsers = List.of(userNo1, userNo2, userNo3);
        team = Team.builder()
                .name("Eldorado")
                .active(true)
                .maxSize(7)
                .course(course)
                .users(new ArrayList<>())
                .build();
        team.getUsers().addAll(listOfUsers);

        rgPoolNo1 = new ResourceGroupPool();
        rgPoolNo1.setName("SSBD-RGPoolNo1");
        rgPoolNo1.setMaxRent(12);
        rgPoolNo1.setGracePeriod(12);

        rgPoolNo2 = new ResourceGroupPool();
        rgPoolNo2.setName("SSBD-RGPoolNo2");
        rgPoolNo2.setMaxRent(12);
        rgPoolNo2.setGracePeriod(12);

        rgPoolNo3 = new ResourceGroupPool();
        rgPoolNo3.setName("SSBD-RGPoolNo3");
        rgPoolNo3.setMaxRent(12);
        rgPoolNo3.setGracePeriod(12);

        resourceGroupNo1 = new ResourceGroup();
        resourceGroupNo1.setName("SSBD-RGNo1");
        resourceGroupNo1.setDescription("First resource group for SSBD course.");
        resourceGroupNo1.setMaxRentTime(12);
        resourceGroupNo1.setStateless(false);

        resourceGroupNo1.getVms().addAll(List.of(
                VirtualMachine.builder().id(UUID.randomUUID()).build(),
                VirtualMachine.builder().id(UUID.randomUUID()).build(),
                VirtualMachine.builder().id(UUID.randomUUID()).build(),
                VirtualMachine.builder().id(UUID.randomUUID()).build(),
                VirtualMachine.builder().id(UUID.randomUUID()).build()
        ));

        resourceGroupNo2 = new ResourceGroup();
        resourceGroupNo2.setName("SSBD-RGNo2");
        resourceGroupNo2.setDescription("Second resource group for SSBD course.");
        resourceGroupNo2.setMaxRentTime(12);
        resourceGroupNo2.setStateless(false);

        resourceGroupNo2.getVms().addAll(List.of(
                VirtualMachine.builder().id(UUID.randomUUID()).build(),
                VirtualMachine.builder().id(UUID.randomUUID()).build(),
                VirtualMachine.builder().id(UUID.randomUUID()).build(),
                VirtualMachine.builder().id(UUID.randomUUID()).build(),
                VirtualMachine.builder().id(UUID.randomUUID()).build()
        ));

        resourceGroupNo3 = new ResourceGroup();
        resourceGroupNo3.setName("SSBD-RGNo3");
        resourceGroupNo3.setDescription("Third resource group for SSBD course.");
        resourceGroupNo3.setMaxRentTime(12);
        resourceGroupNo3.setStateless(false);

        resourceGroupNo3.getVms().addAll(List.of(
                VirtualMachine.builder().id(UUID.randomUUID()).build(),
                VirtualMachine.builder().id(UUID.randomUUID()).build(),
                VirtualMachine.builder().id(UUID.randomUUID()).build(),
                VirtualMachine.builder().id(UUID.randomUUID()).build(),
                VirtualMachine.builder().id(UUID.randomUUID()).build()
        ));

        rgPoolNo1.getResourceGroups().add(resourceGroupNo1);
        rgPoolNo2.getResourceGroups().add(resourceGroupNo2);
        rgPoolNo3.getResourceGroups().add(resourceGroupNo3);

        reservationNo1 = new Reservation(resourceGroupNo1, team, LocalDateTime.now().minusHours(12), LocalDateTime.now(), true, 10);
        reservationNo2 = new Reservation(resourceGroupNo1, team, LocalDateTime.now().plusHours(12), LocalDateTime.now().plusHours(24), true, 0);
        reservationNo3 = new Reservation(resourceGroupNo1, team, LocalDateTime.now().plusHours(36), LocalDateTime.now().plusHours(48), true, 15);

        id.setAccessible(true);

        id.set(course, UUID.randomUUID());
        id.set(team, UUID.randomUUID());

        id.set(rgPoolNo1, UUID.randomUUID());
        id.set(rgPoolNo2, UUID.randomUUID());
        id.set(rgPoolNo3, UUID.randomUUID());

        id.set(resourceGroupNo1, UUID.randomUUID());
        id.set(resourceGroupNo2, UUID.randomUUID());
        id.set(resourceGroupNo3, UUID.randomUUID());

        id.set(reservationNo1, UUID.randomUUID());
        id.set(reservationNo2, UUID.randomUUID());
        id.set(reservationNo3, UUID.randomUUID());

        id.setAccessible(false);
    }

    /* Tests */

    /* CreateClusterMaintenanceInterval method tests */

    @Test
    void Given_AllDataMatchesRequiredConditionsAndReservationsExistDuringTheMaintenance_When_CreateClusterMaintenanceInterval_Then_NewIntervalCreateSuccessfully() {
        String cause = "example_cause";
        String description = "example_description";
        UUID clusterId = UUID.randomUUID();
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(3);

        MaintenanceInterval exampleMaintenanceInterval = new MaintenanceInterval(
                cause, description, MaintenanceInterval.IntervalType.CLUSTER, existingClusterId, start, end);

        when(cluster.id()).thenReturn(clusterId.toString());
        when(maintenanceIntervalRepository.findAllIntervalsInGivenTimePeriod(start, end,
                MaintenanceInterval.IntervalType.CLUSTER, clusterId)).thenReturn(List.of());
        when(maintenanceIntervalRepository.saveAndFlush(any())).thenReturn(exampleMaintenanceInterval);
        when(reservationRepository.findClusterReservations(clusterId, start, end))
                .thenReturn(List.of(reservationNo1, reservationNo2));

        when(userRepository.findById(userNo1.getId())).thenReturn(Optional.of(userNo1));
        when(userRepository.findById(userNo2.getId())).thenReturn(Optional.of(userNo2));
        when(userRepository.findById(userNo3.getId())).thenReturn(Optional.of(userNo3));

        doNothing().when(mailProvider).sendReservationRemovalEmail(
                any(String.class), any(String.class), any(String.class),
                any(Reservation.class), any(), any());

        doNothing().when(reservationRepository).delete(reservationNo2);

        maintenanceIntervalService.createClusterMaintenanceInterval(cluster, cause, description, start, end);

        verify(cluster, timeout(1)).id();
        verify(maintenanceIntervalRepository, times(1))
                .findAllIntervalsInGivenTimePeriod(start, end, MaintenanceInterval.IntervalType.CLUSTER, clusterId);
        verify(maintenanceIntervalRepository, times(1)).saveAndFlush(any());
        verify(reservationRepository, times(1)).findClusterReservations(clusterId, start, end);

        verify(userRepository, times(6)).findById(any(UUID.class));
        verify(mailProvider, times(3)).sendReservationRemovalEmail(
                any(String.class), any(String.class), any(String.class),
                any(Reservation.class), any(), any());
        verify(reservationRepository, times(1)).delete(any(Reservation.class));
    }

    @Test
    void Given_AllDataMatchesRequiredConditionsAndNoReservationsAreFound_When_CreateClusterMaintenanceInterval_Then_NewIntervalCreateSuccessfully() {
        String cause = "example_cause";
        String description = "example_description";
        UUID clusterId = UUID.randomUUID();
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(3);

        MaintenanceInterval exampleMaintenanceInterval = new MaintenanceInterval(
                cause, description, MaintenanceInterval.IntervalType.CLUSTER, existingClusterId, start, end);

        when(cluster.id()).thenReturn(clusterId.toString());
        when(maintenanceIntervalRepository.findAllIntervalsInGivenTimePeriod(start, end,
                MaintenanceInterval.IntervalType.CLUSTER, clusterId)).thenReturn(List.of());
        when(maintenanceIntervalRepository.saveAndFlush(any())).thenReturn(exampleMaintenanceInterval);
        when(reservationRepository.findClusterReservations(clusterId, start, end)).thenReturn(List.of());

        maintenanceIntervalService.createClusterMaintenanceInterval(cluster, cause, description, start, end);

        verify(cluster, timeout(1)).id();
        verify(maintenanceIntervalRepository, times(1))
                .findAllIntervalsInGivenTimePeriod(start, end, MaintenanceInterval.IntervalType.CLUSTER, clusterId);
        verify(maintenanceIntervalRepository, times(1)).saveAndFlush(any());
        verify(reservationRepository, times(1)).findClusterReservations(clusterId, start, end);
    }

    @Test
    void Given_MaintenanceIntervalBeginAtAfterEndAt_When_CreateClusterMaintenanceInterval_Then_ThrowsException() {
        String cause = "example_cause";
        String description = "example_description";
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(4);
        LocalDateTime end = currentTime.plusHours(2);

        assertThrows(MaintenanceIntervalInvalidTimeWindowException.class, () -> maintenanceIntervalService
                .createClusterMaintenanceInterval(cluster, cause, description, start, end));
    }

    @Test
    void Given_MaintenanceIntervalBeginAtIsInThePast_When_CreateClusterMaintenanceInterval_Then_ThrowsException() {
        String cause = "example_cause";
        String description = "example_description";
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.minusMinutes(1);
        LocalDateTime end = currentTime.plusHours(2).plusMinutes(59);

        assertThrows(MaintenanceIntervalInvalidTimeWindowException.class, () -> maintenanceIntervalService
                .createClusterMaintenanceInterval(cluster, cause, description, start, end));
    }

    @Test
    void Given_OtherMaintenanceIntervalsExistForGivenCluster_When_CreateClusterMaintenanceInterval_Then_ThrowsException() {
        String cause = "example_cause";
        String description = "example_description";
        UUID clusterId = UUID.randomUUID();
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(3);

        when(cluster.id()).thenReturn(clusterId.toString());
        when(maintenanceIntervalRepository.findAllIntervalsInGivenTimePeriod(start, end,
                MaintenanceInterval.IntervalType.CLUSTER, clusterId)).thenReturn(List.of(maintenanceInterval1, maintenanceInterval2));

        assertThrows(MaintenanceIntervalConflictException.class, () -> maintenanceIntervalService
                .createClusterMaintenanceInterval(cluster, cause, description, start, end));

        verify(cluster, timeout(1)).id();
        verify(maintenanceIntervalRepository, times(1))
                .findAllIntervalsInGivenTimePeriod(start, end, MaintenanceInterval.IntervalType.CLUSTER, clusterId);
    }

    /* CreateSystemMaintenanceInterval method tests */

    @Test
    void Given_AllDataMatchesRequiredConditionsAndSomeReservationsAreFound_When_CreateSystemMaintenanceInterval_Then_CreatesNewSystemMaintenanceIntervalSuccessfully() {
        String cause = "example_cause";
        String description = "example_description";
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(4);

        MaintenanceInterval exampleMaintenanceInterval = new MaintenanceInterval(
                cause, description, MaintenanceInterval.IntervalType.SYSTEM, null, start, end);

        when(maintenanceIntervalRepository.findAllIntervalsInGivenTimePeriod(eq(start), eq(end),
                eq(MaintenanceInterval.IntervalType.SYSTEM), isNull())).thenReturn(List.of());
        when(maintenanceIntervalRepository.saveAndFlush(any())).thenReturn(exampleMaintenanceInterval);

        when(reservationRepository.findSystemReservations(start, end))
                .thenReturn(List.of(reservationNo1, reservationNo2));

        when(userRepository.findById(userNo1.getId())).thenReturn(Optional.of(userNo1));
        when(userRepository.findById(userNo2.getId())).thenReturn(Optional.of(userNo2));
        when(userRepository.findById(userNo3.getId())).thenReturn(Optional.of(userNo3));

        doNothing().when(mailProvider).sendReservationRemovalEmail(
                any(String.class), any(String.class), any(String.class),
                any(Reservation.class), any(), any());

        doNothing().when(mailProvider).sendReservationShortenedEmail(
                any(String.class), any(String.class), any(String.class),
                any(Reservation.class), any(), any());

        doNothing().when(reservationRepository).delete(reservationNo2);

        maintenanceIntervalService.createSystemMaintenanceInterval(cause, description, start, end);

        verify(maintenanceIntervalRepository, times(1)).findAllIntervalsInGivenTimePeriod(
                eq(start), eq(end), eq(MaintenanceInterval.IntervalType.SYSTEM), isNull());
        verify(maintenanceIntervalRepository, times(1)).saveAndFlush(any());
        verify(reservationRepository, times(1)).findSystemReservations(start, end);

        verify(reservationRepository, times(1)).findSystemReservations(start, end);
        verify(userRepository, times(6)).findById(any(UUID.class));

        verify(mailProvider, times(3)).sendReservationRemovalEmail(
                any(String.class), any(String.class), any(String.class),
                any(Reservation.class), any(), any());

        verify(mailProvider, times(3)).sendReservationShortenedEmail(
                any(String.class), any(String.class), any(String.class),
                any(Reservation.class), any(), any());

        verify(reservationRepository, times(1)).delete(any(Reservation.class));
    }

    @Test
    void Given_AllDataMatchesRequiredConditionsAndNoReservationsAreFound_When_CreateSystemMaintenanceInterval_Then_CreatesNewSystemMaintenanceIntervalSuccessfully() {
        String cause = "example_cause";
        String description = "example_description";
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(4);

        MaintenanceInterval exampleMaintenanceInterval = new MaintenanceInterval(
                cause, description, MaintenanceInterval.IntervalType.SYSTEM, null, start, end);

        when(maintenanceIntervalRepository.findAllIntervalsInGivenTimePeriod(eq(start), eq(end),
                eq(MaintenanceInterval.IntervalType.SYSTEM), isNull())).thenReturn(List.of());
        when(maintenanceIntervalRepository.saveAndFlush(any())).thenReturn(exampleMaintenanceInterval);
        when(reservationRepository.findSystemReservations(start, end)).thenReturn(List.of());

        maintenanceIntervalService.createSystemMaintenanceInterval(cause, description, start, end);

        verify(maintenanceIntervalRepository, times(1))
                .findAllIntervalsInGivenTimePeriod(eq(start), eq(end), eq(MaintenanceInterval.IntervalType.SYSTEM), isNull());
        verify(maintenanceIntervalRepository, times(1)).saveAndFlush(any());
        verify(reservationRepository, times(1)).findSystemReservations(start, end);
    }

    @Test
    void Given_MaintenanceIntervalBeginAtAfterEndAt_When_CreateSystemMaintenanceInterval_Then_ThrowsException() {
        String cause = "example_cause";
        String description = "example_description";
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(4);
        LocalDateTime end = currentTime.plusHours(2);

        assertThrows(MaintenanceIntervalInvalidTimeWindowException.class, () -> maintenanceIntervalService
                .createSystemMaintenanceInterval(cause, description, start, end));
    }

    @Test
    void Given_MaintenanceIntervalBeginAtIsInThePast_When_CreateSystemMaintenanceInterval_Then_ThrowsException() {
        String cause = "example_cause";
        String description = "example_description";
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.minusMinutes(1);
        LocalDateTime end = currentTime.plusHours(2).plusMinutes(59);

        assertThrows(MaintenanceIntervalInvalidTimeWindowException.class, () -> maintenanceIntervalService
                .createSystemMaintenanceInterval(cause, description, start, end));
    }

    @Test
    void Given_OtherMaintenanceIntervalsExistForGivenCluster_When_CreateSystemMaintenanceInterval_Then_ThrowsException() {
        String cause = "example_cause";
        String description = "example_description";
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(4);

        when(maintenanceIntervalRepository.findAllIntervalsInGivenTimePeriod(eq(start), eq(end),
                eq(MaintenanceInterval.IntervalType.SYSTEM), isNull())).thenReturn(List.of(maintenanceInterval1, maintenanceInterval2));

        assertThrows(MaintenanceIntervalConflictException.class, () -> maintenanceIntervalService
                .createSystemMaintenanceInterval(cause, description, start, end));

        verify(maintenanceIntervalRepository, times(1)).findAllIntervalsInGivenTimePeriod(
                eq(start), eq(end), eq(MaintenanceInterval.IntervalType.SYSTEM), isNull());
    }

    /* FindMaintenanceInterval method tests */

    @Test
    void Given_ExistingMaintenanceIntervalIdentifierIsPassed_When_FindMaintenanceInterval_Then_ReturnsFoundMaintenanceInterval() {
        when(maintenanceIntervalRepository.findById(maintenanceInterval1.getId())).thenReturn(Optional.of(maintenanceInterval1));

        Optional<MaintenanceInterval> foundMaintenanceInterval = maintenanceIntervalService.findMaintenanceInterval(maintenanceInterval1.getId());

        assertNotNull(foundMaintenanceInterval);
        assertTrue(foundMaintenanceInterval.isPresent());

        MaintenanceInterval maintenanceInterval = foundMaintenanceInterval.get();

        assertNotNull(maintenanceInterval);
        assertEquals(maintenanceInterval1, maintenanceInterval);

        verify(maintenanceIntervalRepository, times(1)).findById(maintenanceInterval1.getId());
    }

    @Test
    void Given_NonExistentMaintenanceIntervalIdentifierIsPassed_When_FindMaintenanceInterval_Then_ThrowsException() {
        UUID randomUUID = UUID.randomUUID();
        when(maintenanceIntervalRepository.findById(randomUUID)).thenReturn(Optional.empty());

        Optional<MaintenanceInterval> foundMaintenanceInterval = maintenanceIntervalService.findMaintenanceInterval(randomUUID);

        assertNotNull(foundMaintenanceInterval);
        assertFalse(foundMaintenanceInterval.isPresent());

        verify(maintenanceIntervalRepository, times(1)).findById(randomUUID);
    }

    /* FindAllMaintenanceIntervals method tests */

    @Test
    void Given_SelectedActiveIntervalsForGivenCluster_When_FindAllMaintenanceIntervals_Then_FoundAllMaintenanceIntervals() {
        UUID randomUUID = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        when(maintenanceIntervalRepository.findAllActiveIntervalsForGivenCluster(eq(randomUUID), any(LocalDateTime.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(maintenanceInterval1, maintenanceInterval2, maintenanceInterval3), pageable, 3));

        Page<MaintenanceInterval> maintenanceIntervalPage = maintenanceIntervalService.findAllMaintenanceIntervals(randomUUID, true, pageable);

        assertNotNull(maintenanceIntervalPage);
        assertNotNull(maintenanceIntervalPage.getContent());

        List<MaintenanceInterval> foundMaintenanceIntervals = maintenanceIntervalPage.getContent();

        assertNotNull(foundMaintenanceIntervals);
        assertEquals(3, foundMaintenanceIntervals.size());

        assertEquals(maintenanceInterval1, foundMaintenanceIntervals.getFirst());
        assertEquals(maintenanceInterval2, foundMaintenanceIntervals.get(1));
        assertEquals(maintenanceInterval3, foundMaintenanceIntervals.getLast());

        verify(maintenanceIntervalRepository, times(1))
                .findAllActiveIntervalsForGivenCluster(eq(randomUUID), any(LocalDateTime.class), eq(pageable));
    }

    @Test
    void Given_SelectedInactiveIntervalsForGivenCluster_When_FindAllMaintenanceIntervals_Then_FoundAllMaintenanceIntervals() {
        UUID randomUUID = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        when(maintenanceIntervalRepository.findAllHistoricalIntervalsForGivenCluster(eq(randomUUID), any(LocalDateTime.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(maintenanceInterval1, maintenanceInterval2, maintenanceInterval3), pageable, 3));

        Page<MaintenanceInterval> maintenanceIntervalPage = maintenanceIntervalService.findAllMaintenanceIntervals(randomUUID, false, pageable);

        assertNotNull(maintenanceIntervalPage);
        assertNotNull(maintenanceIntervalPage.getContent());

        List<MaintenanceInterval> foundMaintenanceIntervals = maintenanceIntervalPage.getContent();

        assertNotNull(foundMaintenanceIntervals);
        assertEquals(3, foundMaintenanceIntervals.size());

        assertEquals(maintenanceInterval1, foundMaintenanceIntervals.getFirst());
        assertEquals(maintenanceInterval2, foundMaintenanceIntervals.get(1));
        assertEquals(maintenanceInterval3, foundMaintenanceIntervals.getLast());

        verify(maintenanceIntervalRepository, times(1))
                .findAllHistoricalIntervalsForGivenCluster(eq(randomUUID), any(LocalDateTime.class), eq(pageable));
    }

    @Test
    void Given_SelectedActiveIntervalsForSystem_When_FindAllMaintenanceIntervals_Then_FoundAllMaintenanceIntervals() {
        Pageable pageable = PageRequest.of(0, 10);

        when(maintenanceIntervalRepository.findAllActiveIntervals(any(LocalDateTime.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(maintenanceInterval1, maintenanceInterval2, maintenanceInterval3), pageable, 3));

        Page<MaintenanceInterval> maintenanceIntervalPage = maintenanceIntervalService.findAllMaintenanceIntervals(null, true, pageable);

        assertNotNull(maintenanceIntervalPage);
        assertNotNull(maintenanceIntervalPage.getContent());

        List<MaintenanceInterval> foundMaintenanceIntervals = maintenanceIntervalPage.getContent();

        assertNotNull(foundMaintenanceIntervals);
        assertEquals(3, foundMaintenanceIntervals.size());

        assertEquals(maintenanceInterval1, foundMaintenanceIntervals.getFirst());
        assertEquals(maintenanceInterval2, foundMaintenanceIntervals.get(1));
        assertEquals(maintenanceInterval3, foundMaintenanceIntervals.getLast());

        verify(maintenanceIntervalRepository, times(1))
                .findAllActiveIntervals(any(LocalDateTime.class), eq(pageable));
    }

    @Test
    void Given_SelectedInactiveIntervalsForSystem_When_FindAllMaintenanceIntervals_Then_FoundAllMaintenanceIntervals() {
        Pageable pageable = PageRequest.of(0, 10);

        when(maintenanceIntervalRepository.findAllHistoricalIntervals(any(LocalDateTime.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(maintenanceInterval1, maintenanceInterval2, maintenanceInterval3), pageable, 3));

        Page<MaintenanceInterval> maintenanceIntervalPage = maintenanceIntervalService.findAllMaintenanceIntervals(null, false, pageable);

        assertNotNull(maintenanceIntervalPage);
        assertNotNull(maintenanceIntervalPage.getContent());

        List<MaintenanceInterval> foundMaintenanceIntervals = maintenanceIntervalPage.getContent();

        assertNotNull(foundMaintenanceIntervals);
        assertEquals(3, foundMaintenanceIntervals.size());

        assertEquals(maintenanceInterval1, foundMaintenanceIntervals.getFirst());
        assertEquals(maintenanceInterval2, foundMaintenanceIntervals.get(1));
        assertEquals(maintenanceInterval3, foundMaintenanceIntervals.getLast());

        verify(maintenanceIntervalRepository, times(1))
                .findAllHistoricalIntervals(any(LocalDateTime.class), eq(pageable));
    }

    /* FindAllMaintenanceIntervalsInTimePeriod method tests */

    @Test
    void Given_SomeMaintenanceIntervalExistInSelectedTimePeriod_When_FindAllMaintenanceIntervalsInTimePeriod_Then_ReturnAllFoundMaintenanceInterval() {
        LocalDateTime start = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime().plusHours(2);
        LocalDateTime end = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime().plusHours(4);

        when(maintenanceIntervalRepository.findAllIntervalsInGivenTimePeriod(existingClusterId, start, end))
                .thenReturn(List.of(maintenanceInterval1, maintenanceInterval2));

        List<MaintenanceInterval> foundMaintenanceInterval = maintenanceIntervalService
                .findAllMaintenanceIntervalsInTimePeriod(existingClusterId, start, end);

        assertNotNull(foundMaintenanceInterval);
        assertFalse(foundMaintenanceInterval.isEmpty());
        assertEquals(2, foundMaintenanceInterval.size());

        assertEquals(maintenanceInterval1, foundMaintenanceInterval.getFirst());
        assertEquals(maintenanceInterval2, foundMaintenanceInterval.getLast());

        verify(maintenanceIntervalRepository, times(1))
                .findAllIntervalsInGivenTimePeriod(existingClusterId, start, end);
    }

    @Test
    void Given_NoMaintenanceIntervalExistInSelectedTimePeriod_When_FindAllMaintenanceIntervalsInTimePeriod_Then_ReturnAllFoundMaintenanceInterval() {
        LocalDateTime start = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime().minusHours(48);
        LocalDateTime end = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime().minusHours(36);

        when(maintenanceIntervalRepository.findAllIntervalsInGivenTimePeriod(existingClusterId, start, end)).thenReturn(List.of());

        List<MaintenanceInterval> foundMaintenanceInterval = maintenanceIntervalService
                .findAllMaintenanceIntervalsInTimePeriod(existingClusterId, start, end);

        assertNotNull(foundMaintenanceInterval);
        assertTrue(foundMaintenanceInterval.isEmpty());

        verify(maintenanceIntervalRepository, times(1))
                .findAllIntervalsInGivenTimePeriod(existingClusterId, start, end);
    }

    /* FinishMaintenanceInterval method tests */

    @Test
    void Given_ExistingMaintenanceIntervalIdentifierIsPassedAndIntervalDidNotStart_When_FinishMaintenanceInterval_Then_FinishedMaintenanceIntervalSuccessfully() {
        when(maintenanceIntervalRepository.findById(maintenanceInterval1.getId())).thenReturn(Optional.of(maintenanceInterval1));
        doNothing().when(maintenanceIntervalRepository).delete(maintenanceInterval1);

        maintenanceIntervalService.finishMaintenanceInterval(maintenanceInterval1.getId());

        verify(maintenanceIntervalRepository, times(1)).findById(maintenanceInterval1.getId());
        verify(maintenanceIntervalRepository, times(1)).delete(maintenanceInterval1);
    }

    @Test
    void Given_NonExistentMaintenanceIntervalIdentifierIsPassed_When_FinishMaintenanceInterval_Then_ThrowsException() {
        UUID randomUUID = UUID.randomUUID();
        when(maintenanceIntervalRepository.findById(randomUUID)).thenReturn(Optional.empty());
        assertThrows(MaintenanceIntervalNotFound.class, () -> maintenanceIntervalService.finishMaintenanceInterval(randomUUID));
        verify(maintenanceIntervalRepository, times(1)).findById(randomUUID);
    }

    @Test
    void Given_ExistingIdentifierIsPassedMaintenanceIntervalForMaintenanceIntervalThatIsAlreadyFinished_When_FinishMaintenanceInterval_Then_ThrowsException() {
        LocalDateTime start = OffsetDateTime.now(ZoneOffset.UTC).minusHours(24).toLocalDateTime();
        LocalDateTime end = start.plusHours(8);

        maintenanceInterval1.setBeginAt(start);
        maintenanceInterval1.setEndAt(end);

        when(maintenanceIntervalRepository.findById(maintenanceInterval1.getId()))
                .thenReturn(Optional.of(maintenanceInterval1));

        assertThrows(MaintenanceIntervalAlreadyFinishedException.class,
                () -> maintenanceIntervalService.finishMaintenanceInterval(maintenanceInterval1.getId()));

        verify(maintenanceIntervalRepository, times(1))
                .findById(maintenanceInterval1.getId());
    }

    @Test
    void Given_ExistingMaintenanceIntervalIdentifierIsPassedAndIntervalHasAlreadyStarted_When_FinishMaintenanceInterval_Then_FinishedMaintenanceIntervalSuccessfully() throws Exception {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();

        MaintenanceInterval updatedMaintenanceInterval = new MaintenanceInterval(
                maintenanceInterval4.getCause(),
                maintenanceInterval4.getDescription(),
                maintenanceInterval4.getType(),
                maintenanceInterval4.getClusterId(),
                maintenanceInterval4.getBeginAt(),
                currentTime
        );

        Field id = AbstractEntity.class.getDeclaredField("id");
        id.setAccessible(true);
        id.set(updatedMaintenanceInterval, maintenanceInterval4.getId());
        id.setAccessible(false);

        when(maintenanceIntervalRepository.findById(maintenanceInterval4.getId())).thenReturn(Optional.of(maintenanceInterval4));
        when(maintenanceIntervalRepository.saveAndFlush(updatedMaintenanceInterval)).thenReturn(updatedMaintenanceInterval
        );

        maintenanceIntervalService.finishMaintenanceInterval(maintenanceInterval4.getId());

        verify(maintenanceIntervalRepository, times(1)).findById(maintenanceInterval4.getId());
        verify(maintenanceIntervalRepository, times(1)).saveAndFlush(maintenanceInterval4);
    }
}
