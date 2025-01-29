package pl.lodz.p.it.eduvirt.unit.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.lodz.p.it.eduvirt.dto.statistics.CourseStatsDto;
import pl.lodz.p.it.eduvirt.dto.statistics.SoloCourseStatsDto;
import pl.lodz.p.it.eduvirt.dto.statistics.TeamStatsDto;
import pl.lodz.p.it.eduvirt.repository.ReservationRepository;
import pl.lodz.p.it.eduvirt.repository.ResourceGroupPoolRepository;
import pl.lodz.p.it.eduvirt.service.CourseService;
import pl.lodz.p.it.eduvirt.service.impl.ReservationStatisticsServiceImpl;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import pl.lodz.p.it.eduvirt.entity.*;
import pl.lodz.p.it.eduvirt.entity.Reservation.ReservationStatus;
import pl.lodz.p.it.eduvirt.entity.key.CourseType;
import pl.lodz.p.it.eduvirt.repository.TeamRepository;


@ExtendWith(MockitoExtension.class)
public class ReservationStatisticsServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private CourseService courseService;

    @Mock
    private ResourceGroupPoolRepository resourceGroupPoolRepository;

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private ReservationStatisticsServiceImpl statisticsService;

    private Course soloCourse;
    private Course teamBasedCourse;
    private User user1;
    private User user2;
    private Team team1;
    private Team team2;
    private Team soloTeam1;
    private Team soloTeam2;
    private ResourceGroup statefulGroup;
    private ResourceGroup statelessGroup;
    private ResourceGroupPool pool;
    private List<Reservation> teamReservations;
    private List<Reservation> soloReservations;

    @BeforeEach
    void setUp() {
        setupUsers();
        setupCourses();
        setupTeams();
        setupResourceGroups();
        setupReservations();
    }

    private void setupUsers() {
        user1 = new User(UUID.randomUUID(), UUID.randomUUID(), "user1@test.com", "user1", "First1", "Last1");
        user2 = new User(UUID.randomUUID(), UUID.randomUUID(), "user2@test.com", "user2", "First2", "Last2");
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
                .build();
        setEntityId(team1, UUID.randomUUID());

        team2 = Team.builder()
                .name("Team2")
                .course(teamBasedCourse)
                .build();
        setEntityId(team2, UUID.randomUUID());

        soloTeam1 = Team.builder()
                .name("SC-Student1")
                .course(soloCourse)
                .users(new ArrayList<>(List.of(user1)))
                .build();
        setEntityId(soloTeam1, UUID.randomUUID());

        soloTeam2 = Team.builder()
                .name("SC-Student2")
                .course(soloCourse)
                .users(new ArrayList<>(List.of(user2)))
                .build();
        setEntityId(soloTeam2, UUID.randomUUID());
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

        pool = new ResourceGroupPool();
        pool.setResourceGroups(new ArrayList<>(List.of(statelessGroup)));
        setEntityId(pool, UUID.randomUUID());
    }

    private void setupReservations() {
        LocalDateTime now = LocalDateTime.now();

        teamReservations = List.of(
                createReservation(team1, statefulGroup, now.minusDays(2), now.minusDays(1)),
                createReservation(team1, statelessGroup, now.minusHours(12), now.plusHours(12)),
                createReservation(team2, statefulGroup, now.minusDays(3), now.minusDays(2)),
                createReservation(team1, statefulGroup, now.plusDays(1), now.plusDays(2))
        );

        teamReservations.forEach(r -> r.setStatus(ReservationStatus.COMPLETED));
        teamReservations.get(2).setStatus(ReservationStatus.IN_PROGRESS);
        teamReservations.get(3).setStatus(ReservationStatus.PENDING);

        soloReservations = List.of(
                createReservation(soloTeam1, statefulGroup, now.minusDays(1), now.minusHours(12)),
                createReservation(soloTeam2, statelessGroup, now.minusHours(6), now.plusHours(6)),
                createReservation(soloTeam1, statelessGroup, now.plusDays(1), now.plusDays(2))
        );

        soloReservations.forEach(r -> r.setStatus(ReservationStatus.COMPLETED));
        soloReservations.get(1).setStatus(ReservationStatus.IN_PROGRESS);
        soloReservations.get(2).setStatus(ReservationStatus.PENDING);

    }

    @Test
    void Given_TeamBasedCourse_When_GetStatistics_Then_ReturnCorrectStats() {
        when(courseService.getCourse(teamBasedCourse.getId())).thenReturn(teamBasedCourse);
        when(reservationRepository.findAllByCourseId(teamBasedCourse.getId()))
                .thenReturn(teamReservations);

        CourseStatsDto result = (CourseStatsDto) statisticsService.getCourseStatistics(teamBasedCourse.getId());

        assertEquals(teamBasedCourse.getId(), result.getCourseId());
        assertEquals(teamBasedCourse.getName(), result.getCourseName());
        assertEquals(3, result.getTotalReservations());
        assertEquals(2, result.getTotalTeams());
        assertTrue(result.getReservationsPerTeam().containsKey("Team1"));
        assertEquals(2, result.getReservationsPerTeam().get("Team1"));
    }


    @Test
    void Given_SoloCourse_When_GetStatistics_Then_ReturnCorrectStats() {
        when(courseService.getCourse(soloCourse.getId())).thenReturn(soloCourse);
        when(reservationRepository.findAllByCourseId(soloCourse.getId()))
                .thenReturn(soloReservations);

        SoloCourseStatsDto result = (SoloCourseStatsDto) statisticsService.getCourseStatistics(soloCourse.getId());

        assertEquals(soloCourse.getId(), result.getCourseId());
        assertEquals(soloCourse.getName(), result.getCourseName());
        assertEquals(2, result.getTotalReservations());
        assertTrue(result.getTotalHours() > 0);
        assertEquals(2, result.getTotalTeams());
    }

    @Test
    void Given_TeamWithReservations_When_GetStatistics_Then_ReturnCorrectStats() {
        List<Reservation> teamSpecificReservations = teamReservations.stream()
                .filter(r -> r.getTeam().equals(team1))
                .filter(r -> r.getStatus() == ReservationStatus.COMPLETED ||
                        r.getStatus() == ReservationStatus.IN_PROGRESS)
                .toList();

        lenient().when(teamRepository.findById(team1.getId()))
                .thenReturn(Optional.of(team1));
        lenient().when(reservationRepository.findAllByTeamId(team1.getId()))
                .thenReturn(teamSpecificReservations);
        lenient().when(resourceGroupPoolRepository.getResourceGroupPoolByResourceGroupsContaining(any(ResourceGroup.class)))
                .thenAnswer(invocation -> {
                    ResourceGroup group = invocation.getArgument(0);
                    return group.isStateless() ? Optional.of(pool) : Optional.empty();
                });

        TeamStatsDto result = statisticsService.getTeamStatistics(teamBasedCourse.getId(), team1.getId());

        assertEquals(team1.getId(), result.getTeamId());
        assertEquals(team1.getName(), result.getTeamName());
        assertEquals(2, result.getTotalReservations());
        assertEquals(48, result.getTotalHours());
        assertEquals(1, result.getStatefulResourceCount());
        assertEquals(1, result.getStatelessPoolCount());
        assertEquals(2, result.getTimeline().size());
    }

    @Test
    void Given_EmptyCourse_When_GetStatistics_Then_ReturnEmptyStats() {
        when(courseService.getCourse(teamBasedCourse.getId())).thenReturn(teamBasedCourse);
        when(reservationRepository.findAllByCourseId(teamBasedCourse.getId()))
                .thenReturn(List.of());

        CourseStatsDto result = (CourseStatsDto) statisticsService.getCourseStatistics(teamBasedCourse.getId());

        assertEquals(0, result.getTotalReservations());
        assertEquals(0, result.getTotalHours());
        assertEquals(0, result.getTotalTeams());
        assertTrue(result.getReservationsPerTeam().isEmpty());
        assertTrue(result.getHoursPerTeam().isEmpty());
    }

    @Test
    void Given_TeamWithNoReservations_When_GetStatistics_Then_ReturnEmptyStats() {
        when(teamRepository.findById(team1.getId()))
                .thenReturn(Optional.of(team1));
        when(reservationRepository.findAllByTeamId(team1.getId()))
                .thenReturn(List.of());

        TeamStatsDto result = statisticsService.getTeamStatistics(teamBasedCourse.getId(), team1.getId());

        assertEquals(team1.getId(), result.getTeamId());
        assertEquals(team1.getName(), result.getTeamName());
        assertEquals(0, result.getTotalReservations());
        assertEquals(0, result.getTotalHours());
        assertEquals(0.0, result.getAverageLength());
        assertEquals(0, result.getStatefulResourceCount());
        assertEquals(0, result.getStatelessPoolCount());
        assertTrue(result.getTimeline().isEmpty());
    }

    private Reservation createReservation(Team team, ResourceGroup resourceGroup, LocalDateTime start, LocalDateTime end) {
        Reservation reservation = new Reservation();
        reservation.setTeam(team);
        reservation.setResourceGroup(resourceGroup);
        reservation.setStartTime(start);
        reservation.setEndTime(end);
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