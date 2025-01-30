package pl.lodz.p.it.eduvirt.unit.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;
import org.springframework.test.web.servlet.MvcResult;
import pl.lodz.p.it.eduvirt.aspect.exception.GeneralControllerExceptionResolver;
import pl.lodz.p.it.eduvirt.controller.AccessKeyController;
import pl.lodz.p.it.eduvirt.dto.access_key.CourseAccessKeyDto;
import pl.lodz.p.it.eduvirt.dto.access_key.CreateCourseKeyDto;
import pl.lodz.p.it.eduvirt.entity.AbstractEntity;
import pl.lodz.p.it.eduvirt.entity.Course;
import pl.lodz.p.it.eduvirt.entity.Team;
import pl.lodz.p.it.eduvirt.entity.User;
import pl.lodz.p.it.eduvirt.entity.key.CourseAccessKey;
import pl.lodz.p.it.eduvirt.entity.key.CourseType;
import pl.lodz.p.it.eduvirt.entity.key.TeamAccessKey;
import pl.lodz.p.it.eduvirt.mappers.AccessKeyMapper;
import pl.lodz.p.it.eduvirt.mappers.AccessKeyMapperImpl;
import pl.lodz.p.it.eduvirt.repository.UserRepository;
import pl.lodz.p.it.eduvirt.service.AccessKeyService;
import pl.lodz.p.it.eduvirt.service.CourseService;
import pl.lodz.p.it.eduvirt.service.TeamService;

@Import({
        AccessKeyController.class,
        GeneralControllerExceptionResolver.class,
        AccessKeyMapperImpl.class,
        ObjectMapper.class,
})
@WebMvcTest(controllers = {AccessKeyController.class}, useDefaultFilters = false)
public class AccessKeyControllerTest {

    private static final String STUDENT_ID = "2f3be36e-8119-4bd0-9114-9ae1885dbf1a";
    private static final String TEACHER_ID = "6d8d6d4e-7625-4aba-8a17-46c237f126be";
    private static final String ADMIN_ID = "067a369d-3911-4c1e-8219-65ef3b5af622";
    private static final String COURSE_ID = "77ec0abd-98b9-499c-bf77-dfb784c121c2";
    private static final String TEAM_ID = "93af6a78-b65e-4e2e-9651-db3c09c914f7";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccessKeyService accessKeyService;

    @MockitoBean
    private CourseService courseService;

    @MockitoBean
    private TeamService teamService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoSpyBean
    private AccessKeyMapper accessKeyMapper;

    @MockitoSpyBean
    private final ObjectMapper mapper = new ObjectMapper();

    private Course course;
    private Team team;
    private User student;
    private User teacher;
    private User admin;
    private CourseAccessKey courseKey;
    private TeamAccessKey teamKey;

    @BeforeEach
    void setUp() {
        mapper.findAndRegisterModules();
        setupTestData();
    }

    private void setupTestData() {
        course = Course.builder()
                .name("Test Course")
                .courseType(CourseType.TEAM_BASED)
                .teachers(new ArrayList<>())
                .teams(new ArrayList<>())
                .build();
        setEntityId(course, UUID.fromString(COURSE_ID));

        team = Team.builder()
                .name("Test Team")
                .course(course)
                .maxSize(5)
                .active(true)
                .users(new ArrayList<>())
                .statelessPods(new ArrayList<>())
                .build();
        setEntityId(team, UUID.fromString(TEAM_ID));

        student = new User(UUID.fromString(STUDENT_ID), UUID.randomUUID(),
                "student@test.com", "student", "Student", "Test");
        teacher = new User(UUID.fromString(TEACHER_ID), UUID.randomUUID(),
                "teacher@test.com", "teacher", "Teacher", "Test");
        admin = new User(UUID.fromString(ADMIN_ID), UUID.randomUUID(),
                "admin@test.com", "admin", "Admin", "Test");

        courseKey = new CourseAccessKey();
        courseKey.setKeyValue("course-key-123");
        courseKey.setCourse(course);
        setEntityId(courseKey, UUID.randomUUID());

        teamKey = new TeamAccessKey();
        teamKey.setKeyValue("team-key-123");
        teamKey.setTeam(team);
        setEntityId(teamKey, UUID.randomUUID());

        course.getTeachers().add(teacher);
        course.getTeams().add(team);
        team.getUsers().add(student);
    }

