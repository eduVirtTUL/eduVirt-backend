package pl.lodz.p.it.eduvirt.unit.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.lodz.p.it.eduvirt.exceptions.course.CourseNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.pod.InvalidPodTypeException;
import pl.lodz.p.it.eduvirt.exceptions.pod.PodAlreadyExistsException;
import pl.lodz.p.it.eduvirt.exceptions.pod.PodDeletionException;
import pl.lodz.p.it.eduvirt.exceptions.pod.PodNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.resource_group.ResourceGroupNotFoundException;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
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
import pl.lodz.p.it.eduvirt.repository.CourseRepository;
import pl.lodz.p.it.eduvirt.repository.PodStatefulRepository;
import pl.lodz.p.it.eduvirt.repository.ReservationRepository;
import pl.lodz.p.it.eduvirt.repository.ResourceGroupRepository;
import pl.lodz.p.it.eduvirt.repository.TeamRepository;
import pl.lodz.p.it.eduvirt.service.impl.PodStatefulServiceImpl;

@ExtendWith(MockitoExtension.class)
public class PodStatefulServiceTest {

    @Mock
    private PodStatefulRepository podStatefulRepository;

    @Mock
    private ResourceGroupRepository resourceGroupRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private PodStatefulServiceImpl podStatefulService;

    private Course teamBasedCourse;
    private Team team;
    private ResourceGroup statefulGroup;
    private ResourceGroup statelessGroup;
    private PodStateful pod;
    private List<Reservation> reservations;

    @BeforeEach
    void setUp() {
        setupCourseAndTeam();
        setupResourceGroups();
        setupPod();
        setupReservations();
    }

    private void setupCourseAndTeam() {
        teamBasedCourse = Course.builder()
                .name("Team Course")
                .courseType(CourseType.TEAM_BASED)
                .build();
        setEntityId(teamBasedCourse, UUID.randomUUID());

        team = Team.builder()
                .name("Team1")
                .course(teamBasedCourse)
                .maxSize(5)
                .active(true)
                .statefulPods(new ArrayList<>())
                .build();
        setEntityId(team, UUID.randomUUID());
    }

    private void setupResourceGroups() {
        statefulGroup = new ResourceGroup();
        statefulGroup.setName("Stateful-RG");
        statefulGroup.setStateless(false);
        setEntityId(statefulGroup, UUID.randomUUID());

        statelessGroup = new ResourceGroup();
        statelessGroup.setName("Stateless-RG");
        statelessGroup.setStateless(true);
        setEntityId(statelessGroup, UUID.randomUUID());
    }

    private void setupPod() {
        pod = new PodStateful();
        pod.setResourceGroup(statefulGroup);
        pod.setTeam(team);
        setEntityId(pod, UUID.randomUUID());
    }

    private void setupReservations() {
        LocalDateTime now = LocalDateTime.now();
        reservations = List.of(
                createReservation(now.minusDays(1), now.plusDays(1), ReservationStatus.IN_PROGRESS),
                createReservation(now.plusDays(2), now.plusDays(3), ReservationStatus.PENDING)
        );
    }

    /* Create Operations Tests */

    @Test
    void Given_ValidData_When_CreateStatefulPod_Then_Success() {
        PodStateful newPod = new PodStateful();

        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(courseRepository.findById(teamBasedCourse.getId())).thenReturn(Optional.of(teamBasedCourse));
        when(resourceGroupRepository.findById(statefulGroup.getId())).thenReturn(Optional.of(statefulGroup));
        when(podStatefulRepository.existsByResourceGroupId(statefulGroup.getId())).thenReturn(false);
        when(podStatefulRepository.saveAndFlush(any(PodStateful.class))).thenReturn(newPod);

        PodStateful result = podStatefulService.createStatefulPod(newPod, team.getId(), statefulGroup.getId());

        assertNotNull(result);
        verify(podStatefulRepository).saveAndFlush(newPod);
    }

    @Test
    void Given_StatelessResourceGroup_When_CreatePod_Then_ThrowException() {
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(courseRepository.findById(teamBasedCourse.getId())).thenReturn(Optional.of(teamBasedCourse));
        when(resourceGroupRepository.findById(statelessGroup.getId())).thenReturn(Optional.of(statelessGroup));

        assertThrows(InvalidPodTypeException.class,
                () -> podStatefulService.createStatefulPod(new PodStateful(), team.getId(), statelessGroup.getId()));
    }

    /* Get Operations Tests */

    @Test
    void Given_ValidId_When_GetPodById_Then_ReturnPod() {
        when(podStatefulRepository.findById(pod.getId())).thenReturn(Optional.of(pod));

        PodStateful result = podStatefulService.getStatefulPodById(pod.getId());

        assertEquals(pod, result);
    }

    @Test
    void Given_TeamId_When_GetPodsByTeam_Then_ReturnList() {
        when(podStatefulRepository.findByTeamId(team.getId())).thenReturn(List.of(pod));

        List<PodStateful> result = podStatefulService.getStatefulPodsByTeam(team.getId());

        assertEquals(1, result.size());
        assertEquals(pod, result.getFirst());
    }

