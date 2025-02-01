package pl.lodz.p.it.eduvirt.unit.controller;

import static org.mockito.ArgumentMatchers.any;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;
import pl.lodz.p.it.eduvirt.aspect.exception.GeneralControllerExceptionResolver;
import pl.lodz.p.it.eduvirt.controller.PodStatelessController;
import pl.lodz.p.it.eduvirt.dto.course.CourseBasicDto;
import pl.lodz.p.it.eduvirt.dto.pod.*;
import pl.lodz.p.it.eduvirt.dto.resource_group_pool.ResourceGroupPoolWithMaxRentTimeDto;
import pl.lodz.p.it.eduvirt.dto.team.TeamDto;
import pl.lodz.p.it.eduvirt.entity.*;
import pl.lodz.p.it.eduvirt.entity.key.CourseType;
import pl.lodz.p.it.eduvirt.mappers.PodStatelessMapper;
import pl.lodz.p.it.eduvirt.mappers.PodStatelessMapperImpl;
import pl.lodz.p.it.eduvirt.repository.UserRepository;
import pl.lodz.p.it.eduvirt.service.*;

@Import({
        PodStatelessController.class,
        GeneralControllerExceptionResolver.class,
        PodStatelessMapperImpl.class,
        ObjectMapper.class
})
@WebMvcTest(controllers = {PodStatelessController.class}, useDefaultFilters = false)
public class PodStatelessControllerTest {

    private static final String STUDENT_ID = "11111111-1111-1111-1111-111111111111";
    private static final String TEACHER_ID = "22222222-2222-2222-2222-222222222222";
    private static final String ADMIN_ID = "33333333-3333-3333-3333-333333333333";
    private static final String COURSE_ID = "44444444-4444-4444-4444-444444444444";
    private static final String TEAM_ID = "55555555-5555-5555-5555-555555555555";
    private static final String POD_ID = "66666666-6666-6666-6666-666666666666";
    private static final String POOL_ID = "77777777-7777-7777-7777-777777777777";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PodStatelessService podStatelessService;

    @MockitoBean
    private TeamService teamService;

    @MockitoBean
    private CourseService courseService;

    @MockitoBean
    private ResourceGroupPoolService resourceGroupPoolService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoSpyBean
    private PodStatelessMapper podStatelessMapper;

    @MockitoSpyBean
    private final ObjectMapper mapper = new ObjectMapper();

    private Course course;
    private Team team;
    private User student;
    private User teacher;
    private User admin;
    private PodStateless podStateless;
    private ResourceGroupPool resourceGroupPool;

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
                .statelessPods(new ArrayList<>())
                .build();
        setEntityId(team, UUID.fromString(TEAM_ID));

        resourceGroupPool = ResourceGroupPool.builder()
                .name("Test Pool")
                .maxRentTime(60)
                .course(course)
                .build();
        setEntityId(resourceGroupPool, UUID.fromString(POOL_ID));

        student = new User(UUID.fromString(STUDENT_ID), UUID.randomUUID(),
                "student@test.com", "student", "Student", "Test");
        teacher = new User(UUID.fromString(TEACHER_ID), UUID.randomUUID(),
                "teacher@test.com", "teacher", "Teacher", "Test");
        admin = new User(UUID.fromString(ADMIN_ID), UUID.randomUUID(),
                "admin@test.com", "admin", "Admin", "Test");

        podStateless = new PodStateless();
        podStateless.setTeam(team);
        podStateless.setResourceGroupPool(resourceGroupPool);
        podStateless.setCourse(course);
        setEntityId(podStateless, UUID.fromString(POD_ID));

