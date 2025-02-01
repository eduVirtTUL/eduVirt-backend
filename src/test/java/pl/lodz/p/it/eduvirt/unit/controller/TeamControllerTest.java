package pl.lodz.p.it.eduvirt.unit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.lang.reflect.Field;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;
import pl.lodz.p.it.eduvirt.aspect.exception.GeneralControllerExceptionResolver;
import pl.lodz.p.it.eduvirt.controller.TeamController;
import pl.lodz.p.it.eduvirt.dto.EmailDto;
import pl.lodz.p.it.eduvirt.dto.access_key.JoinTeamKeyDto;
import pl.lodz.p.it.eduvirt.dto.course.CourseBasicDto;
import pl.lodz.p.it.eduvirt.dto.team.*;
import pl.lodz.p.it.eduvirt.entity.*;
import pl.lodz.p.it.eduvirt.entity.key.*;
import pl.lodz.p.it.eduvirt.mappers.TeamMapper;
import pl.lodz.p.it.eduvirt.mappers.TeamMapperImpl;
import pl.lodz.p.it.eduvirt.repository.*;
import pl.lodz.p.it.eduvirt.repository.key.TeamAccessKeyRepository;
import pl.lodz.p.it.eduvirt.service.*;
import pl.lodz.p.it.eduvirt.util.etag.ETagHelper;

@Import({
        TeamController.class,
        GeneralControllerExceptionResolver.class,
        TeamMapperImpl.class,
        ObjectMapper.class
})
@WebMvcTest(controllers = {TeamController.class}, useDefaultFilters = false)
public class TeamControllerTest {

    private static final String STUDENT_ID = "11111111-1111-1111-1111-111111111111";
    private static final String TEACHER_ID = "22222222-2222-2222-2222-222222222222";
    private static final String ADMIN_ID = "33333333-3333-3333-3333-333333333333";
    private static final String COURSE_ID = "44444444-4444-4444-4444-444444444444";
    private static final String TEAM_ID = "55555555-5555-5555-5555-555555555555";
    private static final String KEY_VALUE = "team-key-123";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TeamService teamService;

    @MockitoBean
    private CourseService courseService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private TeamRepository teamRepository;

    @MockitoBean
    private TeamAccessKeyRepository teamAccessKeyRepository;

    @MockitoBean
    private ETagHelper etagHelper;

    @MockitoSpyBean
    private TeamMapper teamMapper;

    @MockitoSpyBean
    private final ObjectMapper mapper = new ObjectMapper();

    private Course course;
    private Team team;
    private User student;
    private User teacher;
    private User admin;
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
                .clusterId(UUID.randomUUID())
                .build();
        setEntityId(course, UUID.fromString(COURSE_ID));

        team = Team.builder()
                .name("Test Team")
                .course(course)
                .maxSize(5)
                .active(true)
                .users(new ArrayList<>())
                .build();
        setEntityId(team, UUID.fromString(TEAM_ID));

        student = new User(UUID.fromString(STUDENT_ID), UUID.randomUUID(),
                "student@test.com", "student", "Student", "Test");
        teacher = new User(UUID.fromString(TEACHER_ID), UUID.randomUUID(),
                "teacher@test.com", "teacher", "Teacher", "Test");
        admin = new User(UUID.fromString(ADMIN_ID), UUID.randomUUID(),
                "admin@test.com", "admin", "Admin", "Test");

        teamKey = new TeamAccessKey();
        teamKey.setKeyValue(KEY_VALUE);
        teamKey.setTeam(team);