    @Test
    @WithMockUser(username = TEACHER_ID, authorities = "teacher")
    void Given_ValidData_When_CreateCourseKey_Then_Success() throws Exception {
        String keyValue = "new-course-key-123";
        CreateCourseKeyDto createDto = new CreateCourseKeyDto(keyValue);

        when(userRepository.findById(UUID.fromString(TEACHER_ID))).thenReturn(Optional.of(teacher));
        when(courseService.getCourse(UUID.fromString(COURSE_ID))).thenReturn(course);
        when(accessKeyService.createCourseKey(course, keyValue)).thenReturn(courseKey);

        mockMvc.perform(post("/access-keys/course/{courseId}", COURSE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());

        verify(accessKeyService).createCourseKey(course, keyValue);
        verify(accessKeyMapper).toCourseKeyDto(courseKey);
    }

    @Test
    @WithMockUser(username = ADMIN_ID, authorities = "administrator")
    void Given_AdminUser_When_CreateCourseKey_Then_Success() throws Exception {
        String keyValue = "new-course-key-123";
        CreateCourseKeyDto createDto = new CreateCourseKeyDto(keyValue);

        when(userRepository.findById(UUID.fromString(ADMIN_ID))).thenReturn(Optional.of(admin));
        when(courseService.getCourse(UUID.fromString(COURSE_ID))).thenReturn(course);
        when(accessKeyService.createCourseKey(course, createDto.getKeyValue())).thenReturn(courseKey);

        mockMvc.perform(post("/access-keys/course/{courseId}", COURSE_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.keyValue").value(courseKey.getKeyValue()));

        verify(accessKeyService).createCourseKey(course, createDto.getKeyValue());
        verify(accessKeyMapper).toCourseKeyDto(courseKey);
    }


    @Test
    @WithMockUser(username = STUDENT_ID, authorities = "student")
    void Given_StudentUser_When_CreateCourseKey_Then_Forbidden() throws Exception {
        when(userRepository.findById(UUID.fromString(STUDENT_ID))).thenReturn(Optional.of(student));
        String keyValue = "new-course-key-123";
        CreateCourseKeyDto createDto = new CreateCourseKeyDto(keyValue);

        mockMvc.perform(post("/access-keys/course/{courseId}", COURSE_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = TEACHER_ID, authorities = "teacher")
    void Given_TeacherNotInCourse_When_CreateCourseKey_Then_Forbidden() throws Exception {
        User otherTeacher = new User(UUID.randomUUID(), UUID.randomUUID(),
                "other@test.com", "other", "Other", "Test");
        String keyValue = "new-course-key-123";
        CreateCourseKeyDto createDto = new CreateCourseKeyDto(keyValue);

        when(userRepository.findById(UUID.fromString(TEACHER_ID))).thenReturn(Optional.of(otherTeacher));
        when(courseService.getCourse(UUID.fromString(COURSE_ID))).thenReturn(course);

        mockMvc.perform(post("/access-keys/course/{courseId}", COURSE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = TEACHER_ID, authorities = "teacher")
    void Given_ValidRequest_When_GetCourseKey_Then_Success() throws Exception {
        when(userRepository.findById(UUID.fromString(TEACHER_ID))).thenReturn(Optional.of(teacher));
        when(courseService.getCourse(UUID.fromString(COURSE_ID))).thenReturn(course);
        when(accessKeyService.getKeyForCourse(course)).thenReturn(courseKey);

        MvcResult result = mockMvc.perform(get("/access-keys/course/{courseId}", COURSE_ID))
                .andDo(print())
                .andExpect(status().isOk()).andReturn();

        String json = result.getResponse().getContentAsString();
        CourseAccessKeyDto responseDto = mapper.readValue(json, CourseAccessKeyDto.class);

        assertNotNull(responseDto);
        assertEquals(courseKey.getKeyValue(), responseDto.getKeyValue());
        assertEquals(courseKey.getCourse().getId(), responseDto.getCourseId());
    }

    @Test
    @WithMockUser(username = STUDENT_ID, authorities = "student")
    void Given_ValidRequest_When_GetTeamKey_Then_Success() throws Exception {
        when(userRepository.findById(UUID.fromString(STUDENT_ID))).thenReturn(Optional.of(student));
        when(teamService.getTeamById(UUID.fromString(TEAM_ID))).thenReturn(team);
        when(courseService.getCourse(UUID.fromString(COURSE_ID))).thenReturn(course);
        when(accessKeyService.getKeyForTeam(team, course)).thenReturn(teamKey);

        mockMvc.perform(get("/access-keys/team/{teamId}", TEAM_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keyValue").value(teamKey.getKeyValue()));
    }

    @Test
    @WithAnonymousUser
    void Given_AnonymousUser_When_AccessEndpoint_Then_Unauthorized() throws Exception {
        mockMvc.perform(get("/access-keys/team/{teamId}", TEAM_ID))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = STUDENT_ID, authorities = "student")
    void Given_StudentNotInTeam_When_GetTeamKey_Then_NoContent() throws Exception {
        User otherStudent = new User(UUID.randomUUID(), UUID.randomUUID(),
                "other@test.com", "other", "Other", "Test");

        when(userRepository.findById(UUID.fromString(STUDENT_ID))).thenReturn(Optional.of(otherStudent));
        when(teamService.getTeamById(UUID.fromString(TEAM_ID))).thenReturn(team);

        mockMvc.perform(get("/access-keys/team/{teamId}", TEAM_ID)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = TEACHER_ID, authorities = "teacher")
    void Given_TeacherNotInCourse_When_GetCourseKey_Then_NoContent() throws Exception {
        User otherTeacher = new User(UUID.fromString(TEACHER_ID), UUID.randomUUID(),
                "other@test.com", "other", "Other", "Test");

        when(userRepository.findById(UUID.fromString(TEACHER_ID))).thenReturn(Optional.of(otherTeacher));
        when(courseService.getCourse(UUID.fromString(COURSE_ID))).thenReturn(course);

        mockMvc.perform(get("/access-keys/course/{courseId}", COURSE_ID))
                .andExpect(status().isNoContent());
    }

    @SneakyThrows
    private void setEntityId(AbstractEntity entity, UUID id) {
        try {
            Field idField = AbstractEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
            idField.setAccessible(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}