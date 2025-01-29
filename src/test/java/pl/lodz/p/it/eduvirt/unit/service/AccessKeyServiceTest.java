package pl.lodz.p.it.eduvirt.unit.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.lodz.p.it.eduvirt.exceptions.access_key.AccessKeyAlreadyExistsException;
import pl.lodz.p.it.eduvirt.exceptions.access_key.AccessKeyLengthException;
import pl.lodz.p.it.eduvirt.exceptions.access_key.DuplicateKeyValueException;
import pl.lodz.p.it.eduvirt.exceptions.course.InvalidCourseTypeException;

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
import pl.lodz.p.it.eduvirt.repository.TeamRepository;
import pl.lodz.p.it.eduvirt.repository.key.CourseAccessKeyRepository;
import pl.lodz.p.it.eduvirt.repository.key.TeamAccessKeyRepository;
import pl.lodz.p.it.eduvirt.service.impl.AccessKeyServiceImpl;


@ExtendWith(MockitoExtension.class)
public class AccessKeyServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamAccessKeyRepository teamAccessKeyRepository;

    @Mock
    private CourseAccessKeyRepository courseAccessKeyRepository;

    @InjectMocks
    private AccessKeyServiceImpl accessKeyService;

    /* Test data */
    private Course teamBasedCourse;
    private Course soloCourse;
    private Team team1;
    private User user1;

    @BeforeEach
    public void setUp() throws Exception {
        teamBasedCourse = Course.builder()
                .name("Team Course")
                .courseType(CourseType.TEAM_BASED)
                .build();
        setEntityId(teamBasedCourse, UUID.randomUUID());

        soloCourse = Course.builder()
                .name("Solo Course")
                .courseType(CourseType.SOLO)
                .build();
        setEntityId(soloCourse, UUID.randomUUID());

        user1 = new User(UUID.randomUUID(), UUID.randomUUID(), "user1@test.com", "user1", "First1", "Last1");

        team1 = Team.builder()
                .name("Team1")
                .course(teamBasedCourse)
                .maxSize(5)
                .active(true)
                .users(new ArrayList<>(List.of(user1)))
                .build();
        setEntityId(team1, UUID.randomUUID());
    }

    /* Course Key Tests */

    @Test
    void Given_ValidSoloCourse_When_CreateKey_Then_Success() {
        when(courseAccessKeyRepository.existsByCourseId(soloCourse.getId()))
                .thenReturn(false);
        when(courseAccessKeyRepository.saveAndFlush(any(CourseAccessKey.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CourseAccessKey result = accessKeyService.createCourseKey(soloCourse, null);

        assertNotNull(result);
        assertEquals(soloCourse, result.getCourse());
        assertTrue(result.getKeyValue().matches("^[a-zA-Z0-9-_]{4,20}$"));
    }

    @Test
    void Given_TeamBasedCourse_When_CreateKey_Then_ThrowException() {
        assertThrows(InvalidCourseTypeException.class,
                () -> accessKeyService.createCourseKey(teamBasedCourse, null));
    }

    @Test
    void Given_ExistingKey_When_CreateCourseKey_Then_ThrowException() {
        when(courseAccessKeyRepository.existsByCourseId(soloCourse.getId()))
                .thenReturn(true);

        assertThrows(AccessKeyAlreadyExistsException.class,
                () -> accessKeyService.createCourseKey(soloCourse, null));
    }

    @Test
    void Given_ValidRequest_When_GetCourseKey_Then_Success() {
        CourseAccessKey key = new CourseAccessKey();
        key.setCourse(soloCourse);
        setKeyValue(key, "TEST-KEY");

        when(courseAccessKeyRepository.findByCourseId(soloCourse.getId()))
                .thenReturn(Optional.of(key));

        CourseAccessKey result = accessKeyService.getKeyForCourse(soloCourse);

        assertNotNull(result);
        assertEquals("TEST-KEY", result.getKeyValue());
    }

    @Test
    void Given_TeamBasedCourse_When_GetKey_Then_ThrowException() {
        assertThrows(InvalidCourseTypeException.class,
                () -> accessKeyService.getKeyForCourse(teamBasedCourse));
    }

    /* Team Key Tests */

    @Test
    void Given_ValidTeam_When_CreateKey_Then_Success() {
        when(teamRepository.findById(team1.getId()))
                .thenReturn(Optional.of(team1));
        when(teamAccessKeyRepository.existsByTeamId(team1.getId()))
                .thenReturn(false);

        assertDoesNotThrow(() ->
                accessKeyService.createTeamKey(team1.getId(), null));

        verify(teamAccessKeyRepository).saveAndFlush(any(TeamAccessKey.class));
    }

    @Test
    void Given_CustomKey_When_CreateTeamKey_Then_UseProvidedKey() {
        String customKey = "CUSTOM-KEY";
        when(teamRepository.findById(team1.getId()))
                .thenReturn(Optional.of(team1));
        when(teamAccessKeyRepository.existsByTeamId(team1.getId()))
                .thenReturn(false);
        when(teamAccessKeyRepository.existsByKeyValue(customKey))
                .thenReturn(false);

        accessKeyService.createTeamKey(team1.getId(), customKey);

        verify(teamAccessKeyRepository).saveAndFlush(argThat(key ->
                key.getKeyValue().equals(customKey)));
    }

    @Test
    void Given_ExistingTeamKey_When_CreateKey_Then_ThrowException() {
        when(teamRepository.findById(team1.getId()))
                .thenReturn(Optional.of(team1));
        when(teamAccessKeyRepository.existsByTeamId(team1.getId()))
                .thenReturn(true);

        assertThrows(AccessKeyAlreadyExistsException.class,
                () -> accessKeyService.createTeamKey(team1.getId(), null));
    }

    @Test
    void Given_ValidTeam_When_GetKey_Then_Success() {
        TeamAccessKey key = new TeamAccessKey();
        key.setTeam(team1);
        setKeyValue(key, "TEAM-KEY");

        when(teamAccessKeyRepository.findByTeamId(team1.getId()))
                .thenReturn(Optional.of(key));

        TeamAccessKey result = accessKeyService.getKeyForTeam(team1, teamBasedCourse);

        assertNotNull(result);
        assertEquals("TEAM-KEY", result.getKeyValue());
    }

    @Test
    void Given_SoloCourse_When_GetTeamKey_Then_ThrowException() {
        assertThrows(InvalidCourseTypeException.class,
                () -> accessKeyService.getKeyForTeam(team1, soloCourse));
    }

    /* Validation Tests */

    @Test
    void Given_InvalidKeyFormat_When_CreateKey_Then_ThrowException() {
        String invalidKey = "inv@lid";
        when(teamRepository.findById(team1.getId()))
                .thenReturn(Optional.of(team1));
        when(teamAccessKeyRepository.existsByTeamId(team1.getId()))
                .thenReturn(false);

        assertThrows(AccessKeyLengthException.class,
                () -> accessKeyService.createTeamKey(team1.getId(), invalidKey));
    }

    @Test
    void Given_DuplicateKeyValue_When_CreateKey_Then_ThrowException() {
        String existingKey = "EXISTING";
        when(teamRepository.findById(team1.getId()))
                .thenReturn(Optional.of(team1));
        when(teamAccessKeyRepository.existsByKeyValue(existingKey))
                .thenReturn(true);

        assertThrows(DuplicateKeyValueException.class,
                () -> accessKeyService.createTeamKey(team1.getId(), existingKey));
    }

    /* Helper Methods */

    @SneakyThrows
    private void setEntityId(AbstractEntity entity, UUID id) {
        Field idField = AbstractEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
        idField.setAccessible(false);
    }

    @SneakyThrows
    private void setKeyValue(AccessKey key, String value) {
        Field keyValueField = AccessKey.class.getDeclaredField("keyValue");
        keyValueField.setAccessible(true);
        keyValueField.set(key, value);
        keyValueField.setAccessible(false);
    }
}