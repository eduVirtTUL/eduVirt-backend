package pl.lodz.p.it.eduvirt.unit.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.lodz.p.it.eduvirt.exceptions.course.CourseNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.course.IncorrectCourseTypeException;
import pl.lodz.p.it.eduvirt.exceptions.pod.InvalidPodTypeException;
import pl.lodz.p.it.eduvirt.exceptions.pod.PodAlreadyExistsException;
import pl.lodz.p.it.eduvirt.exceptions.pod.PodDeletionException;
import pl.lodz.p.it.eduvirt.exceptions.pod.PodNotFoundException;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import pl.lodz.p.it.eduvirt.entity.*;
import pl.lodz.p.it.eduvirt.entity.Reservation.ReservationStatus;
import pl.lodz.p.it.eduvirt.entity.key.CourseType;
import pl.lodz.p.it.eduvirt.exceptions.team.TeamNotFoundException;
import pl.lodz.p.it.eduvirt.repository.PodStatelessRepository;
import pl.lodz.p.it.eduvirt.repository.ReservationRepository;
import pl.lodz.p.it.eduvirt.repository.ResourceGroupPoolRepository;
import pl.lodz.p.it.eduvirt.repository.TeamRepository;
import pl.lodz.p.it.eduvirt.service.impl.PodStatelessServiceImpl;

@ExtendWith(MockitoExtension.class)
public class PodStatelessServiceTest {

    @Mock
    private PodStatelessRepository podStatelessRepository;

    @Mock
    private ResourceGroupPoolRepository resourceGroupPoolRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private PodStatelessServiceImpl podStatelessService;

    private Course soloCourse;
    private Course teamBasedCourse;
    private Team team1;
    private Team team2;
    private ResourceGroupPool pool1;
    private ResourceGroupPool pool2;
    private PodStateless pod;
    private List<Reservation> reservations;
    private ResourceGroup statelessGroup;

    @BeforeEach
    void setUp() {
        setupCourses();
        setupTeams();
        setupResourcePools();
        setupPods();
        setupReservations();
    }

    private void setupCourses() {
        soloCourse = Course.builder()
                .name("Solo Course")
                .courseType(CourseType.SOLO)
                .build();
        setEntityId(soloCourse, UUID.randomUUID());

        teamBasedCourse = Course.builder()
                .name("Team Course")
                .courseType(CourseType.TEAM_BASED)
                .build();
        setEntityId(teamBasedCourse, UUID.randomUUID());
    }

    private void setupTeams() {
        team1 = Team.builder()
                .name("Team1")
                .course(teamBasedCourse)
                .maxSize(5)
                .active(true)
                .statelessPods(new ArrayList<>())
                .build();
        setEntityId(team1, UUID.randomUUID());

        team2 = Team.builder()
                .name("Team2")
                .course(soloCourse)
                .maxSize(1)
                .active(true)
                .statelessPods(new ArrayList<>())
                .build();
        setEntityId(team2, UUID.randomUUID());
    }

    private void setupResourcePools() {
        statelessGroup = new ResourceGroup();
        statelessGroup.setName("Stateless-RG");
        statelessGroup.setStateless(true);
        setEntityId(statelessGroup, UUID.randomUUID());

        pool1 = new ResourceGroupPool();
        pool1.setCourse(teamBasedCourse);
        pool1.setResourceGroups(new ArrayList<>(List.of(statelessGroup)));
        setEntityId(pool1, UUID.randomUUID());

        pool2 = new ResourceGroupPool();
        pool2.setCourse(soloCourse);
        pool2.setResourceGroups(new ArrayList<>());
        setEntityId(pool2, UUID.randomUUID());
    }

    private void setupReservations() {
        LocalDateTime now = LocalDateTime.now();

        reservations = List.of(
                createReservation(now.minusDays(1), now.plusDays(1), ReservationStatus.IN_PROGRESS, statelessGroup),
                createReservation(now.plusDays(2), now.plusDays(3), ReservationStatus.PENDING, statelessGroup)
        );
    }

    private void setupPods() {
        pod = new PodStateless();
        pod.setTeam(team1);
        pod.setResourceGroupPool(pool1);
        pod.setCourse(teamBasedCourse);
        setEntityId(pod, UUID.randomUUID());
    }

