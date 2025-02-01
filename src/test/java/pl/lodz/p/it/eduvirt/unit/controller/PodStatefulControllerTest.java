package pl.lodz.p.it.eduvirt.unit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;
import pl.lodz.p.it.eduvirt.aspect.exception.GeneralControllerExceptionResolver;
import pl.lodz.p.it.eduvirt.controller.PodStatefulController;
import pl.lodz.p.it.eduvirt.dto.course.CourseBasicDto;
import pl.lodz.p.it.eduvirt.dto.pod.CreatePodStatefulDto;
import pl.lodz.p.it.eduvirt.dto.pod.PodStatefulDetailsDto;
import pl.lodz.p.it.eduvirt.dto.resource_group.ResourceGroupDto;
import pl.lodz.p.it.eduvirt.dto.team.TeamDto;
import pl.lodz.p.it.eduvirt.entity.*;
import pl.lodz.p.it.eduvirt.entity.key.CourseType;
import pl.lodz.p.it.eduvirt.mappers.PodStatefulMapper;
import pl.lodz.p.it.eduvirt.mappers.PodStatefulMapperImpl;
import pl.lodz.p.it.eduvirt.repository.UserRepository;
import pl.lodz.p.it.eduvirt.service.*;

@Import({
        PodStatefulController.class,
        GeneralControllerExceptionResolver.class,
        PodStatefulMapperImpl.class,
        ObjectMapper.class
})
@WebMvcTest(controllers = {PodStatefulController.class}, useDefaultFilters = false)
public class PodStatefulControllerTest {

    private static final String STUDENT_ID = "11111111-1111-1111-1111-111111111111";
    private static final String TEACHER_ID = "22222222-2222-2222-2222-222222222222";
    private static final String ADMIN_ID = "33333333-3333-3333-3333-333333333333";
    private static final String COURSE_ID = "44444444-4444-4444-4444-444444444444";
    private static final String TEAM_ID = "55555555-5555-5555-5555-555555555555";
    private static final String POD_ID = "66666666-6666-6666-6666-666666666666";
    private static final String RG_ID = "77777777-7777-7777-7777-777777777777";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PodStatefulService podStatefulService;

    @MockitoBean
    private TeamService teamService;

    @MockitoBean
    private CourseService courseService;

    @MockitoBean
    private ResourceGroupService resourceGroupService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoSpyBean
    private PodStatefulMapper podStatefulMapper;

    @MockitoSpyBean
    private final ObjectMapper mapper = new ObjectMapper();

    private Course course;
    private Team team;
    private User student;
    private User teacher;
    private User admin;
    private PodStateful podStateful;
    private ResourceGroup resourceGroup;

    @BeforeEach
    void setUp() {
        mapper.findAndRegisterModules();
        setupTestData();
    }

    private void setupTestData() {
        course = Course.builder()
                .name("Test Course")
                .teachers(new ArrayList<>())
                .teams(new ArrayList<>())
                .clusterId(UUID.randomUUID())
                .courseType(CourseType.TEAM_BASED)
                .build();
        setEntityId(course, UUID.fromString(COURSE_ID));

        team = Team.builder()
                .name("Test Team")
                .course(course)
                .maxSize(5)
                .active(true)
                .users(new ArrayList<>())
                .statefulPods(new ArrayList<>())
                .build();
        setEntityId(team, UUID.fromString(TEAM_ID));

        resourceGroup = ResourceGroup.builder()
                .name("Test RG")
                .stateless(false)
                .build();
        setEntityId(resourceGroup, UUID.fromString(RG_ID));

        student = new User(UUID.fromString(STUDENT_ID), UUID.randomUUID(),
                "student@test.com", "student", "Student", "Test");
        teacher = new User(UUID.fromString(TEACHER_ID), UUID.randomUUID(),
                "teacher@test.com", "teacher", "Teacher", "Test");
        admin = new User(UUID.fromString(ADMIN_ID), UUID.randomUUID(),
                "admin@test.com", "admin", "Admin", "Test");

        podStateful = new PodStateful();
        podStateful.setTeam(team);
        podStateful.setResourceGroup(resourceGroup);
        podStateful.setMaxRent(10);
        podStateful.setCourse(course);
        setEntityId(podStateful, UUID.fromString(POD_ID));

        course.getTeachers().add(teacher);
        course.getTeams().add(team);
        team.getUsers().add(student);
        team.getStatefulPods().add(podStateful);
    }

