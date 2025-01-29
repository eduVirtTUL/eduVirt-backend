package pl.lodz.p.it.eduvirt.unit.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import pl.lodz.p.it.eduvirt.exceptions.access_key.DuplicateKeyValueException;
import pl.lodz.p.it.eduvirt.exceptions.course.IncorrectCourseTypeException;
import pl.lodz.p.it.eduvirt.util.etag.ETagHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import pl.lodz.p.it.eduvirt.entity.*;
import pl.lodz.p.it.eduvirt.entity.key.AccessKey;
import pl.lodz.p.it.eduvirt.entity.key.CourseAccessKey;
import pl.lodz.p.it.eduvirt.entity.key.CourseType;
import pl.lodz.p.it.eduvirt.entity.key.TeamAccessKey;
import pl.lodz.p.it.eduvirt.exceptions.team.TeamAlreadyExistsException;
import pl.lodz.p.it.eduvirt.exceptions.team.TeamConflictException;
import pl.lodz.p.it.eduvirt.exceptions.team.TeamDeletionException;
import pl.lodz.p.it.eduvirt.exceptions.team.TeamNotActiveException;
import pl.lodz.p.it.eduvirt.exceptions.team.TeamNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.team.TeamSizeException;
import pl.lodz.p.it.eduvirt.exceptions.team.TeamUserAlreadyMemberException;
import pl.lodz.p.it.eduvirt.exceptions.team.TeamUserNotMemberException;
import pl.lodz.p.it.eduvirt.exceptions.user.UserAlreadyInCourseException;
import pl.lodz.p.it.eduvirt.exceptions.user.UserDoesntBelongToCourseException;
import pl.lodz.p.it.eduvirt.exceptions.user.UserNotFoundException;
import pl.lodz.p.it.eduvirt.repository.TeamRepository;
import pl.lodz.p.it.eduvirt.repository.UserRepository;
import pl.lodz.p.it.eduvirt.repository.key.CourseAccessKeyRepository;
import pl.lodz.p.it.eduvirt.repository.key.TeamAccessKeyRepository;
import pl.lodz.p.it.eduvirt.service.AccessKeyService;
import pl.lodz.p.it.eduvirt.service.KeyGeneratorService;
import pl.lodz.p.it.eduvirt.service.impl.TeamServiceImpl;