    /* Delete Operations Tests */

    @Test
    void Given_NoActiveReservations_When_DeletePod_Then_Success() {
        when(podStatefulRepository.findById(pod.getId())).thenReturn(Optional.of(pod));
        when(reservationRepository.findAllRgReservationsForGivenTeam(statefulGroup, team))
                .thenReturn(List.of());

        podStatefulService.deleteStatefulPod(pod.getId());

        verify(podStatefulRepository).delete(pod);
    }

    @Test
    void Given_ActiveReservations_When_DeletePod_Then_ThrowException() {
        when(podStatefulRepository.findById(pod.getId())).thenReturn(Optional.of(pod));
        when(reservationRepository.findAllRgReservationsForGivenTeam(statefulGroup, team))
                .thenReturn(reservations);

        assertThrows(PodDeletionException.class,
                () -> podStatefulService.deleteStatefulPod(pod.getId()));
    }

    /* Error Cases */

    @Test
    void Given_ExistingPod_When_CreatePod_Then_ThrowException() {
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(courseRepository.findById(teamBasedCourse.getId())).thenReturn(Optional.of(teamBasedCourse));
        when(resourceGroupRepository.findById(statefulGroup.getId())).thenReturn(Optional.of(statefulGroup));
        when(podStatefulRepository.existsByResourceGroupId(statefulGroup.getId())).thenReturn(true);

        assertThrows(PodAlreadyExistsException.class,
                () -> podStatefulService.createStatefulPod(new PodStateful(), team.getId(), statefulGroup.getId()));
    }

    @Test
    void Given_NonExistentTeam_When_CreatePod_Then_ThrowException() {
        when(teamRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(TeamNotFoundException.class,
                () -> podStatefulService.createStatefulPod(new PodStateful(), UUID.randomUUID(), statefulGroup.getId()));
    }

    @Test
    void Given_NonExistentPodId_When_GetById_Then_ThrowException() {
        UUID nonExistentId = UUID.randomUUID();
        when(podStatefulRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        PodNotFoundException exception = assertThrows(PodNotFoundException.class,
                () -> podStatefulService.getStatefulPodById(nonExistentId));
        assertEquals("Stateful pod with id %s not found".formatted(nonExistentId),
                exception.getMessage());
    }

    @Test
    void Given_CourseId_When_GetPodsByCourse_Then_ReturnList() {
        List<PodStateful> expectedPods = List.of(pod);
        when(podStatefulRepository.findByCourseId(teamBasedCourse.getId()))
                .thenReturn(expectedPods);

        List<PodStateful> result = podStatefulService.getStatefulPodsByCourse(teamBasedCourse.getId());

        assertEquals(expectedPods, result);
        verify(podStatefulRepository).findByCourseId(teamBasedCourse.getId());
    }

    @Test
    void Given_ResourceGroupId_When_GetPodsByResourceGroup_Then_ReturnList() {
        List<PodStateful> expectedPods = List.of(pod);
        when(podStatefulRepository.findByResourceGroupId(statefulGroup.getId()))
                .thenReturn(expectedPods);

        List<PodStateful> result = podStatefulService.getStatefulPodsByResourceGroup(statefulGroup.getId());

        assertEquals(expectedPods, result);
        verify(podStatefulRepository).findByResourceGroupId(statefulGroup.getId());
    }

    @Test
    void Given_NonExistentCourse_When_CreateStatefulPod_Then_ThrowException() {
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(courseRepository.findById(team.getCourse().getId())).thenReturn(Optional.empty());

        CourseNotFoundException exception = assertThrows(CourseNotFoundException.class,
                () -> podStatefulService.createStatefulPod(new PodStateful(), team.getId(), statefulGroup.getId()));
        assertEquals("Course with id %s not found.".formatted(team.getCourse().getId()),
                exception.getMessage());
    }

    @Test
    void Given_NonExistentResourceGroup_When_CreateStatefulPod_Then_ThrowException() {
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(courseRepository.findById(team.getCourse().getId())).thenReturn(Optional.of(teamBasedCourse));
        when(resourceGroupRepository.findById(statefulGroup.getId())).thenReturn(Optional.empty());

        ResourceGroupNotFoundException exception = assertThrows(ResourceGroupNotFoundException.class,
                () -> podStatefulService.createStatefulPod(new PodStateful(), team.getId(), statefulGroup.getId()));
        assertEquals("Resource group with id %s not found".formatted(statefulGroup.getId()),
                exception.getMessage());
    }

    /* Helper Methods */

    private Reservation createReservation(LocalDateTime start, LocalDateTime end, ReservationStatus status) {
        Reservation reservation = new Reservation();
        reservation.setStartTime(start);
        reservation.setEndTime(end);
        reservation.setStatus(status);
        reservation.setTeam(team);
        reservation.setResourceGroup(statefulGroup);
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