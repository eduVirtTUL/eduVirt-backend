package pl.lodz.p.it.eduvirt.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.sdk4.types.Cluster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import pl.lodz.p.it.eduvirt.entity.eduvirt.AbstractEntity;
import pl.lodz.p.it.eduvirt.entity.eduvirt.Updatable;
import pl.lodz.p.it.eduvirt.entity.eduvirt.reservation.MaintenanceInterval;
import pl.lodz.p.it.eduvirt.exceptions.maintenance_interval.MaintenanceIntervalConflictException;
import pl.lodz.p.it.eduvirt.exceptions.maintenance_interval.MaintenanceIntervalInvalidTimeWindowException;
import pl.lodz.p.it.eduvirt.exceptions.maintenance_interval.MaintenanceIntervalNotFound;
import pl.lodz.p.it.eduvirt.repository.eduvirt.MaintenanceIntervalRepository;
import pl.lodz.p.it.eduvirt.service.impl.MaintenanceIntervalServiceImpl;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MaintenanceIntervalServiceTest {

    @Mock
    private MaintenanceIntervalRepository maintenanceIntervalRepository;

    @InjectMocks
    private MaintenanceIntervalServiceImpl maintenanceIntervalService;

    @Mock
    private Cluster cluster;

    /* Initialization */

    private final UUID existingClusterId = UUID.randomUUID();
    private final UUID nonExistentClusterId = UUID.randomUUID();

    private MaintenanceInterval maintenanceInterval1;
    private MaintenanceInterval maintenanceInterval2;
    private MaintenanceInterval maintenanceInterval3;
    private MaintenanceInterval maintenanceInterval4;

    @BeforeEach
    public void prepareTestData() throws Exception {
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

        Field id = AbstractEntity.class.getDeclaredField("id");
        id.setAccessible(true);
        id.set(maintenanceInterval1, UUID.randomUUID());
        id.set(maintenanceInterval2, UUID.randomUUID());
        id.set(maintenanceInterval3, UUID.randomUUID());
        id.set(maintenanceInterval4, UUID.randomUUID());
        id.setAccessible(false);

        Field version = Updatable.class.getDeclaredField("version");
        version.setAccessible(true);
        version.set(maintenanceInterval1, 1L);
        version.set(maintenanceInterval2, 1L);
        version.set(maintenanceInterval3, 1L);
        version.set(maintenanceInterval4, 1L);
        version.setAccessible(false);
    }

    /* Tests */

    /* CreateClusterMaintenanceInterval method tests */

    @Test
    public void Given_AllDataMatchesRequiredConditions_When_CreateClusterMaintenanceInterval_Then_NewIntervalCreateSuccessfully() {
        String cause = "example_cause";
        String description = "example_description";
        UUID clusterId = UUID.randomUUID();
        LocalDateTime start = OffsetDateTime.now(ZoneOffset.UTC).plusHours(2).toLocalDateTime();
        LocalDateTime end = OffsetDateTime.now(ZoneOffset.UTC).plusHours(3).toLocalDateTime();

        MaintenanceInterval exampleMaintenanceInterval = new MaintenanceInterval(
                cause, description, MaintenanceInterval.IntervalType.CLUSTER, existingClusterId, start, end);

        when(cluster.id()).thenReturn(clusterId.toString());
        when(maintenanceIntervalRepository.findAllIntervalsInGivenTimePeriod(Mockito.eq(start), Mockito.eq(end),
                Mockito.eq(MaintenanceInterval.IntervalType.CLUSTER), Mockito.eq(clusterId))).thenReturn(List.of());
        when(maintenanceIntervalRepository.saveAndFlush(exampleMaintenanceInterval)).thenReturn(exampleMaintenanceInterval);

        maintenanceIntervalService.createClusterMaintenanceInterval(cluster, cause, description, start, end);

        verify(cluster, timeout(1)).id();
        verify(maintenanceIntervalRepository, times(1)).findAllIntervalsInGivenTimePeriod(
                Mockito.eq(start), Mockito.eq(end), Mockito.eq(MaintenanceInterval.IntervalType.CLUSTER), Mockito.eq(clusterId));
        verify(maintenanceIntervalRepository, times(1)).saveAndFlush(Mockito.eq(exampleMaintenanceInterval));
    }

    @Test
    public void Given_MaintenanceIntervalBeginAtAfterEndAt_When_CreateClusterMaintenanceInterval_Then_ThrowsException() {
        String cause = "example_cause";
        String description = "example_description";
        LocalDateTime start = OffsetDateTime.now(ZoneOffset.UTC).plusHours(4).toLocalDateTime();
        LocalDateTime end = OffsetDateTime.now(ZoneOffset.UTC).plusHours(2).toLocalDateTime();

        assertThrows(MaintenanceIntervalInvalidTimeWindowException.class, () -> maintenanceIntervalService
                .createClusterMaintenanceInterval(cluster, cause, description, start, end));
    }

    @Test
    public void Given_MaintenanceIntervalBeginAtIsInThePast_When_CreateClusterMaintenanceInterval_Then_ThrowsException() {
        String cause = "example_cause";
        String description = "example_description";
        LocalDateTime start = OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(1).toLocalDateTime();
        LocalDateTime end = OffsetDateTime.now(ZoneOffset.UTC).plusHours(2).plusMinutes(59).toLocalDateTime();

        assertThrows(MaintenanceIntervalInvalidTimeWindowException.class, () -> maintenanceIntervalService
                .createClusterMaintenanceInterval(cluster, cause, description, start, end));
    }

    @Test
    public void Given_OtherMaintenanceIntervalsExistForGivenCluster_When_CreateClusterMaintenanceInterval_Then_ThrowsException() {
        String cause = "example_cause";
        String description = "example_description";
        UUID clusterId = UUID.randomUUID();
        LocalDateTime start = OffsetDateTime.now(ZoneOffset.UTC).plusHours(2).toLocalDateTime();
        LocalDateTime end = OffsetDateTime.now(ZoneOffset.UTC).plusHours(3).toLocalDateTime();

        when(cluster.id()).thenReturn(clusterId.toString());
        when(maintenanceIntervalRepository.findAllIntervalsInGivenTimePeriod(Mockito.eq(start), Mockito.eq(end),
                Mockito.eq(MaintenanceInterval.IntervalType.CLUSTER), Mockito.eq(clusterId))).thenReturn(List.of(maintenanceInterval1, maintenanceInterval2));

        assertThrows(MaintenanceIntervalConflictException.class, () -> maintenanceIntervalService
                .createClusterMaintenanceInterval(cluster, cause, description, start, end));

        verify(cluster, timeout(1)).id();
        verify(maintenanceIntervalRepository, times(1)).findAllIntervalsInGivenTimePeriod(
                Mockito.eq(start), Mockito.eq(end), Mockito.eq(MaintenanceInterval.IntervalType.CLUSTER), Mockito.eq(clusterId));
    }

    /* CreateSystemMaintenanceInterval method tests */

    @Test
    public void Given_AllDataMatchesRequiredConditions_When_CreateSystemMaintenanceInterval_Then_CreatesNewSystemMaintenanceIntervalSuccessfully() {
        String cause = "example_cause";
        String description = "example_description";
        LocalDateTime start = OffsetDateTime.now(ZoneOffset.UTC).plusHours(2).toLocalDateTime();
        LocalDateTime end = OffsetDateTime.now(ZoneOffset.UTC).plusHours(4).toLocalDateTime();

        MaintenanceInterval exampleMaintenanceInterval = new MaintenanceInterval(
                cause, description, MaintenanceInterval.IntervalType.SYSTEM, null, start, end);

        when(maintenanceIntervalRepository.findAllIntervalsInGivenTimePeriod(Mockito.eq(start), Mockito.eq(end),
                Mockito.eq(MaintenanceInterval.IntervalType.SYSTEM), Mockito.eq(null))).thenReturn(List.of());
        when(maintenanceIntervalRepository.saveAndFlush(exampleMaintenanceInterval)).thenReturn(exampleMaintenanceInterval);

        maintenanceIntervalService.createSystemMaintenanceInterval(cause, description, start, end);

        verify(maintenanceIntervalRepository, times(1)).findAllIntervalsInGivenTimePeriod(
                Mockito.eq(start), Mockito.eq(end), Mockito.eq(MaintenanceInterval.IntervalType.SYSTEM), Mockito.eq(null));
        verify(maintenanceIntervalRepository, times(1)).saveAndFlush(Mockito.eq(exampleMaintenanceInterval));
    }

    @Test
    public void Given_MaintenanceIntervalBeginAtAfterEndAt_When_CreateSystemMaintenanceInterval_Then_ThrowsException() {
        String cause = "example_cause";
        String description = "example_description";
        LocalDateTime start = OffsetDateTime.now(ZoneOffset.UTC).plusHours(4).toLocalDateTime();
        LocalDateTime end = OffsetDateTime.now(ZoneOffset.UTC).plusHours(2).toLocalDateTime();

        assertThrows(MaintenanceIntervalInvalidTimeWindowException.class, () -> maintenanceIntervalService
                .createSystemMaintenanceInterval(cause, description, start, end));
    }

    @Test
    public void Given_MaintenanceIntervalBeginAtIsInThePast_When_CreateSystemMaintenanceInterval_Then_ThrowsException() {
        String cause = "example_cause";
        String description = "example_description";
        LocalDateTime start = OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(1).toLocalDateTime();
        LocalDateTime end = OffsetDateTime.now(ZoneOffset.UTC).plusHours(2).plusHours(59).toLocalDateTime();

        assertThrows(MaintenanceIntervalInvalidTimeWindowException.class, () -> maintenanceIntervalService
                .createSystemMaintenanceInterval(cause, description, start, end));
    }

    @Test
    public void Given_OtherMaintenanceIntervalsExistForGivenCluster_When_CreateSystemMaintenanceInterval_Then_ThrowsException() {
        String cause = "example_cause";
        String description = "example_description";
        LocalDateTime start = OffsetDateTime.now(ZoneOffset.UTC).plusHours(2).toLocalDateTime();
        LocalDateTime end = OffsetDateTime.now(ZoneOffset.UTC).plusHours(4).toLocalDateTime();

        when(maintenanceIntervalRepository.findAllIntervalsInGivenTimePeriod(Mockito.eq(start), Mockito.eq(end),
                Mockito.eq(MaintenanceInterval.IntervalType.SYSTEM), Mockito.eq(null))).thenReturn(List.of(maintenanceInterval1, maintenanceInterval2));

        assertThrows(MaintenanceIntervalConflictException.class, () -> maintenanceIntervalService
                .createSystemMaintenanceInterval(cause, description, start, end));

        verify(maintenanceIntervalRepository, times(1)).findAllIntervalsInGivenTimePeriod(
                Mockito.eq(start), Mockito.eq(end), Mockito.eq(MaintenanceInterval.IntervalType.SYSTEM), Mockito.eq(null));
    }

    /* FindMaintenanceInterval method tests */

    @Test
    public void Given_ExistingMaintenanceIntervalIdentifierIsPassed_When_FindMaintenanceInterval_Then_ReturnsFoundMaintenanceInterval() {
        when(maintenanceIntervalRepository.findById(Mockito.eq(maintenanceInterval1.getId()))).thenReturn(Optional.of(maintenanceInterval1));

        Optional<MaintenanceInterval> foundMaintenanceInterval = maintenanceIntervalService.findMaintenanceInterval(maintenanceInterval1.getId());

        assertNotNull(foundMaintenanceInterval);
        assertTrue(foundMaintenanceInterval.isPresent());

        MaintenanceInterval maintenanceInterval = foundMaintenanceInterval.get();

        assertNotNull(maintenanceInterval);
        assertEquals(maintenanceInterval1, maintenanceInterval);

        verify(maintenanceIntervalRepository, times(1)).findById(Mockito.eq(maintenanceInterval1.getId()));
    }

    @Test
    public void Given_NonExistentMaintenanceIntervalIdentifierIsPassed_When_FindMaintenanceInterval_Then_ThrowsException() {
        UUID randomUUID = UUID.randomUUID();
        when(maintenanceIntervalRepository.findById(Mockito.eq(randomUUID))).thenReturn(Optional.empty());

        Optional<MaintenanceInterval> foundMaintenanceInterval = maintenanceIntervalService.findMaintenanceInterval(randomUUID);

        assertNotNull(foundMaintenanceInterval);
        assertFalse(foundMaintenanceInterval.isPresent());

        verify(maintenanceIntervalRepository, times(1)).findById(Mockito.eq(randomUUID));
    }

    /* FindAllMaintenanceIntervals method tests */

    @Test
    public void Given_SelectedActiveIntervalsForGivenCluster_When_FindAllMaintenanceIntervals_Then_FoundAllMaintenanceIntervals() {
        UUID randomUUID = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        when(maintenanceIntervalRepository.findAllActiveIntervalsForGivenCluster(Mockito.eq(randomUUID), Mockito.eq(pageable)))
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
                .findAllActiveIntervalsForGivenCluster(Mockito.eq(randomUUID), Mockito.eq(pageable));
    }

    @Test
    public void Given_SelectedInactiveIntervalsForGivenCluster_When_FindAllMaintenanceIntervals_Then_FoundAllMaintenanceIntervals() {
        UUID randomUUID = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        when(maintenanceIntervalRepository.findAllHistoricalIntervalsForGivenCluster(Mockito.eq(randomUUID), Mockito.eq(pageable)))
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
                .findAllHistoricalIntervalsForGivenCluster(Mockito.eq(randomUUID), Mockito.eq(pageable));
    }

    @Test
    public void Given_SelectedActiveIntervalsForSystem_When_FindAllMaintenanceIntervals_Then_FoundAllMaintenanceIntervals() {
        Pageable pageable = PageRequest.of(0, 10);

        when(maintenanceIntervalRepository.findAllActiveIntervals(Mockito.eq(pageable)))
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

        verify(maintenanceIntervalRepository, times(1)).findAllActiveIntervals(Mockito.eq(pageable));
    }

    @Test
    public void Given_SelectedInactiveIntervalsForSystem_When_FindAllMaintenanceIntervals_Then_FoundAllMaintenanceIntervals() {
        Pageable pageable = PageRequest.of(0, 10);

        when(maintenanceIntervalRepository.findAllHistoricalIntervals(Mockito.eq(pageable)))
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

        verify(maintenanceIntervalRepository, times(1)).findAllHistoricalIntervals(Mockito.eq(pageable));
    }

    /* FindAllMaintenanceIntervalsInTimePeriod method tests */

    @Test
    public void Given_SomeMaintenanceIntervalExistInSelectedTimePeriod_When_FindAllMaintenanceIntervalsInTimePeriod_Then_ReturnAllFoundMaintenanceInterval() {
        LocalDateTime start = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime().plusHours(2);
        LocalDateTime end = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime().plusHours(4);

        when(maintenanceIntervalRepository.findAllIntervalsInGivenTimePeriod(Mockito.eq(start), Mockito.eq(end)))
                .thenReturn(List.of(maintenanceInterval1, maintenanceInterval2));

        List<MaintenanceInterval> foundMaintenanceInterval = maintenanceIntervalService.findAllMaintenanceIntervalsInTimePeriod(start, end);

        assertNotNull(foundMaintenanceInterval);
        assertFalse(foundMaintenanceInterval.isEmpty());
        assertEquals(2, foundMaintenanceInterval.size());

        assertEquals(maintenanceInterval1, foundMaintenanceInterval.getFirst());
        assertEquals(maintenanceInterval2, foundMaintenanceInterval.getLast());

        verify(maintenanceIntervalRepository, times(1)).findAllIntervalsInGivenTimePeriod(Mockito.eq(start), Mockito.eq(end));
    }

    @Test
    public void Given_NoMaintenanceIntervalExistInSelectedTimePeriod_When_FindAllMaintenanceIntervalsInTimePeriod_Then_ReturnAllFoundMaintenanceInterval() {
        LocalDateTime start = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime().minusHours(48);
        LocalDateTime end = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime().minusHours(36);

        when(maintenanceIntervalRepository.findAllIntervalsInGivenTimePeriod(Mockito.eq(start), Mockito.eq(end))).thenReturn(List.of());

        List<MaintenanceInterval> foundMaintenanceInterval = maintenanceIntervalService.findAllMaintenanceIntervalsInTimePeriod(start, end);

        assertNotNull(foundMaintenanceInterval);
        assertTrue(foundMaintenanceInterval.isEmpty());

        verify(maintenanceIntervalRepository, times(1)).findAllIntervalsInGivenTimePeriod(Mockito.eq(start), Mockito.eq(end));
    }

    /* FinishMaintenanceInterval method tests */

    @Test
    public void Given_ExistingMaintenanceIntervalIdentifierIsPassedAndIntervalDidNotStart_When_FinishMaintenanceInterval_Then_FinishedMaintenanceIntervalSuccessfully() {
        when(maintenanceIntervalRepository.findById(maintenanceInterval1.getId())).thenReturn(Optional.of(maintenanceInterval1));
        doNothing().when(maintenanceIntervalRepository).delete(Mockito.eq(maintenanceInterval1));

        maintenanceIntervalService.finishMaintenanceInterval(maintenanceInterval1.getId());

        verify(maintenanceIntervalRepository, times(1)).findById(Mockito.eq(maintenanceInterval1.getId()));
        verify(maintenanceIntervalRepository, times(1)).delete(Mockito.eq(maintenanceInterval1));
    }

    @Test
    public void Given_NonExistentMaintenanceIntervalIdentifierIsPassed_When_FinishMaintenanceInterval_Then_ThrowsException() {
        UUID randomUUID = UUID.randomUUID();
        when(maintenanceIntervalRepository.findById(randomUUID)).thenReturn(Optional.empty());
        assertThrows(MaintenanceIntervalNotFound.class, () -> maintenanceIntervalService.finishMaintenanceInterval(randomUUID));
        verify(maintenanceIntervalRepository, times(1)).findById(Mockito.eq(randomUUID));
    }

    @Test
    public void Given_ExistingMaintenanceIntervalIdentifierIsPassedAndIntervalHasAlreadyStarted_When_FinishMaintenanceInterval_Then_FinishedMaintenanceIntervalSuccessfully() throws Exception {
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
        when(maintenanceIntervalRepository.saveAndFlush(Mockito.eq(updatedMaintenanceInterval))).thenReturn(updatedMaintenanceInterval
        );

        maintenanceIntervalService.finishMaintenanceInterval(maintenanceInterval4.getId());

        verify(maintenanceIntervalRepository, times(1)).findById(Mockito.eq(maintenanceInterval4.getId()));
        verify(maintenanceIntervalRepository, times(1)).saveAndFlush(Mockito.eq(maintenanceInterval4));
    }
}