@ExtendWith(MockitoExtension.class)
public class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamAccessKeyRepository teamKeyRepository;

    @Mock
    private CourseAccessKeyRepository courseKeyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccessKeyService accessKeyService;

    @Mock
    private KeyGeneratorService keyGeneratorService;

    @Mock
    private ETagHelper eTagHelper;

    @InjectMocks
    private TeamServiceImpl teamService;

    /* Test data */
    private Course teamBasedCourse;
    private Course soloCourse;
    private Team team1;
    private Team team2;
    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    public void setUp() throws Exception {
        Field id = AbstractEntity.class.getDeclaredField("id");
        id.setAccessible(true);

        teamBasedCourse = Course.builder()
                .name("Team Course")
                .courseType(CourseType.TEAM_BASED)
                .build();
        id.set(teamBasedCourse, UUID.randomUUID());

        soloCourse = Course.builder()
                .name("Solo Course")
                .courseType(CourseType.SOLO)
                .build();
        id.set(soloCourse, UUID.randomUUID());

        user1 = new User(UUID.randomUUID(), UUID.randomUUID(), "user1@test.com", "user1", "First1", "Last1");
        user2 = new User(UUID.randomUUID(), UUID.randomUUID(), "user2@test.com", "user2", "First2", "Last2");
        user3 = new User(UUID.randomUUID(), UUID.randomUUID(), "user3@test.com", "user3", "First3", "Last3");

        team1 = Team.builder()
                .name("Team1")
                .course(teamBasedCourse)
                .maxSize(5)
                .active(true)
                .users(new ArrayList<>(List.of(user1)))
                .build();

        team2 = Team.builder()
                .name("Team2")
                .course(teamBasedCourse)
                .maxSize(5)
                .active(true)
                .users(new ArrayList<>())
                .build();
    }

    @Test
    void Given_TeamExists_When_GetById_Then_ReturnTeam() {
        UUID teamId = UUID.randomUUID();
        when(teamRepository.findByIdWithUsers(teamId)).thenReturn(Optional.of(team1));

        Team result = teamService.getTeamById(teamId);

        assertNotNull(result);
        assertEquals(team1.getName(), result.getName());
        assertEquals(team1.getUsers(), result.getUsers());
    }

    @Test
    void Given_TeamDoesNotExist_When_GetById_Then_ThrowException() {
        UUID teamId = UUID.randomUUID();
        when(teamRepository.findByIdWithUsers(teamId)).thenReturn(Optional.empty());

        assertThrows(TeamNotFoundException.class, () -> teamService.getTeamById(teamId));
    }

    /* Create Operations Tests */

    @Test
    void Given_ValidTeamData_When_CreateTeam_Then_Success() {
        Team newTeam = Team.builder()
                .name("NewTeam")
                .maxSize(5)
                .active(true)
                .users(new ArrayList<>())
                .build();

        UUID teamId = UUID.randomUUID();
        setEntityId(newTeam, teamId);

        when(teamRepository.existsByNameAndCourseId(anyString(), any(UUID.class))).thenReturn(false);
        when(teamRepository.saveAndFlush(any(Team.class))).thenAnswer(invocation -> {
            Team savedTeam = invocation.getArgument(0);
            setEntityId(savedTeam, teamId);
            return savedTeam;
        });
        when(keyGeneratorService.generateUniqueTeamKey(any())).thenReturn("NEW-KEY");

        Team result = teamService.createTeam(newTeam, teamBasedCourse, null);

        assertNotNull(result);
        assertEquals("NewTeam", result.getName());
        verify(teamRepository).saveAndFlush(any(Team.class));
        verify(accessKeyService).createTeamKey(eq(teamId), anyString());
    }

    @Test
    void Given_DuplicateTeamName_When_CreateTeam_Then_ThrowException() {
        Team newTeam = Team.builder()
                .name("Team1")
                .maxSize(5)
                .active(true)
                .users(new ArrayList<>())
                .build();

        when(teamRepository.existsByNameAndCourseId(anyString(), any(UUID.class))).thenReturn(true);

        assertThrows(TeamAlreadyExistsException.class,
                () -> teamService.createTeam(newTeam, teamBasedCourse, null));
    }

    /* Update Operations Tests */

    @Test
    void Given_ValidTeamUpdate_When_UpdateTeam_Then_Success() {
        UUID teamId = UUID.randomUUID();
        Team existingTeam = team1;
        Team updatedTeam = Team.builder()
                .name("UpdatedName")
                .maxSize(10)
                .build();

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(existingTeam));
        when(eTagHelper.validateEtag(anyString(), any())).thenReturn(true);
        when(teamRepository.saveAndFlush(any(Team.class))).thenReturn(existingTeam);

        Team result = teamService.updateTeam(updatedTeam, teamId, "valid-etag");

        assertEquals("UpdatedName", result.getName());
        assertEquals(10, result.getMaxSize());
    }

    /* Join/Leave Operations Tests */

    @Test
    void Given_ValidTeamKey_When_JoinTeam_Then_Success() {
        String keyValue = "VALID-KEY";
        TeamAccessKey teamKey = createTeamKeyWithValue(team2, keyValue);

        when(teamKeyRepository.findByKeyValue(keyValue)).thenReturn(Optional.of(teamKey));
        when(teamRepository.saveAndFlush(any(Team.class))).thenReturn(team2);
        when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));

        assertDoesNotThrow(() -> teamService.joinUsingKey(keyValue, user2));
        assertTrue(team2.getUsers().contains(user2));

        verify(teamRepository).saveAndFlush(team2);
        verify(userRepository).findById(user2.getId());
    }

    @Test
    void Given_InactiveTeam_When_JoinTeam_Then_ThrowException() {
        String keyValue = "VALID-KEY";
        team2.setActive(false);
        TeamAccessKey teamKey = createTeamKeyWithValue(team2, keyValue);

        when(teamKeyRepository.findByKeyValue(keyValue)).thenReturn(Optional.of(teamKey));

        assertThrows(TeamNotActiveException.class,
                () -> teamService.joinUsingKey(keyValue, user2));
    }

    /* Solo Course Operations Tests */

    @Test
    void Given_ValidSoloCourse_When_CreateSoloTeam_Then_Success() {
        when(teamRepository.findTeamNumbersByCourseIdAndPrefix(any(), anyString()))
                .thenReturn(new ArrayList<>());
        when(teamRepository.saveAndFlush(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));

        teamService.createSoloTeam(soloCourse, user1);

        verify(teamRepository).saveAndFlush(argThat(team ->
                team.getName().startsWith("SC-Student") &&
                        team.getMaxSize() == 1 &&
                        team.getUsers().contains(user1)
        ));
    }

    @Test
    void Given_SoloCourse_When_GetStudents_Then_ReturnList() {
        Team soloTeam1 = Team.builder()
                .name("SC-Student1")
                .course(soloCourse)
                .users(new ArrayList<>(List.of(user1)))
                .build();
        Team soloTeam2 = Team.builder()
                .name("SC-Student2")
                .course(soloCourse)
                .users(new ArrayList<>(List.of(user2)))
                .build();

        when(teamRepository.findByCourseId(soloCourse.getId()))
                .thenReturn(List.of(soloTeam1, soloTeam2));

        List<User> result = teamService.getStudentsInSoloCourse(soloCourse);

        assertEquals(2, result.size());
        assertTrue(result.contains(user1));
        assertTrue(result.contains(user2));
    }

    @Test
    void Given_TeamBasedCourse_When_GetStudents_Then_ThrowException() {
        assertThrows(IncorrectCourseTypeException.class,
                () -> teamService.getStudentsInSoloCourse(teamBasedCourse));
    }

    /* Batch Operations Tests */

    @Test
    void Given_ValidData_When_CreateTeamsBatch_Then_Success() {
        String prefix = "BATCH";
        int teamSize = 3;
        int numberOfTeams = 2;

        when(teamRepository.findTeamNumbersByPrefix(any(), anyString()))
                .thenReturn(new ArrayList<>());
        when(teamRepository.saveAndFlush(any(Team.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(keyGeneratorService.generateUniqueTeamKey(any()))
                .thenReturn("KEY-1", "KEY-2");

        List<Team> result = teamService.createTeamsBatch(teamBasedCourse, prefix, teamSize, numberOfTeams);

        assertEquals(2, result.size());
        assertEquals("BATCH-1", result.get(0).getName());
        assertEquals("BATCH-2", result.get(1).getName());
        verify(accessKeyService, times(2)).createTeamKey(any(), anyString());
    }

    /* Team Management Edge Cases */

    @Test
    void Given_TeamWithPods_When_DeleteTeam_Then_ThrowException() {
        Team teamWithPods = Team.builder()
                .name("TeamWithPods")
                .course(teamBasedCourse)
                .statefulPods(new ArrayList<>())
                .build();
        setEntityId(teamWithPods, UUID.randomUUID());

        PodStateful pod = createPodStateful();
        teamWithPods.getStatefulPods().add(pod);

        when(teamRepository.findById(teamWithPods.getId()))
                .thenReturn(Optional.of(teamWithPods));

        assertThrows(TeamDeletionException.class,
                () -> teamService.deleteTeam(teamWithPods));
    }

    @Test
    void Given_CourseKey_When_JoinTeam_Then_CreateSoloTeam() {
        String keyValue = "COURSE-KEY";
        CourseAccessKey courseKey = new CourseAccessKey();
        courseKey.setCourse(soloCourse);

        when(teamKeyRepository.findByKeyValue(keyValue))
                .thenReturn(Optional.empty());
        when(courseKeyRepository.findByKeyValue(keyValue))
                .thenReturn(Optional.of(courseKey));
        when(teamRepository.existsByUserIdAndCourseId(any(), any()))
                .thenReturn(false);

        assertDoesNotThrow(() -> teamService.joinUsingKey(keyValue, user1));
        verify(teamRepository).saveAndFlush(any());
    }

    @Test
    void Given_InsufficientSize_When_UpdateTeam_Then_ThrowException() {
        UUID teamId = UUID.randomUUID();
        Team existingTeam = team1;
        Team updatedTeam = Team.builder()
                .name("UpdatedName")
                .maxSize(0)
                .build();

        when(teamRepository.findById(teamId))
                .thenReturn(Optional.of(existingTeam));
        when(eTagHelper.validateEtag(anyString(), any()))
                .thenReturn(true);

        assertThrows(TeamSizeException.class,
                () -> teamService.updateTeam(updatedTeam, teamId, "valid-etag"));
    }

    @Test
    void Given_EtagMismatch_When_UpdateTeam_Then_ThrowException() {
        UUID teamId = UUID.randomUUID();
        when(teamRepository.findById(teamId))
                .thenReturn(Optional.of(team1));
        when(eTagHelper.validateEtag(anyString(), any()))
                .thenReturn(false);

        assertThrows(TeamConflictException.class,
                () -> teamService.updateTeam(team2, teamId, "invalid-etag"));
    }

    @Test
    void Given_ValidRequest_When_LeaveTeam_Then_Success() {
        UUID teamId = UUID.randomUUID();
        Team team = team1;
        setEntityId(team, teamId);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));

        teamService.leaveTeam(teamId, user1.getId());

        assertFalse(team.getUsers().contains(user1));
        verify(teamRepository).saveAndFlush(team);
    }

    /* Student Management Tests */

    @Test
    void Given_FullTeam_When_AddStudent_Then_ThrowException() {
        team2.setMaxSize(1);
        team2.getUsers().add(user1);

        when(userRepository.findByEmailIgnoreCase(user2.getEmail()))
                .thenReturn(Optional.of(user2));

        assertThrows(TeamSizeException.class,
                () -> teamService.addStudentToTeam(team2, user2.getEmail()));
    }

    @Test
    void Given_ExistingStudent_When_AddToTeam_Then_ThrowException() {
        team1.getUsers().add(user2);

        when(userRepository.findByEmailIgnoreCase(user2.getEmail()))
                .thenReturn(Optional.of(user2));
        when(userRepository.findById(user2.getId()))
                .thenReturn(Optional.of(user2));

        assertThrows(TeamUserAlreadyMemberException.class,
                () -> teamService.addStudentToTeam(team1, user2.getEmail()));
    }

    @Test
    void Given_ValidRequest_When_RemoveStudentFromTeam_Then_Success() {
        team1.getUsers().add(user2);

        when(userRepository.findByEmailIgnoreCase(user2.getEmail()))
                .thenReturn(Optional.of(user2));

        teamService.removeStudentFromTeam(team1, user2.getEmail());

        assertFalse(team1.getUsers().contains(user2));
        verify(teamRepository).saveAndFlush(team1);
    }

    @Test
    void Given_TeamBasedCourse_When_RemoveStudent_Then_ThrowException() {
        when(userRepository.findByEmailIgnoreCase(user1.getEmail()))
                .thenReturn(Optional.of(user1));

        assertThrows(IncorrectCourseTypeException.class,
                () -> teamService.removeStudentFromCourse(teamBasedCourse, user1.getEmail()));
    }

    /* Course Type Validation Tests */

    @Test
    void Given_TeamBasedCourse_When_AddStudentDirectly_Then_ThrowException() {
        when(userRepository.findByEmailIgnoreCase(user1.getEmail()))
                .thenReturn(Optional.of(user1));

        assertThrows(IncorrectCourseTypeException.class,
                () -> teamService.addStudentToCourse(teamBasedCourse, user1.getEmail()));
    }

    @Test
    void Given_SoloCourse_When_CreateBatchTeams_Then_ThrowException() {
        assertThrows(IncorrectCourseTypeException.class,
                () -> teamService.createTeamsBatch(soloCourse, "PREFIX", 5, 2));
    }

    @Test
    void Given_SoloTeam_When_Update_Then_ThrowException() {
        UUID teamId = UUID.randomUUID();
        Team soloTeam = Team.builder()
                .name("SoloTeam")
                .course(soloCourse)
                .build();

        when(teamRepository.findById(teamId))
                .thenReturn(Optional.of(soloTeam));
        when(eTagHelper.validateEtag(anyString(), any()))
                .thenReturn(true);

        assertThrows(IncorrectCourseTypeException.class,
                () -> teamService.updateTeam(team2, teamId, "valid-etag"));
    }

    /* Search & Pagination Tests */

    @Test
    void Given_ValidSearch_When_GetTeamsByStudent_Then_ReturnFilteredList() {
        UUID userId = UUID.randomUUID();
        String search = "Team";
        when(teamRepository.findByUsersIdAndNameContainingIgnoreCase(
                eq(userId), eq(search), any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<Team> result = teamService.getTeamsByStudent(userId, 0, 10, search, "ASC");

        verify(teamRepository).findByUsersIdAndNameContainingIgnoreCase(
                eq(userId), eq(search), any(Pageable.class));
    }

    @Test
    void Given_ValidSearch_When_GetTeamsByCourse_Then_ReturnFilteredList() {
        UUID courseId = UUID.randomUUID();
        String search = "Team";
        when(teamRepository.findByCourseIdWithSearch(
                eq(courseId), eq(search), eq("name"), any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<Team> result = teamService.getTeamsByCourse(courseId, 0, 10, search, "name", "ASC");

        verify(teamRepository).findByCourseIdWithSearch(
                eq(courseId), eq(search), eq("name"), any(Pageable.class));
    }

    @Test
    void Given_EmailPrefixes_When_FindTeams_Then_ReturnMatchingTeams() {
        UUID courseId = UUID.randomUUID();
        List<String> emailPrefixes = List.of("user1", "user2");
        when(teamRepository.findByCourseIdAndEmailPrefixes(
                eq(courseId), eq(emailPrefixes), any(Sort.class)))
                .thenReturn(List.of(team1, team2));

        List<Team> result = teamService.findTeamsByEmails(courseId, emailPrefixes, "DESC");

        assertEquals(2, result.size());
        verify(teamRepository).findByCourseIdAndEmailPrefixes(
                eq(courseId), eq(emailPrefixes), any(Sort.class));
    }

    @Test
    void Given_SoloCourseTeam_When_DeleteTeam_Then_ThrowException() {
        Team soloTeam = Team.builder()
                .name("SoloTeam")
                .course(soloCourse)
                .build();
        setEntityId(soloTeam, UUID.randomUUID());

        assertThrows(IncorrectCourseTypeException.class,
                () -> teamService.deleteTeam(soloTeam));
        verify(teamRepository, never()).findById(any());
    }

    @Test
    void Given_NonExistentTeam_When_DeleteTeam_Then_ThrowException() {
        Team team = Team.builder()
                .name("Team")
                .course(teamBasedCourse)
                .build();
        UUID teamId = UUID.randomUUID();
        setEntityId(team, teamId);

        when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

        assertThrows(TeamNotFoundException.class,
                () -> teamService.deleteTeam(team));
    }

    @Test
    void Given_ValidTeam_When_DeleteTeam_Then_Success() {
        Team team = Team.builder()
                .name("Team")
                .course(teamBasedCourse)
                .users(new ArrayList<>(List.of(user1, user2)))
                .statefulPods(new ArrayList<>())
                .statelessPods(new ArrayList<>())
                .build();
        UUID teamId = UUID.randomUUID();
        setEntityId(team, teamId);

        TeamAccessKey teamKey = createTeamKeyWithValue(team, "KEY");

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(teamKeyRepository.findByTeamId(teamId)).thenReturn(Optional.of(teamKey));

        teamService.deleteTeam(team);

        assertTrue(team.getUsers().isEmpty());
        verify(teamRepository).saveAndFlush(team);
        verify(teamKeyRepository).delete(teamKey);
        verify(teamKeyRepository).flush();
        verify(teamRepository).delete(team);
        verify(teamRepository).flush();
    }

    /* Pagination Tests */

    @Test
    void Given_ValidRequest_When_GetAllTeams_Then_ReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(teamRepository.findAllWithUsers(pageable))
                .thenReturn(Page.empty());

        Page<Team> result = teamService.getAllTeams(pageable);

        verify(teamRepository).findAllWithUsers(pageable);
    }

    @Test
    void Given_EmptySearch_When_GetTeamsByStudent_Then_ReturnPage() {
        UUID userId = UUID.randomUUID();
        when(teamRepository.findByUsersId(eq(userId), any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<Team> result = teamService.getTeamsByStudent(userId, 0, 10, "", "DESC");

        verify(teamRepository).findByUsersId(eq(userId), any(Pageable.class));
    }

    @Test
    void Given_EmptySearch_When_GetTeamsByCourse_Then_ReturnPage() {
        UUID courseId = UUID.randomUUID();
        when(teamRepository.findByCourseId(eq(courseId), any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<Team> result = teamService.getTeamsByCourse(courseId, 0, 10, "", "name", "DESC");

        verify(teamRepository).findByCourseId(eq(courseId), any(Pageable.class));
    }

    /* Error Cases */

    @Test
    void Given_TeamNotFound_When_GetTeamByCourseAndUser_Then_ThrowException() {
        UUID userId = UUID.randomUUID();
        when(teamRepository.findByUserIdAndCourse(userId, teamBasedCourse))
                .thenReturn(Optional.empty());

        assertThrows(UserDoesntBelongToCourseException.class,
                () -> teamService.getTeamByCourseAndUser(teamBasedCourse, userId));
    }

    @Test
    void Given_DuplicateKey_When_CreateTeam_Then_ThrowException() {
        String existingKey = "EXISTING-KEY";
        when(teamKeyRepository.existsByKeyValue(existingKey))
                .thenReturn(true);

        assertThrows(DuplicateKeyValueException.class,
                () -> teamService.createTeam(team1, teamBasedCourse, existingKey));
    }

    @Test
    void Given_DuplicateName_When_UpdateTeam_Then_ThrowException() {
        UUID teamId = UUID.randomUUID();
        Team existingTeam = team1;
        Team updatedTeam = Team.builder()
                .name("Team2")
                .maxSize(5)
                .build();

        when(teamRepository.findById(teamId))
                .thenReturn(Optional.of(existingTeam));
        when(eTagHelper.validateEtag(anyString(), any()))
                .thenReturn(true);
        when(teamRepository.existsByNameAndCourseId(anyString(), any(UUID.class)))
                .thenReturn(true);

        assertThrows(TeamAlreadyExistsException.class,
                () -> teamService.updateTeam(updatedTeam, teamId, "valid-etag"));
    }

    @Test
    void Given_UserNotMember_When_RemoveFromTeam_Then_ThrowException() {
        String email = "nonmember@test.com";
        when(userRepository.findByEmailIgnoreCase(email))
                .thenReturn(Optional.of(user3));

        assertThrows(TeamUserNotMemberException.class,
                () -> teamService.removeStudentFromTeam(team1, email));
    }

    /* Student Management Tests */

    @Test
    void Given_ValidData_When_AddStudentToTeam_Then_Success() {
        when(userRepository.findByEmailIgnoreCase(user2.getEmail()))
                .thenReturn(Optional.of(user2));
        when(userRepository.findById(user2.getId()))
                .thenReturn(Optional.of(user2));

        teamService.addStudentToTeam(team1, user2.getEmail());

        assertTrue(team1.getUsers().contains(user2));
        verify(teamRepository).saveAndFlush(team1);
    }

    @Test
    void Given_InactiveTeam_When_AddStudent_Then_ThrowException() {
        team1.setActive(false);
        when(userRepository.findByEmailIgnoreCase(user2.getEmail()))
                .thenReturn(Optional.of(user2));

        assertThrows(TeamNotActiveException.class,
                () -> teamService.addStudentToTeam(team1, user2.getEmail()));
    }

    @Test
    void Given_UserNotFound_When_AddStudent_Then_ThrowException() {
        String email = "notfound@test.com";
        when(userRepository.findByEmailIgnoreCase(email))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> teamService.addStudentToTeam(team1, email));
    }

    /* Course Student Management Tests */

    @Test
    void Given_SoloCourse_When_AddStudent_Then_Success() {
        when(userRepository.findByEmailIgnoreCase(user2.getEmail()))
                .thenReturn(Optional.of(user2));
        when(teamRepository.existsByUserIdAndCourseId(user2.getId(), soloCourse.getId()))
                .thenReturn(false);

        teamService.addStudentToCourse(soloCourse, user2.getEmail());

        verify(teamRepository).saveAndFlush(any());
    }

    @Test
    void Given_TeamBasedCourse_When_AddStudent_Then_ThrowException() {
        when(userRepository.findByEmailIgnoreCase(user2.getEmail()))
                .thenReturn(Optional.of(user2));

        assertThrows(IncorrectCourseTypeException.class,
                () -> teamService.addStudentToCourse(teamBasedCourse, user2.getEmail()));
    }

    @Test
    void Given_UserAlreadyInCourse_When_AddStudent_Then_ThrowException() {
        when(userRepository.findByEmailIgnoreCase(user2.getEmail()))
                .thenReturn(Optional.of(user2));
        when(teamRepository.existsByUserIdAndCourseId(user2.getId(), soloCourse.getId()))
                .thenReturn(true);

        assertThrows(UserAlreadyInCourseException.class,
                () -> teamService.addStudentToCourse(soloCourse, user2.getEmail()));
    }

    @Test
    void Given_ValidSoloCourse_When_RemoveStudent_Then_Success() {
        Team soloTeam = Team.builder()
                .course(soloCourse)
                .users(new ArrayList<>(List.of(user1)))
                .build();

        when(userRepository.findByEmailIgnoreCase(user1.getEmail()))
                .thenReturn(Optional.of(user1));
        when(teamRepository.findByUserIdAndCourse(user1.getId(), soloCourse))
                .thenReturn(Optional.of(soloTeam));

        teamService.removeStudentFromCourse(soloCourse, user1.getEmail());

        verify(teamRepository).delete(soloTeam);
    }

    @Test
    void Given_TeamNotFound_When_RemoveStudent_Then_ThrowException() {
        when(userRepository.findByEmailIgnoreCase(user1.getEmail()))
                .thenReturn(Optional.of(user1));
        when(teamRepository.findByUserIdAndCourse(user1.getId(), soloCourse))
                .thenReturn(Optional.empty());

        assertThrows(TeamNotFoundException.class,
                () -> teamService.removeStudentFromCourse(soloCourse, user1.getEmail()));
    }

    @Test
    void Given_UserNotInTeam_When_LeaveTeam_Then_ThrowException() {
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Team team = team1;
        User nonMember = user3;
        setEntityId(team, teamId);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(userRepository.findById(userId)).thenReturn(Optional.of(nonMember));

        assertThrows(TeamUserNotMemberException.class,
                () -> teamService.leaveTeam(teamId, userId));
    }

    /* Helper Methods */

    @SneakyThrows
    private Team getTeamWithRandomId() {
        Team team = Team.builder().build();
        Field idField = AbstractEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(team, UUID.randomUUID());
        idField.setAccessible(false);
        return team;
    }

    @SneakyThrows
    private TeamAccessKey createTeamKeyWithValue(Team team, String keyValue) {
        TeamAccessKey teamKey = TeamAccessKey.builder()
                .team(team)
                .build();

        Field keyValueField = AccessKey.class.getDeclaredField("keyValue");
        keyValueField.setAccessible(true);
        keyValueField.set(teamKey, keyValue);
        keyValueField.setAccessible(false);

        return teamKey;
    }

    @SneakyThrows
    private PodStateful createPodStateful() {
        PodStateful pod = new PodStateful();
        setEntityId(pod, UUID.randomUUID());
        return pod;
    }

    @SneakyThrows
    private void setEntityId(AbstractEntity entity, UUID id) {
        Field idField = AbstractEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
        idField.setAccessible(false);
    }
}