    @Test
    @WithMockUser(username = TEACHER_ID, authorities = "teacher")
    void Given_ValidData_When_CreateStatefulPod_Then_Success() throws Exception {
        CreatePodStatefulDto createDto = new CreatePodStatefulDto(
                UUID.fromString(TEAM_ID),
                UUID.fromString(RG_ID),
                10
        );

        when(userRepository.findById(UUID.fromString(TEACHER_ID))).thenReturn(Optional.of(teacher));
        when(teamService.getTeamById(UUID.fromString(TEAM_ID))).thenReturn(team);
        when(podStatefulService.createStatefulPod(any(), any(), any())).thenReturn(podStateful);

        mockMvc.perform(post("/pods/stateful")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(podStatefulService).createStatefulPod(any(), any(), any());
    }

    @Test
    @WithMockUser(username = STUDENT_ID, authorities = "student")
    void Given_StudentUser_When_CreateStatefulPod_Then_Forbidden() throws Exception {
        when(userRepository.findById(UUID.fromString(STUDENT_ID))).thenReturn(Optional.of(student));
        when(teamService.getTeamById(UUID.fromString(TEAM_ID))).thenReturn(team);
        CreatePodStatefulDto createDto = new CreatePodStatefulDto(
                UUID.fromString(TEAM_ID),
                UUID.fromString(RG_ID),
                10
        );

        mockMvc.perform(post("/pods/stateful")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = TEACHER_ID, authorities = "teacher")
    void Given_TeacherNotInCourse_When_CreateStatefulPod_Then_Forbidden() throws Exception {
        CreatePodStatefulDto createDto = new CreatePodStatefulDto(
                UUID.fromString(TEAM_ID),
                UUID.fromString(RG_ID),
                10
        );

        User otherTeacher = new User(UUID.fromString(TEACHER_ID), UUID.randomUUID(),
                "other@test.com", "other", "Other", "Test");

        when(userRepository.findById(UUID.fromString(TEACHER_ID))).thenReturn(Optional.of(otherTeacher));
        when(teamService.getTeamById(UUID.fromString(TEAM_ID))).thenReturn(team);

        mockMvc.perform(post("/pods/stateful")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = STUDENT_ID, authorities = "student")
    void Given_ValidRequest_When_GetPodsByTeam_Then_Success() throws Exception {
        List<PodStateful> pods = List.of(podStateful);

        ResourceGroupDto rgDto = new ResourceGroupDto(
                RG_ID,
                "Test RG",
                null,
                false,
                0
        );

        CourseBasicDto courseDto = new CourseBasicDto(
                UUID.fromString(COURSE_ID),
                "Test Course",
                null,
                "TEAM_BASED",
                course.getClusterId().toString()
        );

        TeamDto teamDto = TeamDto.builder()
                .id(UUID.fromString(TEAM_ID))
                .name("Test Team")
                .maxSize(5)
                .users(List.of())
                .build();

        PodStatefulDetailsDto dto = new PodStatefulDetailsDto(
                UUID.fromString(POD_ID),
                rgDto,
                courseDto,
                teamDto,
                10
        );

        when(userRepository.findById(UUID.fromString(STUDENT_ID))).thenReturn(Optional.of(student));
        when(teamService.getTeamById(UUID.fromString(TEAM_ID))).thenReturn(team);
        when(podStatefulService.getStatefulPodsByTeam(UUID.fromString(TEAM_ID))).thenReturn(pods);
        when(podStatefulMapper.podStatefulToDetailsDto(podStateful)).thenReturn(dto);

        mockMvc.perform(get("/pods/stateful/team/{teamId}", TEAM_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(POD_ID))
                .andExpect(jsonPath("$[0].resourceGroup.id").value(RG_ID))
                .andExpect(jsonPath("$[0].course.id").value(COURSE_ID))
                .andExpect(jsonPath("$[0].team.id").value(TEAM_ID))
                .andExpect(jsonPath("$[0].maxRent").value(10));

        verify(podStatefulMapper).podStatefulToDetailsDto(podStateful);
    }

    @Test
    @WithMockUser(username = TEACHER_ID, authorities = "teacher")
    void Given_ValidRequest_When_GetPodsByCourse_Then_Success() throws Exception {
        List<PodStateful> pods = List.of(podStateful);
        when(userRepository.findById(UUID.fromString(TEACHER_ID))).thenReturn(Optional.of(teacher));
        when(courseService.getCourse(UUID.fromString(COURSE_ID))).thenReturn(course);
        when(podStatefulService.getStatefulPodsByCourse(UUID.fromString(COURSE_ID))).thenReturn(pods);

        mockMvc.perform(get("/pods/stateful/course/{courseId}", COURSE_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(username = TEACHER_ID, authorities = "teacher")
    void Given_ValidRequest_When_DeletePod_Then_Success() throws Exception {
        when(userRepository.findById(UUID.fromString(TEACHER_ID))).thenReturn(Optional.of(teacher));
        when(podStatefulService.getStatefulPodById(UUID.fromString(POD_ID))).thenReturn(podStateful);

        mockMvc.perform(delete("/pods/stateful/{podId}", POD_ID)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(podStatefulService).deleteStatefulPod(UUID.fromString(POD_ID));
    }

    @Test
    @WithAnonymousUser
    void Given_AnonymousUser_When_AccessEndpoint_Then_Unauthorized() throws Exception {
        mockMvc.perform(get("/pods/stateful/team/{teamId}", TEAM_ID))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = TEACHER_ID, authorities = "teacher")
    void Given_UserNotFound_When_CreateStatefulPod_Then_NotFound() throws Exception {
        CreatePodStatefulDto createDto = new CreatePodStatefulDto(
                UUID.fromString(TEAM_ID),
                UUID.fromString(RG_ID),
                10
        );

        when(userRepository.findById(UUID.fromString(TEACHER_ID))).thenReturn(Optional.empty());

        mockMvc.perform(post("/pods/stateful")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = STUDENT_ID, authorities = "student")
    void Given_UserNotFound_When_GetPodsByTeam_Then_NotFound() throws Exception {
        when(userRepository.findById(UUID.fromString(STUDENT_ID))).thenReturn(Optional.empty());

        mockMvc.perform(get("/pods/stateful/team/{teamId}", TEAM_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = STUDENT_ID, authorities = "student")
    void Given_EmptyPodList_When_GetPodsByTeam_Then_NoContent() throws Exception {
        when(userRepository.findById(UUID.fromString(STUDENT_ID))).thenReturn(Optional.of(student));
        when(teamService.getTeamById(UUID.fromString(TEAM_ID))).thenReturn(team);
        when(podStatefulService.getStatefulPodsByTeam(UUID.fromString(TEAM_ID))).thenReturn(List.of());

        mockMvc.perform(get("/pods/stateful/team/{teamId}", TEAM_ID))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = TEACHER_ID, authorities = "teacher")
    void Given_EmptyPodList_When_GetPodsByCourse_Then_NoContent() throws Exception {
        when(userRepository.findById(UUID.fromString(TEACHER_ID))).thenReturn(Optional.of(teacher));
        when(courseService.getCourse(UUID.fromString(COURSE_ID))).thenReturn(course);
        when(podStatefulService.getStatefulPodsByCourse(UUID.fromString(COURSE_ID))).thenReturn(List.of());

        mockMvc.perform(get("/pods/stateful/course/{courseId}", COURSE_ID))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = TEACHER_ID, authorities = "teacher")
    void Given_TeacherNotInCourse_When_DeletePod_Then_Forbidden() throws Exception {
        User otherTeacher = new User(UUID.fromString(TEACHER_ID), UUID.randomUUID(),
                "other@test.com", "other", "Other", "Test");

        when(userRepository.findById(UUID.fromString(TEACHER_ID))).thenReturn(Optional.of(otherTeacher));
        when(podStatefulService.getStatefulPodById(UUID.fromString(POD_ID))).thenReturn(podStateful);

        mockMvc.perform(delete("/pods/stateful/{podId}", POD_ID)
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