        course.getTeachers().add(teacher);
        course.getTeams().add(team);
        team.getUsers().add(student);
        team.getStatelessPods().add(podStateless);
    }

    @Test
    @WithMockUser(username = TEACHER_ID, authorities = "teacher")
    void Given_ValidData_When_CreateStatelessPod_Then_Success() throws Exception {
        CreatePodStatelessDto createDto = new CreatePodStatelessDto(
                UUID.fromString(TEAM_ID),
                UUID.fromString(POOL_ID)
        );

        PodStatelessDto responseDto = new PodStatelessDto(
                UUID.fromString(POD_ID),
                UUID.fromString(TEAM_ID),
                UUID.fromString(COURSE_ID),
                UUID.fromString(POOL_ID)
        );

        when(userRepository.findById(UUID.fromString(TEACHER_ID))).thenReturn(Optional.of(teacher));
        when(teamService.getTeamById(UUID.fromString(TEAM_ID))).thenReturn(team);
        when(podStatelessService.createStatelessPod(any(), any(), any())).thenReturn(podStateless);
        when(podStatelessMapper.podStatelessToDto(podStateless)).thenReturn(responseDto);

        mockMvc.perform(post("/pods/stateless")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(POD_ID));
    }

    @Test
    @WithMockUser(username = TEACHER_ID, authorities = "teacher")
    void Given_ValidData_When_CreateStatelessPodsBatch_Then_Success() throws Exception {
        List<CreatePodStatelessDto> createDtos = List.of(
                new CreatePodStatelessDto(UUID.fromString(TEAM_ID), UUID.fromString(POOL_ID))
        );

        List<PodStateless> createdPods = List.of(podStateless);
        List<PodStatelessDto> responseDtos = List.of(
                new PodStatelessDto(UUID.fromString(POD_ID), UUID.fromString(TEAM_ID),
                        UUID.fromString(COURSE_ID), UUID.fromString(POOL_ID))
        );

        when(userRepository.findById(UUID.fromString(TEACHER_ID))).thenReturn(Optional.of(teacher));
        when(teamService.getTeamById(UUID.fromString(TEAM_ID))).thenReturn(team);
        when(podStatelessService.createStatelessPodsBatch(any(), any(), any())).thenReturn(createdPods);
        when(podStatelessMapper.podStatelessToDto(podStateless)).thenReturn(responseDtos.getFirst());

        mockMvc.perform(post("/pods/stateless/batch")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDtos)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(POD_ID));
    }

    @Test
    @WithMockUser(username = STUDENT_ID, authorities = "student")
    void Given_ValidRequest_When_GetPodsByTeam_Then_Success() throws Exception {
        List<PodStateless> pods = List.of(podStateless);

        ResourceGroupPoolWithMaxRentTimeDto poolDto = new ResourceGroupPoolWithMaxRentTimeDto(
                UUID.fromString(POOL_ID),
                "Test Pool",
                null,
                60
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

        PodStatelessDetailsDto dto = new PodStatelessDetailsDto(
                UUID.fromString(POD_ID),
                poolDto,
                courseDto,
                teamDto,
                null
        );

        when(userRepository.findById(UUID.fromString(STUDENT_ID))).thenReturn(Optional.of(student));
        when(teamService.getTeamById(UUID.fromString(TEAM_ID))).thenReturn(team);
        when(podStatelessService.getStatelessPodsByTeam(UUID.fromString(TEAM_ID))).thenReturn(pods);
        when(podStatelessMapper.podStatelessToDetailsDto(podStateless)).thenReturn(dto);

        mockMvc.perform(get("/pods/stateless/team/{teamId}", TEAM_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(POD_ID));
    }

    @Test
    @WithMockUser(username = TEACHER_ID, authorities = "teacher")
    void Given_ValidRequest_When_DeleteStatelessPodsBatch_Then_Success() throws Exception {
        List<UUID> podIds = List.of(UUID.fromString(POD_ID));

        when(userRepository.findById(UUID.fromString(TEACHER_ID))).thenReturn(Optional.of(teacher));
        when(podStatelessService.getStatelessPodById(UUID.fromString(POD_ID))).thenReturn(podStateless);

        mockMvc.perform(delete("/pods/stateless/batch")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(podIds)))
                .andExpect(status().isNoContent());

        verify(podStatelessService).deleteStatelessPodsBatch(podIds);
    }

    @Test
    @WithMockUser(username = TEACHER_ID, authorities = "teacher")
    void Given_EmptyList_When_DeleteStatelessPodsBatch_Then_BadRequest() throws Exception {
        List<UUID> podIds = List.of();

        mockMvc.perform(delete("/pods/stateless/batch")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(podIds)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = TEACHER_ID, authorities = "teacher")
    void Given_ValidRequest_When_GetPodsByPool_Then_Success() throws Exception {
        List<PodStateless> pods = List.of(podStateless);
        List<PodStatelessDto> responseDtos = List.of(
                new PodStatelessDto(UUID.fromString(POD_ID), UUID.fromString(TEAM_ID),
                        UUID.fromString(COURSE_ID), UUID.fromString(POOL_ID))
        );

        when(userRepository.findById(UUID.fromString(TEACHER_ID))).thenReturn(Optional.of(teacher));
        when(resourceGroupPoolService.getResourceGroupPool(UUID.fromString(POOL_ID))).thenReturn(resourceGroupPool);
        when(podStatelessService.getStatelessPodsByResourceGroupPool(UUID.fromString(POOL_ID))).thenReturn(pods);
        when(podStatelessMapper.podStatelessToDto(podStateless)).thenReturn(responseDtos.getFirst());

        mockMvc.perform(get("/pods/stateless/resource-group-pool/{poolId}", POOL_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(POD_ID));
    }

    @Test
    @WithAnonymousUser
    void Given_AnonymousUser_When_AccessEndpoint_Then_Unauthorized() throws Exception {
        mockMvc.perform(get("/pods/stateless/team/{teamId}", TEAM_ID))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = TEACHER_ID, authorities = "teacher")
    void Given_ValidRequest_When_GetPodsByCourse_Then_Success() throws Exception {
        List<PodStateless> pods = List.of(podStateless);
        List<PodStatelessDetailsDto> dtos = List.of(new PodStatelessDetailsDto(
                UUID.fromString(POD_ID),
                new ResourceGroupPoolWithMaxRentTimeDto(UUID.fromString(POOL_ID), "Test Pool", null, 60),
                new CourseBasicDto(UUID.fromString(COURSE_ID), "Test Course", null, "TEAM_BASED",
                        course.getClusterId().toString()),
                TeamDto.builder().id(UUID.fromString(TEAM_ID)).name("Test Team").maxSize(5).users(List.of()).build(),
                null
        ));

        when(userRepository.findById(UUID.fromString(TEACHER_ID))).thenReturn(Optional.of(teacher));
        when(courseService.getCourse(UUID.fromString(COURSE_ID))).thenReturn(course);
        when(podStatelessService.getStatelessPodsByCourse(UUID.fromString(COURSE_ID))).thenReturn(pods);
        when(podStatelessMapper.podStatelessToDetailsDto(podStateless)).thenReturn(dtos.getFirst());

        mockMvc.perform(get("/pods/stateless/course/{courseId}", COURSE_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(POD_ID));
    }

    @Test
    @WithMockUser(username = TEACHER_ID, authorities = "teacher")
    void Given_EmptyList_When_GetPodsByCourse_Then_NoContent() throws Exception {
        when(userRepository.findById(UUID.fromString(TEACHER_ID))).thenReturn(Optional.of(teacher));
        when(courseService.getCourse(UUID.fromString(COURSE_ID))).thenReturn(course);
        when(podStatelessService.getStatelessPodsByCourse(UUID.fromString(COURSE_ID))).thenReturn(List.of());

        mockMvc.perform(get("/pods/stateless/course/{courseId}", COURSE_ID))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = TEACHER_ID, authorities = "teacher")
    void Given_TeacherNotInCourse_When_GetPodsByCourse_Then_NoContent() throws Exception {
        User otherTeacher = new User(UUID.fromString(TEACHER_ID), UUID.randomUUID(),
                "other@test.com", "other", "Other", "Test");
        Course otherCourse = Course.builder().name("Other Course").teachers(new ArrayList<>()).build();
        setEntityId(otherCourse, UUID.fromString(COURSE_ID));

        when(userRepository.findById(UUID.fromString(TEACHER_ID))).thenReturn(Optional.of(otherTeacher));
        when(courseService.getCourse(UUID.fromString(COURSE_ID))).thenReturn(otherCourse);

        mockMvc.perform(get("/pods/stateless/course/{courseId}", COURSE_ID))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = TEACHER_ID, authorities = "teacher")
    void Given_ValidRequest_When_DeleteStatelessPod_Then_Success() throws Exception {
        when(userRepository.findById(UUID.fromString(TEACHER_ID))).thenReturn(Optional.of(teacher));
        when(podStatelessService.getStatelessPodById(UUID.fromString(POD_ID))).thenReturn(podStateless);

        mockMvc.perform(delete("/pods/stateless/{podId}", POD_ID)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(podStatelessService).deleteStatelessPod(UUID.fromString(POD_ID));
    }

    @Test
    @WithMockUser(username = TEACHER_ID, authorities = "teacher")
    void Given_TeacherNotInCourse_When_DeleteStatelessPod_Then_Forbidden() throws Exception {
        User otherTeacher = new User(UUID.fromString(TEACHER_ID), UUID.randomUUID(),
                "other@test.com", "other", "Other", "Test");

        when(userRepository.findById(UUID.fromString(TEACHER_ID))).thenReturn(Optional.of(otherTeacher));
        when(podStatelessService.getStatelessPodById(UUID.fromString(POD_ID))).thenReturn(podStateless);

        mockMvc.perform(delete("/pods/stateless/{podId}", POD_ID)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = TEACHER_ID, authorities = "teacher")
    void Given_UserNotFound_When_DeleteStatelessPod_Then_NotFound() throws Exception {
        when(userRepository.findById(UUID.fromString(TEACHER_ID))).thenReturn(Optional.empty());

        mockMvc.perform(delete("/pods/stateless/{podId}", POD_ID)
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = STUDENT_ID, authorities = "student")
    void Given_StudentUser_When_CreateStatelessPod_Then_Forbidden() throws Exception {
        when(userRepository.findById(UUID.fromString(STUDENT_ID))).thenReturn(Optional.of(student));
        when(teamService.getTeamById(UUID.fromString(TEAM_ID))).thenReturn(team);
        CreatePodStatelessDto createDto = new CreatePodStatelessDto(
                UUID.fromString(TEAM_ID),
                UUID.fromString(POOL_ID)
        );

        mockMvc.perform(post("/pods/stateless")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = STUDENT_ID, authorities = "student")
    void Given_StudentUser_When_CreateStatelessPodsBatch_Then_Forbidden() throws Exception {
        when(userRepository.findById(UUID.fromString(STUDENT_ID))).thenReturn(Optional.of(student));
        when(teamService.getTeamById(UUID.fromString(TEAM_ID))).thenReturn(team);
        List<CreatePodStatelessDto> createDtos = List.of(
                new CreatePodStatelessDto(UUID.fromString(TEAM_ID), UUID.fromString(POOL_ID))
        );

        mockMvc.perform(post("/pods/stateless/batch")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDtos)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = STUDENT_ID, authorities = "student")
    void Given_StudentUser_When_GetPodsByPool_Then_NoContent() throws Exception {
        when(userRepository.findById(UUID.fromString(STUDENT_ID))).thenReturn(Optional.of(student));
        when(resourceGroupPoolService.getResourceGroupPool(UUID.fromString(POOL_ID))).thenReturn(resourceGroupPool);
        mockMvc.perform(get("/pods/stateless/resource-group-pool/{poolId}", POOL_ID))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = STUDENT_ID, authorities = "student")
    void Given_EmptyTeamPods_When_GetPodsByTeam_Then_NoContent() throws Exception {
        when(userRepository.findById(UUID.fromString(STUDENT_ID))).thenReturn(Optional.of(student));
        when(teamService.getTeamById(UUID.fromString(TEAM_ID))).thenReturn(team);
        when(podStatelessService.getStatelessPodsByTeam(UUID.fromString(TEAM_ID))).thenReturn(List.of());

        mockMvc.perform(get("/pods/stateless/team/{teamId}", TEAM_ID))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = TEACHER_ID, authorities = "teacher")
    void Given_EmptyPoolPods_When_GetPodsByPool_Then_NoContent() throws Exception {
        when(userRepository.findById(UUID.fromString(TEACHER_ID))).thenReturn(Optional.of(teacher));
        when(resourceGroupPoolService.getResourceGroupPool(UUID.fromString(POOL_ID))).thenReturn(resourceGroupPool);
        when(podStatelessService.getStatelessPodsByResourceGroupPool(UUID.fromString(POOL_ID))).thenReturn(List.of());

        mockMvc.perform(get("/pods/stateless/resource-group-pool/{poolId}", POOL_ID))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = TEACHER_ID, authorities = "teacher")
    void Given_EmptyList_When_CreateStatelessPodsBatch_Then_BadRequest() throws Exception {
        List<CreatePodStatelessDto> createDtos = List.of();

        mockMvc.perform(post("/pods/stateless/batch")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDtos)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = STUDENT_ID, authorities = "student")
    void Given_StudentUser_When_DeleteStatelessPodsBatch_Then_Forbidden() throws Exception {
        when(userRepository.findById(UUID.fromString(STUDENT_ID))).thenReturn(Optional.of(student));
        List<UUID> podIds = List.of(UUID.fromString(POD_ID));

        mockMvc.perform(delete("/pods/stateless/batch")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(podIds)))
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