    @Test
    void Given_ValidData_When_CreateStatelessPod_Then_Success() {
        PodStateless newPod = new PodStateless();

        when(teamRepository.findById(team1.getId())).thenReturn(Optional.of(team1));
        when(resourceGroupPoolRepository.findById(pool1.getId())).thenReturn(Optional.of(pool1));
        when(podStatelessRepository.existsByResourceGroupPoolIdAndTeamId(pool1.getId(), team1.getId()))
                .thenReturn(false);
        when(podStatelessRepository.saveAndFlush(any(PodStateless.class))).thenReturn(newPod);

        PodStateless result = podStatelessService.createStatelessPod(newPod, team1.getId(), pool1.getId());

        assertNotNull(result);
        verify(podStatelessRepository).saveAndFlush(newPod);
    }

    @Test
    void Given_BatchCreate_When_ValidData_Then_Success() {
        Team team3 = Team.builder()
                .name("Team3")
                .course(soloCourse)
                .maxSize(1)
                .active(true)
                .statelessPods(new ArrayList<>())
                .build();
        setEntityId(team3, UUID.randomUUID());

        Team team4 = Team.builder()
                .name("Team4")
                .course(soloCourse)
                .maxSize(1)
                .active(true)
                .statelessPods(new ArrayList<>())
                .build();
        setEntityId(team4, UUID.randomUUID());

        List<PodStateless> pods = List.of(new PodStateless(), new PodStateless());
        List<UUID> teamIds = List.of(team3.getId(), team4.getId());
        List<UUID> poolIds = List.of(pool2.getId(), pool2.getId());

        when(teamRepository.findById(team3.getId())).thenReturn(Optional.of(team3));
        when(teamRepository.findById(team4.getId())).thenReturn(Optional.of(team4));
        when(resourceGroupPoolRepository.findById(pool2.getId())).thenReturn(Optional.of(pool2));
        when(podStatelessRepository.existsByResourceGroupPoolIdAndTeamId(any(), any()))
                .thenReturn(false);
        when(podStatelessRepository.saveAndFlush(any(PodStateless.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<PodStateless> result = podStatelessService.createStatelessPodsBatch(pods, teamIds, poolIds);

        assertEquals(2, result.size());
        verify(podStatelessRepository, times(2)).saveAndFlush(any(PodStateless.class));
    }

    @Test
    void Given_BatchDelete_When_ValidData_Then_Success() {
        List<UUID> podIds = List.of(UUID.randomUUID(), UUID.randomUUID());
        List<PodStateless> pods = podIds.stream()
                .map(id -> {
                    PodStateless p = new PodStateless();
                    p.setCourse(soloCourse);
                    setEntityId(p, id);
                    return p;
                })
                .toList();

        when(podStatelessRepository.findAllById(podIds)).thenReturn(pods);

        podStatelessService.deleteStatelessPodsBatch(podIds);

        verify(podStatelessRepository).deleteAllById(podIds);
    }

    @Test
    void Given_ActiveReservations_When_DeletePod_Then_ThrowException() {
        ResourceGroup poolResourceGroup = pool1.getResourceGroups().getFirst();
        when(podStatelessRepository.findById(pod.getId())).thenReturn(Optional.of(pod));
        when(reservationRepository.findAllRgPoolReservationsForGivenTeam(eq(pool1), eq(team1)))
                .thenReturn(reservations);

        assertThrows(PodDeletionException.class,
                () -> podStatelessService.deleteStatelessPod(pod.getId()));
        verify(podStatelessRepository).findById(pod.getId());
        verify(reservationRepository).findAllRgPoolReservationsForGivenTeam(pool1, team1);
    }

    @Test
    void Given_NoActiveReservations_When_DeletePod_Then_Success() {
        when(podStatelessRepository.findById(pod.getId())).thenReturn(Optional.of(pod));
        when(reservationRepository.findAllRgPoolReservationsForGivenTeam(pool1, team1))
                .thenReturn(List.of());

        podStatelessService.deleteStatelessPod(pod.getId());

        verify(podStatelessRepository).delete(pod);
        verify(reservationRepository).findAllRgPoolReservationsForGivenTeam(pool1, team1);
    }

    @Test
    void Given_TeamId_When_GetPodsByTeam_Then_ReturnList() {
        when(podStatelessRepository.findByTeamId(team1.getId()))
                .thenReturn(List.of(pod));

        List<PodStateless> result = podStatelessService.getStatelessPodsByTeam(team1.getId());

        assertEquals(1, result.size());
        assertEquals(pod, result.getFirst());
    }

    @Test
    void Given_CourseId_When_GetPodsByCourse_Then_ReturnList() {
        when(podStatelessRepository.findByCourseId(teamBasedCourse.getId()))
                .thenReturn(List.of(pod));

        List<PodStateless> result = podStatelessService.getStatelessPodsByCourse(teamBasedCourse.getId());

        assertEquals(1, result.size());
        assertEquals(pod, result.getFirst());
    }

    @Test
    void Given_ResourcePoolId_When_GetPodsByPool_Then_ReturnList() {
        when(podStatelessRepository.findByResourceGroupPoolId(pool1.getId()))
                .thenReturn(List.of(pod));

        List<PodStateless> result = podStatelessService.getStatelessPodsByResourceGroupPool(pool1.getId());

        assertEquals(1, result.size());
        assertEquals(pod, result.getFirst());
    }

    @Test
    void Given_NonExistentPod_When_GetById_Then_ThrowException() {
        UUID podId = UUID.randomUUID();
        when(podStatelessRepository.findById(podId))
                .thenReturn(Optional.empty());

        assertThrows(PodNotFoundException.class,
                () -> podStatelessService.getStatelessPodById(podId));
    }

    @Test
    void Given_InvalidTeamId_When_CreatePod_Then_ThrowException() {
        when(teamRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(TeamNotFoundException.class,
                () -> podStatelessService.createStatelessPod(new PodStateless(), UUID.randomUUID(), pool1.getId()));
    }

    @Test
    void Given_InvalidPoolId_When_CreatePod_Then_ThrowException() {
        when(teamRepository.findById(team1.getId())).thenReturn(Optional.of(team1));
        when(resourceGroupPoolRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(CourseNotFoundException.class,
                () -> podStatelessService.createStatelessPod(new PodStateless(), team1.getId(), UUID.randomUUID()));
    }

    @Test
    void Given_MismatchedCourse_When_CreatePod_Then_ThrowException() {
        when(teamRepository.findById(team1.getId())).thenReturn(Optional.of(team1));
        when(resourceGroupPoolRepository.findById(pool2.getId())).thenReturn(Optional.of(pool2));

        assertThrows(InvalidPodTypeException.class,
                () -> podStatelessService.createStatelessPod(new PodStateless(), team1.getId(), pool2.getId()));
    }

    @Test
    void Given_ListSizeMismatch_When_BatchCreate_Then_ThrowException() {
        List<PodStateless> pods = List.of(new PodStateless());
        List<UUID> teamIds = List.of(UUID.randomUUID(), UUID.randomUUID());
        List<UUID> poolIds = List.of(UUID.randomUUID());

        assertThrows(IllegalArgumentException.class,
                () -> podStatelessService.createStatelessPodsBatch(pods, teamIds, poolIds));
    }

    @Test
    void Given_NonSoloCourse_When_BatchCreate_Then_ThrowException() {
        Team teamWithTeamBasedCourse = Team.builder()
                .name("TeamX")
                .course(teamBasedCourse)
                .build();
        setEntityId(teamWithTeamBasedCourse, UUID.randomUUID());

        when(teamRepository.findById(teamWithTeamBasedCourse.getId()))
                .thenReturn(Optional.of(teamWithTeamBasedCourse));

        assertThrows(IncorrectCourseTypeException.class,
                () -> podStatelessService.createStatelessPodsBatch(
                        List.of(new PodStateless()),
                        List.of(teamWithTeamBasedCourse.getId()),
                        List.of(pool1.getId())
                ));
    }

    @Test
    void Given_DuplicateTeams_When_BatchCreate_Then_ThrowException() {
        Team soloTeam = Team.builder()
                .name("SoloTeam")
                .course(soloCourse)
                .maxSize(1)
                .active(true)
                .statelessPods(new ArrayList<>())
                .build();
        setEntityId(soloTeam, UUID.randomUUID());

        List<PodStateless> pods = List.of(new PodStateless(), new PodStateless());
        List<UUID> teamIds = List.of(soloTeam.getId(), soloTeam.getId());
        List<UUID> poolIds = List.of(pool2.getId(), pool2.getId());

        when(teamRepository.findById(soloTeam.getId())).thenReturn(Optional.of(soloTeam));

        PodAlreadyExistsException exception = assertThrows(PodAlreadyExistsException.class,
                () -> podStatelessService.createStatelessPodsBatch(pods, teamIds, poolIds));
        assertEquals("Cannot create multiple pods for the same team", exception.getMessage());
    }

    @Test
    void Given_ExistingPod_When_CreatePod_Then_ThrowException() {
        when(teamRepository.findById(team1.getId())).thenReturn(Optional.of(team1));
        when(resourceGroupPoolRepository.findById(pool1.getId())).thenReturn(Optional.of(pool1));
        when(podStatelessRepository.existsByResourceGroupPoolIdAndTeamId(pool1.getId(), team1.getId()))
                .thenReturn(true);

        assertThrows(PodAlreadyExistsException.class,
                () -> podStatelessService.createStatelessPod(new PodStateless(), team1.getId(), pool1.getId()));
    }

    @Test
    void Given_EmptyPodList_When_BatchDelete_Then_ReturnEarly() {
        podStatelessService.deleteStatelessPodsBatch(List.of());
        verify(podStatelessRepository, never()).findAllById(any());
    }

    @Test
    void Given_PodNotFound_When_BatchDelete_Then_ThrowException() {
        List<UUID> podIds = List.of(UUID.randomUUID());
        when(podStatelessRepository.findAllById(podIds)).thenReturn(List.of());

        assertThrows(PodNotFoundException.class,
                () -> podStatelessService.deleteStatelessPodsBatch(podIds));
    }

    @Test
    void Given_NonMatchingCourse_When_BatchDelete_Then_ThrowException() {
        Course soloCourse1 = Course.builder()
                .name("Solo Course 1")
                .courseType(CourseType.SOLO)
                .build();
        setEntityId(soloCourse1, UUID.randomUUID());

        Course soloCourse2 = Course.builder()
                .name("Solo Course 2")
                .courseType(CourseType.SOLO)
                .build();
        setEntityId(soloCourse2, UUID.randomUUID());

        PodStateless pod1 = new PodStateless();
        pod1.setCourse(soloCourse1);
        setEntityId(pod1, UUID.randomUUID());

        PodStateless pod2 = new PodStateless();
        pod2.setCourse(soloCourse2);
        setEntityId(pod2, UUID.randomUUID());

        List<UUID> podIds = List.of(pod1.getId(), pod2.getId());
        List<PodStateless> pods = List.of(pod1, pod2);

        when(podStatelessRepository.findAllById(podIds))
                .thenReturn(pods);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> podStatelessService.deleteStatelessPodsBatch(podIds));
        assertEquals("All pods must belong to the same course", exception.getMessage());
    }

    @Test
    void Given_ActiveReservation_When_Delete_Then_ThrowException() {
        Reservation activeReservation = createReservation(
                LocalDateTime.now(ZoneOffset.UTC).minusHours(1),
                LocalDateTime.now(ZoneOffset.UTC).plusHours(1),
                ReservationStatus.IN_PROGRESS,
                statelessGroup
        );

        when(podStatelessRepository.findById(pod.getId())).thenReturn(Optional.of(pod));
        when(reservationRepository.findAllRgPoolReservationsForGivenTeam(pool1, team1))
                .thenReturn(List.of(activeReservation));

        assertThrows(PodDeletionException.class,
                () -> podStatelessService.deleteStatelessPod(pod.getId()));
    }

    @Test
    void Given_TeamsFromDifferentCourses_When_BatchCreate_Then_ThrowException() {
        Team team1 = Team.builder()
                .name("Team1")
                .course(soloCourse)
                .build();
        setEntityId(team1, UUID.randomUUID());

        Course differentSoloCourse = Course.builder()
                .name("Different Solo Course")
                .courseType(CourseType.SOLO)
                .build();
        setEntityId(differentSoloCourse, UUID.randomUUID());

        Team team2 = Team.builder()
                .name("Team2")
                .course(differentSoloCourse)
                .build();
        setEntityId(team2, UUID.randomUUID());

        when(teamRepository.findById(team1.getId())).thenReturn(Optional.of(team1));
        when(teamRepository.findById(team2.getId())).thenReturn(Optional.of(team2));

        assertThrows(IllegalArgumentException.class,
                () -> podStatelessService.createStatelessPodsBatch(
                        List.of(new PodStateless(), new PodStateless()),
                        List.of(team1.getId(), team2.getId()),
                        List.of(pool1.getId(), pool1.getId())
                ));
    }

    @Test
    void Given_ResourceGroupPoolMismatch_When_BatchCreate_Then_ThrowException() {
        Team team = Team.builder()
                .name("Team")
                .course(soloCourse)
                .build();
        setEntityId(team, UUID.randomUUID());

        ResourceGroupPool differentPool = new ResourceGroupPool();
        differentPool.setCourse(teamBasedCourse);
        setEntityId(differentPool, UUID.randomUUID());

        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(resourceGroupPoolRepository.findById(differentPool.getId()))
                .thenReturn(Optional.of(differentPool));

        assertThrows(InvalidPodTypeException.class,
                () -> podStatelessService.createStatelessPodsBatch(
                        List.of(new PodStateless()),
                        List.of(team.getId()),
                        List.of(differentPool.getId())
                ));
    }

    @Test
    void Given_NonSoloCourse_When_BatchDelete_Then_ThrowException() {
        PodStateless podFromTeamCourse = new PodStateless();
        podFromTeamCourse.setCourse(teamBasedCourse);
        setEntityId(podFromTeamCourse, UUID.randomUUID());

        when(podStatelessRepository.findAllById(List.of(podFromTeamCourse.getId())))
                .thenReturn(List.of(podFromTeamCourse));

        assertThrows(IncorrectCourseTypeException.class,
                () -> podStatelessService.deleteStatelessPodsBatch(List.of(podFromTeamCourse.getId())));
    }

    @Test
    void Given_ExistingPod_When_BatchCreate_Then_SkipExisting() {
        Team team = Team.builder()
                .name("Team")
                .course(soloCourse)
                .build();
        setEntityId(team, UUID.randomUUID());

        ResourceGroupPool sameCoursePoo1 = new ResourceGroupPool();
        sameCoursePoo1.setCourse(soloCourse);
        setEntityId(sameCoursePoo1, UUID.randomUUID());

        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(resourceGroupPoolRepository.findById(sameCoursePoo1.getId()))
                .thenReturn(Optional.of(sameCoursePoo1));
        when(podStatelessRepository.existsByResourceGroupPoolIdAndTeamId(
                sameCoursePoo1.getId(), team.getId()))
                .thenReturn(true);

        List<PodStateless> result = podStatelessService.createStatelessPodsBatch(
                List.of(new PodStateless()),
                List.of(team.getId()),
                List.of(sameCoursePoo1.getId())
        );

        assertTrue(result.isEmpty());
        verify(podStatelessRepository).existsByResourceGroupPoolIdAndTeamId(
                sameCoursePoo1.getId(), team.getId());
        verify(podStatelessRepository, never()).saveAndFlush(any());
    }

    private Reservation createReservation(LocalDateTime start, LocalDateTime end,
                                          ReservationStatus status, ResourceGroup resourceGroup) {
        Reservation reservation = new Reservation();
        reservation.setStartTime(start);
        reservation.setEndTime(end);
        reservation.setStatus(status);
        reservation.setTeam(team1);
        reservation.setResourceGroup(resourceGroup);
        setEntityId(reservation, UUID.randomUUID());
        return reservation;
    }

    @SneakyThrows
    private void setEntityId(AbstractEntity entity, UUID id) {
        Field idField = AbstractEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
        idField.setAccessible(false);
    }
}