        course.getTeachers().add(teacher);
        course.getTeams().add(team);
        team.getUsers().add(student);
    }

    @Test
    @WithMockUser(username = TEACHER_ID, authorities = "teacher")
    void Given_ValidData_When_CreateTeam_Then_Success() throws Exception {
        CreateTeamDto createDto = CreateTeamDto.builder()
                .name("Test Team")
                .keyValue("test-key-123")
                .courseId(UUID.fromString(COURSE_ID))
                .maxSize(5)
                .build();

        TeamWithCourseDto responseDto = TeamWithCourseDto.builder()
                .id(UUID.fromString(TEAM_ID))
                .name("Test Team")
                .maxSize(5)
                .users(List.of())
                .course(new CourseBasicDto(
                        UUID.fromString(COURSE_ID),
                        "Test Course",
                        null,
                        "TEAM_BASED",
                        course.getClusterId().toString()
                ))
                .build();

        when(userRepository.findById(UUID.fromString(TEACHER_ID))).thenReturn(Optional.of(teacher));
        when(courseService.getCourse(UUID.fromString(COURSE_ID))).thenReturn(course);
        when(teamService.createTeam(any(), any(), any())).thenReturn(team);
        when(teamMapper.teamToTeamWithCourseDto(team)).thenReturn(responseDto);

        mockMvc.perform(post("/teams")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(TEAM_ID))
                .andExpect(jsonPath("$.name").value("Test Team"));

        verify(teamService).createTeam(any(), any(), eq("test-key-123"));
    }

    @Test
    @WithMockUser(username = TEACHER_ID, authorities = "teacher")
    void Given_ValidData_When_CreateTeamsBatch_Then_Success() throws Exception {
        CreateTeamBatchDto batchDto = CreateTeamBatchDto.builder()
                .courseId(UUID.fromString(COURSE_ID))
                .prefix("team")
                .teamSize(5)
                .numberOfTeams(3)
                .build();

        List<Team> createdTeams = List.of(team);

        when(userRepository.findById(UUID.fromString(TEACHER_ID))).thenReturn(Optional.of(teacher));
        when(courseService.getCourse(UUID.fromString(COURSE_ID))).thenReturn(course);
        when(teamService.createTeamsBatch(any(), any(), anyInt(), anyInt())).thenReturn(createdTeams);
        when(teamAccessKeyRepository.findByTeamId(any())).thenReturn(Optional.of(teamKey));

        mockMvc.perform(post("/teams/batch")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(batchDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(TEAM_ID))
                .andExpect(jsonPath("$[0].keyValue").value(KEY_VALUE));
    }

    @Test
    @WithMockUser(username = TEACHER_ID, authorities = "teacher")
    void Given_ValidRequest_When_UpdateTeam_Then_Success() throws Exception {
        UpdateTeamDto updateDto = new UpdateTeamDto("Updated Team", 6);
        String etag = "\"valid-etag\"";

        TeamWithCourseDto responseDto = TeamWithCourseDto.builder()
                .id(UUID.fromString(TEAM_ID))
                .name("Updated Team")
                .maxSize(6)
                .users(List.of())
                .course(new CourseBasicDto(
                        UUID.fromString(COURSE_ID),
                        "Test Course",
                        null,
                        "TEAM_BASED",
                        course.getClusterId().toString()
                ))
                .build();

        when(userRepository.findById(UUID.fromString(TEACHER_ID))).thenReturn(Optional.of(teacher));
        when(teamService.getTeamById(UUID.fromString(TEAM_ID))).thenReturn(team);
        when(teamService.updateTeam(any(), any(), any())).thenReturn(team);
        when(teamMapper.teamToTeamWithCourseDto(team)).thenReturn(responseDto);

        mockMvc.perform(put("/teams/{id}", TEAM_ID)
                        .with(csrf())
                        .header(HttpHeaders.IF_MATCH, etag)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Updated Team"))
                .andExpect(jsonPath("$.maxSize").value(6));
    }

    @Test
    @WithMockUser(username = ADMIN_ID, authorities = "administrator")
    void Given_ValidRequest_When_GetTeams_Then_Success() throws Exception {
        Page<Team> teamPage = new PageImpl<>(List.of(team));
        TeamWithCourseDto teamDto = TeamWithCourseDto.builder()
                .id(UUID.fromString(TEAM_ID))
                .name("Test Team")
                .maxSize(5)
                .users(List.of())
                .course(new CourseBasicDto(
                        UUID.fromString(COURSE_ID),
                        "Test Course",
                        null,
                        "TEAM_BASED",
                        course.getClusterId().toString()
                ))
                .build();

        when(teamService.getAllTeams(any(Pageable.class))).thenReturn(teamPage);
        when(teamMapper.teamToTeamWithCourseDto(team)).thenReturn(teamDto);

        mockMvc.perform(get("/teams")
                        .param("pageNumber", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items[0].id").value(TEAM_ID))
                .andExpect(jsonPath("$.items[0].name").value("Test Team"))
                .andExpect(jsonPath("$.page.page").value(0))
                .andExpect(jsonPath("$.page.elements").value(1))
                .andExpect(jsonPath("$.page.totalPages").value(1))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    @WithMockUser(username = STUDENT_ID, authorities = "student")
    void Given_StudentInTeam_When_GetTeamDetails_Then_Success() throws Exception {
        String etag = "\"valid-etag\"";
        TeamWithCourseDto teamDto = TeamWithCourseDto.builder()
                .id(UUID.fromString(TEAM_ID))
                .name("Test Team")
                .maxSize(5)
                .users(List.of())
                .course(new CourseBasicDto(
                        UUID.fromString(COURSE_ID),
                        "Test Course",
                        null,
                        "TEAM_BASED",
                        course.getClusterId().toString()
                ))
                .build();

        when(userRepository.findById(UUID.fromString(STUDENT_ID))).thenReturn(Optional.of(student));
        when(teamService.getTeamById(UUID.fromString(TEAM_ID))).thenReturn(team);
        when(teamMapper.teamToTeamWithCourseDto(team)).thenReturn(teamDto);
        when(etagHelper.generateEtag(team)).thenReturn(etag);

        mockMvc.perform(get("/teams/{teamId}", TEAM_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string(HttpHeaders.ETAG, etag))
                .andExpect(jsonPath("$.id").value(TEAM_ID));
    }

    @Test
    @WithMockUser(username = STUDENT_ID, authorities = "student")
    void Given_StudentUser_When_CreateTeam_Then_Forbidden() throws Exception {
        when(userRepository.findById(UUID.fromString(STUDENT_ID))).thenReturn(Optional.of(student));
        CreateTeamDto createDto = CreateTeamDto.builder()
                .name("Test Team")
                .keyValue("test-key-123")
                .courseId(UUID.fromString(COURSE_ID))
                .maxSize(5)
                .build();

        mockMvc.perform(post("/teams")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = TEACHER_ID, authorities = "teacher")
    void Given_TeacherNotInCourse_When_CreateTeam_Then_Forbidden() throws Exception {
        CreateTeamDto createDto = CreateTeamDto.builder()
                .name("Test Team")
                .keyValue("test-key-123")
                .courseId(UUID.fromString(COURSE_ID))
                .maxSize(5)
                .build();

        User otherTeacher = new User(UUID.fromString(TEACHER_ID), UUID.randomUUID(),
                "other@test.com", "other", "Other", "Test");

        when(userRepository.findById(UUID.fromString(TEACHER_ID))).thenReturn(Optional.of(otherTeacher));
        when(courseService.getCourse(UUID.fromString(COURSE_ID))).thenReturn(course);

        mockMvc.perform(post("/teams")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = STUDENT_ID, authorities = "student")
    void Given_StudentUser_When_CreateTeamsBatch_Then_Forbidden() throws Exception {
        when(userRepository.findById(UUID.fromString(STUDENT_ID))).thenReturn(Optional.of(student));
        CreateTeamBatchDto batchDto = CreateTeamBatchDto.builder()
                .courseId(UUID.fromString(COURSE_ID))
                .prefix("team")
                .teamSize(5)
                .numberOfTeams(3)
                .build();

        mockMvc.perform(post("/teams/batch")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(batchDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = TEACHER_ID, authorities = "teacher")
    void Given_TeacherNotInCourse_When_CreateTeamsBatch_Then_Forbidden() throws Exception {
        CreateTeamBatchDto batchDto = CreateTeamBatchDto.builder()
                .courseId(UUID.fromString(COURSE_ID))
                .prefix("team")
                .teamSize(5)
                .numberOfTeams(3)
                .build();

        User otherTeacher = new User(UUID.fromString(TEACHER_ID), UUID.randomUUID(),
                "other@test.com", "other", "Other", "Test");

        when(userRepository.findById(UUID.fromString(TEACHER_ID))).thenReturn(Optional.of(otherTeacher));
        when(courseService.getCourse(UUID.fromString(COURSE_ID))).thenReturn(course);

        mockMvc.perform(post("/teams/batch")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(batchDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = STUDENT_ID, authorities = "student")
    void Given_StudentUser_When_UpdateTeam_Then_Forbidden() throws Exception {
        when(userRepository.findById(UUID.fromString(STUDENT_ID))).thenReturn(Optional.of(student));
        when(teamService.getTeamById(UUID.fromString(TEAM_ID))).thenReturn(team);
        UpdateTeamDto updateDto = new UpdateTeamDto("Updated Team", 6);
        String etag = "\"valid-etag\"";

        mockMvc.perform(put("/teams/{id}", TEAM_ID)
                        .with(csrf())
                        .header(HttpHeaders.IF_MATCH, etag)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = TEACHER_ID, authorities = "teacher")
    void Given_TeacherNotInCourse_When_UpdateTeam_Then_Forbidden() throws Exception {
        UpdateTeamDto updateDto = new UpdateTeamDto("Updated Team", 6);
        String etag = "\"valid-etag\"";

        User otherTeacher = new User(UUID.fromString(TEACHER_ID), UUID.randomUUID(),
                "other@test.com", "other", "Other", "Test");

        when(userRepository.findById(UUID.fromString(TEACHER_ID))).thenReturn(Optional.of(otherTeacher));
        when(teamService.getTeamById(UUID.fromString(TEAM_ID))).thenReturn(team);

        mockMvc.perform(put("/teams/{id}", TEAM_ID)
                        .with(csrf())
                        .header(HttpHeaders.IF_MATCH, etag)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = STUDENT_ID, authorities = "student")
    void Given_StudentNotInTeam_When_GetTeamDetails_Then_NoContent() throws Exception {
        User otherStudent = new User(UUID.fromString(STUDENT_ID), UUID.randomUUID(),
                "other@test.com", "other", "Other", "Test");
        Team otherTeam = Team.builder()
                .name("Other Team")
                .course(course)
                .maxSize(5)
                .active(true)
                .users(new ArrayList<>())
                .build();
        setEntityId(otherTeam, UUID.fromString(TEAM_ID));

        when(userRepository.findById(UUID.fromString(STUDENT_ID))).thenReturn(Optional.of(otherStudent));
        when(teamService.getTeamById(UUID.fromString(TEAM_ID))).thenReturn(otherTeam);

        mockMvc.perform(get("/teams/{teamId}", TEAM_ID))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = TEACHER_ID, authorities = "teacher")
    void Given_TeacherNotInCourse_When_GetTeamDetails_Then_NoContent() throws Exception {
        User otherTeacher = new User(UUID.fromString(TEACHER_ID), UUID.randomUUID(),
                "other@test.com", "other", "Other", "Test");

        when(userRepository.findById(UUID.fromString(TEACHER_ID))).thenReturn(Optional.of(otherTeacher));
        when(teamService.getTeamById(UUID.fromString(TEAM_ID))).thenReturn(team);

        mockMvc.perform(get("/teams/{teamId}", TEAM_ID))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = STUDENT_ID, authorities = "student")
    void Given_ValidRequest_When_GetTeamsByStudent_Then_Success() throws Exception {
        Page<Team> teamsPage = new PageImpl<>(List.of(team));
        TeamWithCourseDto teamDto = TeamWithCourseDto.builder()
                .id(UUID.fromString(TEAM_ID))
                .name("Test Team")
                .maxSize(5)
                .users(List.of())
                .course(new CourseBasicDto(
                        UUID.fromString(COURSE_ID),
                        "Test Course",
                        null,
                        "TEAM_BASED",
                        course.getClusterId().toString()
                ))
                .build();

        when(teamService.getTeamsByStudent(any(), anyInt(), anyInt(), any(), any()))
                .thenReturn(teamsPage);
        when(teamMapper.teamToTeamWithCourseDto(team)).thenReturn(teamDto);

        mockMvc.perform(get("/teams/student")
                        .param("page", "0")
                        .param("size", "10")
                        .param("search", "test")
                        .param("sort", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(TEAM_ID))
                .andExpect(jsonPath("$.page.page").value(0));
    }

    @Test
    @WithMockUser(username = TEACHER_ID, authorities = "teacher")
    void Given_ValidRequest_When_GetTeamsByCourse_Then_Success() throws Exception {
        Page<Team> teamsPage = new PageImpl<>(List.of(team));
        when(userRepository.findById(UUID.fromString(TEACHER_ID))).thenReturn(Optional.of(teacher));
        when(courseService.getCourse(UUID.fromString(COURSE_ID))).thenReturn(course);
        when(teamService.getTeamsByCourse(any(), anyInt(), anyInt(), any(), any(), any()))
                .thenReturn(teamsPage);
        when(teamAccessKeyRepository.findByTeamId(any())).thenReturn(Optional.of(teamKey));

        mockMvc.perform(get("/teams/course/{courseId}", COURSE_ID)
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .param("search", "test")
                        .param("searchType", "TEAM_NAME")
                        .param("sort", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(TEAM_ID))
                .andExpect(jsonPath("$.items[0].keyValue").value(KEY_VALUE));
    }

    @Test
    @WithMockUser(username = TEACHER_ID, authorities = "teacher")
    void Given_ValidRequest_When_SearchTeamsByEmails_Then_Success() throws Exception {
        List<String> emailPrefixes = List.of("test");
        SearchTeamsByEmailsDto searchDto = new SearchTeamsByEmailsDto(emailPrefixes);

        when(userRepository.findById(UUID.fromString(TEACHER_ID))).thenReturn(Optional.of(teacher));
        when(courseService.getCourse(UUID.fromString(COURSE_ID))).thenReturn(course);
        when(teamService.findTeamsByEmails(any(), any(), any())).thenReturn(List.of(team));
        when(teamAccessKeyRepository.findByTeamId(any())).thenReturn(Optional.of(teamKey));

        mockMvc.perform(post("/teams/course/{courseId}/search-emails", COURSE_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(searchDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(TEAM_ID));
    }

    @Test
    @WithMockUser(username = STUDENT_ID, authorities = "student")
    void Given_ValidKey_When_JoinTeam_Then_Success() throws Exception {
        JoinTeamKeyDto joinDto = new JoinTeamKeyDto(KEY_VALUE);
        when(userRepository.findById(UUID.fromString(STUDENT_ID))).thenReturn(Optional.of(student));
        doNothing().when(teamService).joinUsingKey(KEY_VALUE, student);

        mockMvc.perform(post("/teams/join")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(joinDto)))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = STUDENT_ID, authorities = "student")
    void Given_ValidRequest_When_LeaveTeam_Then_Success() throws Exception {
        LeaveTeamDto leaveDto = new LeaveTeamDto(UUID.fromString(TEAM_ID));
        doNothing().when(teamService).leaveTeam(UUID.fromString(TEAM_ID), UUID.fromString(STUDENT_ID));

        mockMvc.perform(post("/teams/leave")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(leaveDto)))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = STUDENT_ID, authorities = "student")
    void Given_EmptyTeamsList_When_GetTeamsByStudent_Then_NoContent() throws Exception {
        Page<Team> emptyPage = new PageImpl<>(List.of());
        when(teamService.getTeamsByStudent(any(), anyInt(), anyInt(), any(), any()))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/teams/student"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = TEACHER_ID, authorities = "teacher")
    void Given_TeacherNotInCourse_When_GetTeamsByCourse_Then_NoContent() throws Exception {
        User otherTeacher = new User(UUID.fromString(TEACHER_ID), UUID.randomUUID(),
                "other@test.com", "other", "Other", "Test");

        when(userRepository.findById(UUID.fromString(TEACHER_ID))).thenReturn(Optional.of(otherTeacher));
        when(courseService.getCourse(UUID.fromString(COURSE_ID))).thenReturn(course);

        mockMvc.perform(get("/teams/course/{courseId}", COURSE_ID))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = TEACHER_ID, authorities = "teacher")
    void Given_NoTeamsFound_When_SearchTeamsByEmails_Then_NoContent() throws Exception {
        SearchTeamsByEmailsDto searchDto = new SearchTeamsByEmailsDto(List.of("test"));

        when(userRepository.findById(UUID.fromString(TEACHER_ID))).thenReturn(Optional.of(teacher));
        when(courseService.getCourse(UUID.fromString(COURSE_ID))).thenReturn(course);
        when(teamService.findTeamsByEmails(any(), any(), any())).thenReturn(List.of());

        mockMvc.perform(post("/teams/course/{courseId}/search-emails", COURSE_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(searchDto)))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = TEACHER_ID, authorities = "teacher")
    void Given_ValidRequest_When_AddStudentToTeam_Then_Success() throws Exception {
        EmailDto emailDto = new EmailDto("newstudent@test.com");

        when(userRepository.findById(UUID.fromString(TEACHER_ID))).thenReturn(Optional.of(teacher));
        when(teamService.getTeamById(UUID.fromString(TEAM_ID))).thenReturn(team);
        doNothing().when(teamService).addStudentToTeam(team, "newstudent@test.com");

        mockMvc.perform(post("/teams/{teamId}/add-student", TEAM_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(emailDto)))
                .andExpect(status().isNoContent());

        verify(teamService).addStudentToTeam(team, "newstudent@test.com");
    }

    @Test
    @WithMockUser(username = TEACHER_ID, authorities = "teacher")
    void Given_TeacherNotInCourse_When_AddStudentToTeam_Then_Forbidden() throws Exception {
        EmailDto emailDto = new EmailDto("newstudent@test.com");
        User otherTeacher = new User(UUID.fromString(TEACHER_ID), UUID.randomUUID(),
                "other@test.com", "other", "Other", "Test");

        when(userRepository.findById(UUID.fromString(TEACHER_ID))).thenReturn(Optional.of(otherTeacher));
        when(teamService.getTeamById(UUID.fromString(TEAM_ID))).thenReturn(team);

        mockMvc.perform(post("/teams/{teamId}/add-student", TEAM_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(emailDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = TEACHER_ID, authorities = "teacher")
    void Given_ValidRequest_When_RemoveStudentFromTeam_Then_Success() throws Exception {
        EmailDto emailDto = new EmailDto("student@test.com");

        when(userRepository.findById(UUID.fromString(TEACHER_ID))).thenReturn(Optional.of(teacher));
        when(teamService.getTeamById(UUID.fromString(TEAM_ID))).thenReturn(team);
        doNothing().when(teamService).removeStudentFromTeam(team, "student@test.com");

        mockMvc.perform(post("/teams/{teamId}/remove-student", TEAM_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(emailDto)))
                .andExpect(status().isNoContent());

        verify(teamService).removeStudentFromTeam(team, "student@test.com");
    }

    @Test
    @WithMockUser(username = TEACHER_ID, authorities = "teacher")
    void Given_TeacherNotInCourse_When_RemoveStudentFromTeam_Then_Forbidden() throws Exception {
        EmailDto emailDto = new EmailDto("student@test.com");
        User otherTeacher = new User(UUID.fromString(TEACHER_ID), UUID.randomUUID(),
                "other@test.com", "other", "Other", "Test");

        when(userRepository.findById(UUID.fromString(TEACHER_ID))).thenReturn(Optional.of(otherTeacher));
        when(teamService.getTeamById(UUID.fromString(TEAM_ID))).thenReturn(team);

        mockMvc.perform(post("/teams/{teamId}/remove-student", TEAM_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(emailDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = TEACHER_ID, authorities = "teacher")
    void Given_ValidRequest_When_DeleteTeam_Then_Success() throws Exception {
        when(userRepository.findById(UUID.fromString(TEACHER_ID))).thenReturn(Optional.of(teacher));
        when(teamService.getTeamById(UUID.fromString(TEAM_ID))).thenReturn(team);
        doNothing().when(teamService).deleteTeam(team);

        mockMvc.perform(delete("/teams/{teamId}", TEAM_ID)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(teamService).deleteTeam(team);
    }

    @Test
    @WithMockUser(username = TEACHER_ID, authorities = "teacher")
    void Given_TeacherNotInCourse_When_DeleteTeam_Then_Forbidden() throws Exception {
        User otherTeacher = new User(UUID.fromString(TEACHER_ID), UUID.randomUUID(),
                "other@test.com", "other", "Other", "Test");

        when(userRepository.findById(UUID.fromString(TEACHER_ID))).thenReturn(Optional.of(otherTeacher));
        when(teamService.getTeamById(UUID.fromString(TEAM_ID))).thenReturn(team);

        mockMvc.perform(delete("/teams/{teamId}", TEAM_ID)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = STUDENT_ID, authorities = "student")
    void Given_StudentUser_When_AddStudentToTeam_Then_Forbidden() throws Exception {
        when(userRepository.findById(UUID.fromString(STUDENT_ID))).thenReturn(Optional.of(student));
        when(teamService.getTeamById(UUID.fromString(TEAM_ID))).thenReturn(team);
        EmailDto emailDto = new EmailDto("newstudent@test.com");

        mockMvc.perform(post("/teams/{teamId}/add-student", TEAM_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(emailDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = STUDENT_ID, authorities = "student")
    void Given_StudentUser_When_RemoveStudentFromTeam_Then_Forbidden() throws Exception {
        when(userRepository.findById(UUID.fromString(STUDENT_ID))).thenReturn(Optional.of(student));
        when(teamService.getTeamById(UUID.fromString(TEAM_ID))).thenReturn(team);
        EmailDto emailDto = new EmailDto("student@test.com");

        mockMvc.perform(post("/teams/{teamId}/remove-student", TEAM_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(emailDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = STUDENT_ID, authorities = "student")
    void Given_StudentUser_When_DeleteTeam_Then_Forbidden() throws Exception {
        when(userRepository.findById(UUID.fromString(STUDENT_ID))).thenReturn(Optional.of(student));
        when(teamService.getTeamById(UUID.fromString(TEAM_ID))).thenReturn(team);

        mockMvc.perform(delete("/teams/{teamId}", TEAM_ID)
                        .with(csrf()))
                .andExpect(status().isForbidden());
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