package pl.lodz.p.it.eduvirt.unit.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.lodz.p.it.eduvirt.aspect.exception.GeneralControllerExceptionResolver;
import pl.lodz.p.it.eduvirt.controller.CourseController;
import pl.lodz.p.it.eduvirt.dto.EmailDto;
import pl.lodz.p.it.eduvirt.dto.course.CourseDto;
import pl.lodz.p.it.eduvirt.dto.resources.ResourcesAvailabilityDto;
import pl.lodz.p.it.eduvirt.dto.statistics.BaseCourseStatsDto;
import pl.lodz.p.it.eduvirt.dto.statistics.CourseStatsDto;
import pl.lodz.p.it.eduvirt.dto.statistics.TeamStatsDto;
import pl.lodz.p.it.eduvirt.entity.*;
import pl.lodz.p.it.eduvirt.exceptions.course.CourseNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.resource_group.ResourceGroupNotFoundException;
import pl.lodz.p.it.eduvirt.mappers.*;
import pl.lodz.p.it.eduvirt.repository.UserRepository;
import pl.lodz.p.it.eduvirt.service.*;
import pl.lodz.p.it.eduvirt.util.etag.ETagHelper;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import({
        CourseController.class, GeneralControllerExceptionResolver.class,
        CourseMapperImpl.class, ResourceGroupMapperImpl.class,
        UserMapperImpl.class, RGPoolMapperImpl.class,
        ReservationMapperImpl.class, TeamMapperImpl.class,
})
@WebMvcTest(controllers = {CourseController.class}, useDefaultFilters = false)
public class CourseControllerTest {

    /* MockMVC */

    @Autowired
    private MockMvc mockMvc;

    /* Services */

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private ResourceGroupService resourceGroupService;

    @MockitoBean
    private ResourceGroupPoolService resourceGroupPoolService;

    @MockitoBean
    private TeamService teamService;

    @MockitoBean
    private CourseService courseService;

    @MockitoBean
    private ReservationStatisticsService reservationStatisticsService;

    /* Repositories */

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private ETagHelper eTagHelper;

    /* Mappers */

    @MockitoSpyBean
    private CourseMapper courseMapper;

    @MockitoSpyBean
    private RGPoolMapper rgPoolMapper;

    @MockitoSpyBean
    private ResourceGroupMapper resourceGroupMapper;

    @MockitoSpyBean
    private UserMapper userMapper;

    private final ObjectMapper mapper = new ObjectMapper();

    /* Data initialization */

    private UUID existingClusterId = UUID.randomUUID();
    private UUID nonExistentClusterId = UUID.randomUUID();

    private Course course1;
    private Course course2;
    private Course course3;

    private Team team1;
    private Team team2;

    private final UUID studentId1 = UUID.fromString("e7a9f27d-3ef9-4d65-b82c-d902acf3bd9c");
    private final UUID studentId2 = UUID.fromString("7295a120-fba9-4bed-87a9-5631185ad334");
    private final UUID studentId3 = UUID.fromString("ff31a99c-c4fb-4da8-9ec0-479b0dfd071c");
    private final UUID studentId4 = UUID.fromString("61cba42c-fc61-4fbb-beea-b2423a078ae6");
    private final UUID studentId5 = UUID.fromString("a5bf71c0-9e18-45cc-be57-6dabbb7cadb5");

    private final UUID adminId = UUID.fromString("62bddaa4-c9ad-4afc-a0af-3941bd6a0056");
    private final UUID teacherId1 = UUID.fromString("2aeb9120-1584-4f84-bc30-e6751650fcf8");
    private final UUID teacherId2 = UUID.fromString("a8a7e5b9-1a84-483e-bc5c-5587bc2c0517");

    private User student1;
    private User student2;
    private User student3;
    private User student4;
    private User student5;

    private User admin;
    private User teacher1;
    private User teacher2;

    private ResourceGroup resourceGroup1;
    private ResourceGroup resourceGroup2;

    private ResourceGroupPool resourceGroupPool1;

    @BeforeEach
    public void setUp() throws Exception {
        mapper.findAndRegisterModules();

        Field id = AbstractEntity.class.getDeclaredField("id");
        Field version = Updatable.class.getDeclaredField("version");

        /* Example users */

        student1 = new User(studentId1, UUID.randomUUID(), "student1@example.com", "Student1", "First", "Last");
        student2 = new User(studentId2, UUID.randomUUID(), "student2@example.com", "Student2", "First", "Last");
        student3 = new User(studentId3, UUID.randomUUID(), "student3@example.com", "Student3", "First", "Last");
        student4 = new User(studentId4, UUID.randomUUID(), "student4@example.com", "Student4", "First", "Last");
        student5 = new User(studentId5, UUID.randomUUID(), "student5@example.com", "Student5", "First", "Last");

        admin = new User(adminId, UUID.randomUUID(), "admin@example.com", "Admin", "First", "Last");
        teacher1 = new User(teacherId1, UUID.randomUUID(), "teacher1@example.com", "Teacher1", "First", "Last");
        teacher2 = new User(teacherId2, UUID.randomUUID(), "teacher2@example.com", "Teacher2", "First", "Last");

        /* Courses */

        course1 = new Course();
        course1.setName("Course1");
        course1.setDescription("Course1-Description");
        course1.setClusterId(existingClusterId);

        course2 = new Course();
        course2.setName("Course2");
        course2.setDescription("Course2-Description");
        course2.setClusterId(existingClusterId);

        id.setAccessible(true);
        id.set(course1, UUID.randomUUID());
        id.set(course2, UUID.randomUUID());
        id.setAccessible(false);

        /* Teams */

        team1 = new Team("Team1", true, 6, course1);
        team1.setUsers(List.of(student1, student2));

        team2 = new Team("Team2", true, 6, course1);
        team2.setUsers(List.of(student3, student4));

        course1.setTeams(List.of(team1, team2));
        course1.setTeachers(List.of(teacher1));
        course2.setTeachers(List.of(teacher1));

        /* Resource groups */

        resourceGroup1 = new ResourceGroup();
        resourceGroup1.setName("Course-RG1");
        resourceGroup1.setDescription("First resource group for course.");
        resourceGroup1.setMaxRentTime(12);
        resourceGroup1.setStateless(false);

        resourceGroup1.getVms().addAll(List.of(
                VirtualMachine.builder().id(UUID.randomUUID()).build(),
                VirtualMachine.builder().id(UUID.randomUUID()).build(),
                VirtualMachine.builder().id(UUID.randomUUID()).build(),
                VirtualMachine.builder().id(UUID.randomUUID()).build(),
                VirtualMachine.builder().id(UUID.randomUUID()).build()
        ));

        resourceGroup2 = new ResourceGroup();
        resourceGroup2.setName("Course-RG2");
        resourceGroup2.setDescription("Second resource group for course.");
        resourceGroup2.setMaxRentTime(12);
        resourceGroup2.setStateless(false);

        resourceGroup2.getVms().addAll(List.of(
                VirtualMachine.builder().id(UUID.randomUUID()).build(),
                VirtualMachine.builder().id(UUID.randomUUID()).build(),
                VirtualMachine.builder().id(UUID.randomUUID()).build(),
                VirtualMachine.builder().id(UUID.randomUUID()).build(),
                VirtualMachine.builder().id(UUID.randomUUID()).build()
        ));

        id.setAccessible(true);
        id.set(resourceGroup1, UUID.randomUUID());
        id.set(resourceGroup2, UUID.randomUUID());
        id.setAccessible(false);

        /* Resource group pools */

        resourceGroupPool1 = new ResourceGroupPool();
        resourceGroupPool1.setName("Course-RGPool1");
        resourceGroupPool1.setMaxRent(12);
        resourceGroupPool1.setGracePeriod(12);

        resourceGroupPool1.setResourceGroups(List.of(resourceGroup2));

        id.setAccessible(true);
        id.set(resourceGroupPool1, UUID.randomUUID());
        id.setAccessible(false);
    }

    /* Test methods */

    /* GetCoursesForStudent method tests */

    @Test
    @WithMockUser(username = "e7a9f27d-3ef9-4d65-b82c-d902acf3bd9c", authorities = "student")
    public void Given_CurrentlyAuthenticatedUserCanBeFoundAndInSomeCourses_When_GetCoursesForStudent_Then_ReturnsListOfFoundCourses() throws Exception {
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);

        when(userRepository.findById(studentId1)).thenReturn(Optional.of(student1));
        when(courseService.getCoursesForStudent(student1, pageable)).thenReturn(List.of(course1, course2));

        MvcResult result = mockMvc.perform(get("/course/student")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        List<CourseDto> listOfDtos = mapper.readValue(json, new TypeReference<>() {
        });

        assertNotNull(listOfDtos);
        assertFalse(listOfDtos.isEmpty());
        assertEquals(2, listOfDtos.size());

        CourseDto firstCourse = listOfDtos.getFirst();
        assertNotNull(firstCourse);
        assertEquals(course1.getId(), UUID.fromString(firstCourse.id()));
        assertEquals(course1.getName(), firstCourse.name());
        assertEquals(course1.getDescription(), firstCourse.description());
        assertEquals(course1.getExternalLink(), firstCourse.externalLink());
        assertEquals(course1.getClusterId(), firstCourse.clusterId());
        assertEquals(course1.getCourseType(), firstCourse.courseType());

        CourseDto secondCourse = listOfDtos.getLast();
        assertNotNull(secondCourse);
        assertEquals(course2.getId(), UUID.fromString(secondCourse.id()));
        assertEquals(course2.getName(), secondCourse.name());
        assertEquals(course2.getDescription(), secondCourse.description());
        assertEquals(course2.getExternalLink(), secondCourse.externalLink());
        assertEquals(course2.getClusterId(), secondCourse.clusterId());
        assertEquals(course2.getCourseType(), secondCourse.courseType());

        verify(userRepository, times(1)).findById(studentId1);
        verify(courseService, times(1)).getCoursesForStudent(student1, pageable);
    }

    @Test
    @WithMockUser(username = "e7a9f27d-3ef9-4d65-b82c-d902acf3bd9c", authorities = "student")
    public void Given_CurrentlyAuthenticatedUserCanBeFoundAndInNoCourses_When_GetCoursesForStudent_Then_ReturnsEmptyListOfCourses() throws Exception {
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);

        when(userRepository.findById(studentId1)).thenReturn(Optional.of(student1));
        when(courseService.getCoursesForStudent(student1, pageable)).thenReturn(List.of());

        mockMvc.perform(get("/course/student")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(userRepository, times(1)).findById(studentId1);
        verify(courseService, times(1)).getCoursesForStudent(student1, pageable);
    }

    @Test
    @WithMockUser(username = "e7a9f27d-3ef9-4d65-b82c-d902acf3bd9c", authorities = "student")
    public void Given_CurrentlyAuthenticatedUserCouldNotBeFoundAndInNoCourses_When_GetCoursesForStudent_Then_Returns404NotFound() throws Exception {
        int page = 0;
        int size = 10;

        when(userRepository.findById(studentId1)).thenReturn(Optional.empty());

        mockMvc.perform(get("/course/student")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(userRepository, times(1)).findById(studentId1);
    }

    /* FindResourcesAvailabilityForResourceGroup method tests */

    @Test
    @WithMockUser(username = "e7a9f27d-3ef9-4d65-b82c-d902acf3bd9c", authorities = "student")
    public void Given_ExisingCourseAndRgIdentifiersArePassedAndIntervalIsNot0_When_FindResourcesAvailabilityForResourceGroup_Then_ReturnsListOfFoundResourcesAvailability() throws Exception {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(4);

        Map<LocalDateTime, Boolean> availability = new HashMap<>();
        availability.put(start, true);
        availability.put(start.plusMinutes(30), true);
        availability.put(start.plusHours(1), true);
        availability.put(start.plusHours(1).plusMinutes(30), true);

        when(courseService.getCourse(course1.getId())).thenReturn(course1);
        when(resourceGroupService.getResourceGroup(resourceGroup1.getId())).thenReturn(resourceGroup1);
        when(reservationService.checkResourceGroupAvailability(eq(resourceGroup1), eq(course1),
                any(Integer.class), eq(start), eq(end))).thenReturn(availability);

        when(userRepository.findById(studentId1)).thenReturn(Optional.of(student1));

        MvcResult result = mockMvc.perform(get("/course/{courseId}/resource-groups/{rgId}/availability",
                        course1.getId(), resourceGroup1.getId())
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        List<ResourcesAvailabilityDto> listOfDtos = mapper.readValue(json, new TypeReference<>() {
        });

        assertNotNull(listOfDtos);
        assertFalse(listOfDtos.isEmpty());
        assertEquals(4, listOfDtos.size());

        for (ResourcesAvailabilityDto availabilityDto : listOfDtos) {
            assertNotNull(availabilityDto);
            assertNotNull(availabilityDto.time());
            assertTrue(availabilityDto.available());
        }

        verify(courseService, times(1)).getCourse(course1.getId());
        verify(resourceGroupService, times(1)).getResourceGroup(resourceGroup1.getId());
        verify(reservationService, times(1)).checkResourceGroupAvailability(eq(resourceGroup1),
                eq(course1), any(Integer.class), eq(start), eq(end));

        verify(userRepository, times(1)).findById(studentId1);
    }

    @Test
    @WithMockUser(username = "e7a9f27d-3ef9-4d65-b82c-d902acf3bd9c", authorities = "student")
    public void Given_ExisingCourseAndRgIdentifiersArePassedAndIntervalIs0_When_FindResourcesAvailabilityForResourceGroup_Then_ReturnsEmptyListOfResourcesAvailability() throws Exception {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(2);

        Map<LocalDateTime, Boolean> availability = new HashMap<>();

        when(courseService.getCourse(course1.getId())).thenReturn(course1);
        when(resourceGroupService.getResourceGroup(resourceGroup1.getId())).thenReturn(resourceGroup1);
        when(reservationService.checkResourceGroupAvailability(eq(resourceGroup1), eq(course1),
                any(Integer.class), eq(start), eq(end))).thenReturn(availability);

        when(userRepository.findById(studentId1)).thenReturn(Optional.of(student1));

        mockMvc.perform(get("/course/{courseId}/resource-groups/{rgId}/availability",
                        course1.getId(), resourceGroup1.getId())
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(courseService, times(1)).getCourse(course1.getId());
        verify(resourceGroupService, times(1)).getResourceGroup(resourceGroup1.getId());
        verify(reservationService, times(1)).checkResourceGroupAvailability(eq(resourceGroup1),
                eq(course1), any(Integer.class), eq(start), eq(end));

        verify(userRepository, times(1)).findById(studentId1);
    }

    @Test
    @WithMockUser(username = "e7a9f27d-3ef9-4d65-b82c-d902acf3bd9c", authorities = "student")
    public void Given_NonExisingCourseIdentifierIsPassed_When_FindResourcesAvailabilityForResourceGroup_Then_Returns404NotFound() throws Exception {
        UUID nonExistentCourseIdentifier = UUID.randomUUID();
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(2);

        when(courseService.getCourse(nonExistentCourseIdentifier))
                .thenThrow(CourseNotFoundException.class);

        mockMvc.perform(get("/course/{courseId}/resource-groups/{rgId}/availability",
                        nonExistentCourseIdentifier, resourceGroup1.getId())
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(courseService, times(1)).getCourse(nonExistentCourseIdentifier);
    }

    @Test
    @WithMockUser(username = "e7a9f27d-3ef9-4d65-b82c-d902acf3bd9c", authorities = "student")
    public void Given_NonExisingResourceGroupIdentifierIsPassed_When_FindResourcesAvailabilityForResourceGroup_Then_Returns404NotFound() throws Exception {
        UUID nonExistentResourceGroupIdentifier = UUID.randomUUID();
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(2);

        when(courseService.getCourse(course1.getId())).thenReturn(course1);
        when(resourceGroupService.getResourceGroup(nonExistentResourceGroupIdentifier))
                .thenThrow(ResourceGroupNotFoundException.class);

        mockMvc.perform(get("/course/{courseId}/resource-groups/{rgId}/availability",
                        course1.getId(), nonExistentResourceGroupIdentifier)
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(courseService, times(1)).getCourse(course1.getId());
        verify(resourceGroupService, times(1)).getResourceGroup(nonExistentResourceGroupIdentifier);
    }

    @Test
    @WithMockUser(username = "e7a9f27d-3ef9-4d65-b82c-d902acf3bd9c", authorities = "student")
    public void Given_CurrentlyAuthenticatedUserCouldNotBeFound_When_FindResourcesAvailabilityForResourceGroup_Then_Returns404NotFound() throws Exception {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(2);

        when(courseService.getCourse(course1.getId())).thenReturn(course1);
        when(resourceGroupService.getResourceGroup(resourceGroup1.getId())).thenReturn(resourceGroup1);

        mockMvc.perform(get("/course/{courseId}/resource-groups/{rgId}/availability",
                        course1.getId(), resourceGroup1.getId())
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(courseService, times(1)).getCourse(course1.getId());
        verify(resourceGroupService, times(1)).getResourceGroup(resourceGroup1.getId());
    }

    @Test
    @WithMockUser(username = "62bddaa4-c9ad-4afc-a0af-3941bd6a0056", authorities = "administrator")
    public void Given_ExisingCourseAndRgIdentifiersArePassedAndIntervalIsNot0AsAdmin_When_FindResourcesAvailabilityForResourceGroup_Then_ReturnsListOfFoundResourcesAvailability() throws Exception {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(4);

        Map<LocalDateTime, Boolean> availability = new HashMap<>();
        availability.put(start, true);
        availability.put(start.plusMinutes(30), true);
        availability.put(start.plusHours(1), true);
        availability.put(start.plusHours(1).plusMinutes(30), true);

        when(courseService.getCourse(course1.getId())).thenReturn(course1);
        when(resourceGroupService.getResourceGroup(resourceGroup1.getId())).thenReturn(resourceGroup1);
        when(reservationService.checkResourceGroupAvailability(eq(resourceGroup1), eq(course1),
                any(Integer.class), eq(start), eq(end))).thenReturn(availability);

        when(userRepository.findById(eq(adminId))).thenReturn(Optional.of(admin));

        MvcResult result = mockMvc.perform(get("/course/{courseId}/resource-groups/{rgId}/availability",
                        course1.getId(), resourceGroup1.getId())
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        List<ResourcesAvailabilityDto> listOfDtos = mapper.readValue(json, new TypeReference<>() {
        });

        assertNotNull(listOfDtos);
        assertFalse(listOfDtos.isEmpty());
        assertEquals(4, listOfDtos.size());

        for (ResourcesAvailabilityDto availabilityDto : listOfDtos) {
            assertNotNull(availabilityDto);
            assertNotNull(availabilityDto.time());
            assertTrue(availabilityDto.available());
        }

        verify(courseService, times(1)).getCourse(course1.getId());
        verify(resourceGroupService, times(1)).getResourceGroup(resourceGroup1.getId());
        verify(reservationService, times(1)).checkResourceGroupAvailability(eq(resourceGroup1),
                eq(course1), any(Integer.class), eq(start), eq(end));

        verify(userRepository, times(1)).findById(adminId);
    }

    @Test
    @WithMockUser(username = "62bddaa4-c9ad-4afc-a0af-3941bd6a0056", authorities = "administrator")
    public void Given_ExisingCourseAndRgIdentifiersArePassedAndIntervalIs0AsAdmin_When_FindResourcesAvailabilityForResourceGroup_Then_ReturnsEmptyListOfResourcesAvailability() throws Exception {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(4);

        Map<LocalDateTime, Boolean> availability = new HashMap<>();

        when(courseService.getCourse(course1.getId())).thenReturn(course1);
        when(resourceGroupService.getResourceGroup(resourceGroup1.getId())).thenReturn(resourceGroup1);
        when(reservationService.checkResourceGroupAvailability(eq(resourceGroup1), eq(course1),
                any(Integer.class), eq(start), eq(end))).thenReturn(availability);

        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));

        mockMvc.perform(get("/course/{courseId}/resource-groups/{rgId}/availability",
                        course1.getId(), resourceGroup1.getId())
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(courseService, times(1)).getCourse(course1.getId());
        verify(resourceGroupService, times(1)).getResourceGroup(resourceGroup1.getId());
        verify(reservationService, times(1)).checkResourceGroupAvailability(eq(resourceGroup1),
                eq(course1), any(Integer.class), eq(start), eq(end));

        verify(userRepository, times(1)).findById(adminId);
    }

    @Test
    @WithMockUser(username = "2aeb9120-1584-4f84-bc30-e6751650fcf8", authorities = "teacher")
    public void Given_ExisingCourseAndRgIdentifiersArePassedAndIntervalIsNot0AsTeacherInCourse_When_FindResourcesAvailabilityForResourceGroup_Then_ReturnsListOfFoundResourcesAvailability() throws Exception {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(4);

        Map<LocalDateTime, Boolean> availability = new HashMap<>();
        availability.put(start, true);
        availability.put(start.plusMinutes(30), true);
        availability.put(start.plusHours(1), true);
        availability.put(start.plusHours(1).plusMinutes(30), true);

        when(courseService.getCourse(course1.getId())).thenReturn(course1);
        when(resourceGroupService.getResourceGroup(resourceGroup1.getId())).thenReturn(resourceGroup1);
        when(reservationService.checkResourceGroupAvailability(eq(resourceGroup1), eq(course1),
                any(Integer.class), eq(start), eq(end))).thenReturn(availability);

        when(userRepository.findById(teacherId1)).thenReturn(Optional.of(teacher1));

        MvcResult result = mockMvc.perform(get("/course/{courseId}/resource-groups/{rgId}/availability",
                        course1.getId(), resourceGroup1.getId())
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        List<ResourcesAvailabilityDto> listOfDtos = mapper.readValue(json, new TypeReference<>() {
        });

        assertNotNull(listOfDtos);
        assertFalse(listOfDtos.isEmpty());
        assertEquals(4, listOfDtos.size());

        for (ResourcesAvailabilityDto availabilityDto : listOfDtos) {
            assertNotNull(availabilityDto);
            assertNotNull(availabilityDto.time());
            assertTrue(availabilityDto.available());
        }

        verify(courseService, times(1)).getCourse(course1.getId());
        verify(resourceGroupService, times(1)).getResourceGroup(resourceGroup1.getId());
        verify(reservationService, times(1)).checkResourceGroupAvailability(eq(resourceGroup1),
                eq(course1), any(Integer.class), eq(start), eq(end));

        verify(userRepository, times(1)).findById(teacherId1);
    }

    @Test
    @WithMockUser(username = "2aeb9120-1584-4f84-bc30-e6751650fcf8", authorities = "teacher")
    public void Given_ExisingCourseAndRgIdentifiersArePassedAndIntervalIs0AsTeacherInCourse_When_FindResourcesAvailabilityForResourceGroup_Then_ReturnsEmptyListOfResourcesAvailability() throws Exception {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(4);

        Map<LocalDateTime, Boolean> availability = new HashMap<>();

        when(courseService.getCourse(course1.getId())).thenReturn(course1);
        when(resourceGroupService.getResourceGroup(resourceGroup1.getId())).thenReturn(resourceGroup1);
        when(reservationService.checkResourceGroupAvailability(eq(resourceGroup1), eq(course1),
                any(Integer.class), eq(start), eq(end))).thenReturn(availability);

        when(userRepository.findById(teacherId1)).thenReturn(Optional.of(teacher1));

        mockMvc.perform(get("/course/{courseId}/resource-groups/{rgId}/availability",
                        course1.getId(), resourceGroup1.getId())
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(courseService, times(1)).getCourse(course1.getId());
        verify(resourceGroupService, times(1)).getResourceGroup(resourceGroup1.getId());
        verify(reservationService, times(1)).checkResourceGroupAvailability(eq(resourceGroup1),
                eq(course1), any(Integer.class), eq(start), eq(end));

        verify(userRepository, times(1)).findById(teacherId1);
    }

    @Test
    @WithMockUser(username = "a8a7e5b9-1a84-483e-bc5c-5587bc2c0517", authorities = "teacher")
    public void Given_ExisingCourseAndRgIdentifiersArePassedAndIntervalIsNot0AsTeacherNotInCourse_When_FindResourcesAvailabilityForResourceGroup_Then_ReturnsListOfFoundResourcesAvailability() throws Exception {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(4);

        Map<LocalDateTime, Boolean> availability = new HashMap<>();
        availability.put(start, true);
        availability.put(start.plusMinutes(30), true);
        availability.put(start.plusHours(1), true);
        availability.put(start.plusHours(1).plusMinutes(30), true);

        when(courseService.getCourse(course1.getId())).thenReturn(course1);
        when(resourceGroupService.getResourceGroup(resourceGroup1.getId())).thenReturn(resourceGroup1);
        when(reservationService.checkResourceGroupAvailability(eq(resourceGroup1), eq(course1),
                any(Integer.class), eq(start), eq(end))).thenReturn(availability);

        when(userRepository.findById(teacherId2)).thenReturn(Optional.of(teacher2));

        mockMvc.perform(get("/course/{courseId}/resource-groups/{rgId}/availability",
                        course1.getId(), resourceGroup1.getId())
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(courseService, times(1)).getCourse(course1.getId());
        verify(resourceGroupService, times(1)).getResourceGroup(resourceGroup1.getId());
        verify(reservationService, times(1)).checkResourceGroupAvailability(eq(resourceGroup1),
                eq(course1), any(Integer.class), eq(start), eq(end));

        verify(userRepository, times(1)).findById(teacherId2);
    }

    @Test
    @WithMockUser(username = "a8a7e5b9-1a84-483e-bc5c-5587bc2c0517", authorities = "teacher")
    public void Given_ExisingCourseAndRgIdentifiersArePassedAndIntervalIs0AsTeacherNotInCourse_When_FindResourcesAvailabilityForResourceGroup_Then_ReturnsEmptyListOfResourcesAvailability() throws Exception {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(4);

        Map<LocalDateTime, Boolean> availability = new HashMap<>();

        when(courseService.getCourse(course1.getId())).thenReturn(course1);
        when(resourceGroupService.getResourceGroup(resourceGroup1.getId())).thenReturn(resourceGroup1);
        when(reservationService.checkResourceGroupAvailability(eq(resourceGroup1), eq(course1),
                any(Integer.class), eq(start), eq(end))).thenReturn(availability);

        when(userRepository.findById(teacherId2)).thenReturn(Optional.of(teacher2));

        mockMvc.perform(get("/course/{courseId}/resource-groups/{rgId}/availability",
                        course1.getId(), resourceGroup1.getId())
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(courseService, times(1)).getCourse(course1.getId());
        verify(resourceGroupService, times(1)).getResourceGroup(resourceGroup1.getId());
        verify(reservationService, times(1)).checkResourceGroupAvailability(eq(resourceGroup1),
                eq(course1), any(Integer.class), eq(start), eq(end));

        verify(userRepository, times(1)).findById(teacherId2);
    }

    @Test
    @WithMockUser(username = "a5bf71c0-9e18-45cc-be57-6dabbb7cadb5", authorities = "student")
    public void Given_ExisingCourseAndRgIdentifiersArePassedAndIntervalIsNot0AsStudentNotInCourse_When_FindResourcesAvailabilityForResourceGroup_Then_ReturnsListOfFoundResourcesAvailability() throws Exception {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(4);

        Map<LocalDateTime, Boolean> availability = new HashMap<>();
        availability.put(start, true);
        availability.put(start.plusMinutes(30), true);
        availability.put(start.plusHours(1), true);
        availability.put(start.plusHours(1).plusMinutes(30), true);

        when(courseService.getCourse(course1.getId())).thenReturn(course1);
        when(resourceGroupService.getResourceGroup(resourceGroup1.getId())).thenReturn(resourceGroup1);
        when(reservationService.checkResourceGroupAvailability(eq(resourceGroup1), eq(course1),
                any(Integer.class), eq(start), eq(end))).thenReturn(availability);

        when(userRepository.findById(studentId5)).thenReturn(Optional.of(student5));

        mockMvc.perform(get("/course/{courseId}/resource-groups/{rgId}/availability",
                        course1.getId(), resourceGroup1.getId())
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(courseService, times(1)).getCourse(course1.getId());
        verify(resourceGroupService, times(1)).getResourceGroup(resourceGroup1.getId());
        verify(reservationService, times(1)).checkResourceGroupAvailability(eq(resourceGroup1),
                eq(course1), any(Integer.class), eq(start), eq(end));

        verify(userRepository, times(1)).findById(studentId5);
    }

    @Test
    @WithMockUser(username = "a5bf71c0-9e18-45cc-be57-6dabbb7cadb5", authorities = "student")
    public void Given_ExisingCourseAndRgIdentifiersArePassedAndIntervalIs0AsStudentNotInCourse_When_FindResourcesAvailabilityForResourceGroup_Then_ReturnsEmptyListOfResourcesAvailability() throws Exception {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(4);

        Map<LocalDateTime, Boolean> availability = new HashMap<>();

        when(courseService.getCourse(course1.getId())).thenReturn(course1);
        when(resourceGroupService.getResourceGroup(resourceGroup1.getId())).thenReturn(resourceGroup1);
        when(reservationService.checkResourceGroupAvailability(eq(resourceGroup1), eq(course1),
                any(Integer.class), eq(start), eq(end))).thenReturn(availability);

        when(userRepository.findById(studentId5)).thenReturn(Optional.of(student5));

        mockMvc.perform(get("/course/{courseId}/resource-groups/{rgId}/availability",
                        course1.getId(), resourceGroup1.getId())
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(courseService, times(1)).getCourse(course1.getId());
        verify(resourceGroupService, times(1)).getResourceGroup(resourceGroup1.getId());
        verify(reservationService, times(1)).checkResourceGroupAvailability(eq(resourceGroup1),
                eq(course1), any(Integer.class), eq(start), eq(end));

        verify(userRepository, times(1)).findById(studentId5);
    }

    /* FindResourcesAvailabilityForResourceGroupPool method tests */

    @Test
    @WithMockUser(username = "e7a9f27d-3ef9-4d65-b82c-d902acf3bd9c", authorities = "student")
    public void Given_ExisingCourseAndRgPoolIdentifiersArePassedAndIntervalIsNot0_When_FindResourcesAvailabilityForResourceGroupPool_Then_ReturnsListOfFoundResourcesAvailability() throws Exception {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(4);

        Map<LocalDateTime, Boolean> availability = new HashMap<>();
        availability.put(start, true);
        availability.put(start.plusMinutes(30), true);
        availability.put(start.plusHours(1), true);
        availability.put(start.plusHours(1).plusMinutes(30), true);

        when(courseService.getCourse(course1.getId())).thenReturn(course1);
        when(resourceGroupPoolService.getResourceGroupPool(resourceGroupPool1.getId())).thenReturn(resourceGroupPool1);
        when(reservationService.checkResourceGroupPoolAvailability(eq(resourceGroupPool1), eq(course1),
                any(Integer.class), eq(start), eq(end))).thenReturn(availability);

        when(userRepository.findById(studentId1)).thenReturn(Optional.of(student1));

        MvcResult result = mockMvc.perform(get("/course/{courseId}/resource-group-pools/{rgPoolId}/availability",
                        course1.getId(), resourceGroupPool1.getId())
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        List<ResourcesAvailabilityDto> listOfDtos = mapper.readValue(json, new TypeReference<>() {
        });

        assertNotNull(listOfDtos);
        assertFalse(listOfDtos.isEmpty());
        assertEquals(4, listOfDtos.size());

        for (ResourcesAvailabilityDto availabilityDto : listOfDtos) {
            assertNotNull(availabilityDto);
            assertNotNull(availabilityDto.time());
            assertTrue(availabilityDto.available());
        }

        verify(courseService, times(1)).getCourse(course1.getId());
        verify(resourceGroupPoolService, times(1)).getResourceGroupPool(resourceGroupPool1.getId());
        verify(reservationService, times(1)).checkResourceGroupPoolAvailability(eq(resourceGroupPool1),
                eq(course1), any(Integer.class), eq(start), eq(end));

        verify(userRepository, times(1)).findById(studentId1);
    }

    @Test
    @WithMockUser(username = "e7a9f27d-3ef9-4d65-b82c-d902acf3bd9c", authorities = "student")
    public void Given_ExisingCourseAndRgIdentifiersArePassedAndIntervalIs0_When_FindResourcesAvailabilityForResourceGroupPool_Then_ReturnsEmptyListOfResourcesAvailability() throws Exception {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(2);

        Map<LocalDateTime, Boolean> availability = new HashMap<>();

        when(courseService.getCourse(course1.getId())).thenReturn(course1);
        when(resourceGroupPoolService.getResourceGroupPool(resourceGroupPool1.getId())).thenReturn(resourceGroupPool1);
        when(reservationService.checkResourceGroupPoolAvailability(eq(resourceGroupPool1), eq(course1),
                any(Integer.class), eq(start), eq(end))).thenReturn(availability);

        when(userRepository.findById(studentId1)).thenReturn(Optional.of(student1));

        mockMvc.perform(get("/course/{courseId}/resource-group-pools/{rgPoolId}/availability",
                        course1.getId(), resourceGroupPool1.getId())
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(courseService, times(1)).getCourse(course1.getId());
        verify(resourceGroupPoolService, times(1)).getResourceGroupPool(resourceGroupPool1.getId());
        verify(reservationService, times(1)).checkResourceGroupPoolAvailability(eq(resourceGroupPool1),
                eq(course1), any(Integer.class), eq(start), eq(end));

        verify(userRepository, times(1)).findById(studentId1);
    }

    @Test
    @WithMockUser(username = "e7a9f27d-3ef9-4d65-b82c-d902acf3bd9c", authorities = "student")
    public void Given_NonExisingCourseIdentifierIsPassed_When_FindResourcesAvailabilityForResourceGroupPool_Then_Returns404NotFound() throws Exception {
        UUID nonExistentCourseIdentifier = UUID.randomUUID();
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(2);

        when(courseService.getCourse(nonExistentCourseIdentifier)).thenThrow(CourseNotFoundException.class);

        mockMvc.perform(get("/course/{courseId}/resource-group-pools/{rgPoolId}/availability",
                        nonExistentCourseIdentifier, resourceGroupPool1.getId())
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(courseService, times(1)).getCourse(nonExistentCourseIdentifier);
    }

    @Test
    @WithMockUser(username = "e7a9f27d-3ef9-4d65-b82c-d902acf3bd9c", authorities = "student")
    public void Given_NonExisingResourceGroupPoolIdentifierIsPassed_When_FindResourcesAvailabilityForResourceGroupPool_Then_Returns404NotFound() throws Exception {
        UUID nonExistentResourceGroupPoolIdentifier = UUID.randomUUID();
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(2);

        when(courseService.getCourse(course1.getId())).thenReturn(course1);
        when(resourceGroupPoolService.getResourceGroupPool(nonExistentResourceGroupPoolIdentifier))
                .thenThrow(ResourceGroupNotFoundException.class);

        mockMvc.perform(get("/course/{courseId}/resource-group-pools/{rgPoolId}/availability",
                        course1.getId(), nonExistentResourceGroupPoolIdentifier)
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(courseService, times(1)).getCourse(course1.getId());
        verify(resourceGroupPoolService, times(1)).getResourceGroupPool(nonExistentResourceGroupPoolIdentifier);
    }

    @Test
    @WithMockUser(username = "e7a9f27d-3ef9-4d65-b82c-d902acf3bd9c", authorities = "student")
    public void Given_CurrentlyAuthenticatedUserCouldNotBeFound_When_FindResourcesAvailabilityForResourceGroupPool_Then_Returns404NotFound() throws Exception {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(2);

        when(courseService.getCourse(course1.getId())).thenReturn(course1);
        when(resourceGroupPoolService.getResourceGroupPool(resourceGroupPool1.getId())).thenReturn(resourceGroupPool1);

        mockMvc.perform(get("/course/{courseId}/resource-group-pools/{rgPoolId}/availability",
                        course1.getId(), resourceGroupPool1.getId())
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(courseService, times(1)).getCourse(course1.getId());
        verify(resourceGroupPoolService, times(1)).getResourceGroupPool(resourceGroupPool1.getId());
    }

    @Test
    @WithMockUser(username = "62bddaa4-c9ad-4afc-a0af-3941bd6a0056", authorities = "administrator")
    public void Given_ExisingCourseAndRgPoolIdentifiersArePassedAndIntervalIsNot0AsAdmin_When_FindResourcesAvailabilityForResourceGroupPool_Then_ReturnsListOfFoundResourcesAvailability() throws Exception {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(4);

        Map<LocalDateTime, Boolean> availability = new HashMap<>();
        availability.put(start, true);
        availability.put(start.plusMinutes(30), true);
        availability.put(start.plusHours(1), true);
        availability.put(start.plusHours(1).plusMinutes(30), true);

        when(courseService.getCourse(course1.getId())).thenReturn(course1);
        when(resourceGroupPoolService.getResourceGroupPool(resourceGroupPool1.getId())).thenReturn(resourceGroupPool1);
        when(reservationService.checkResourceGroupPoolAvailability(eq(resourceGroupPool1), eq(course1),
                any(Integer.class), eq(start), eq(end))).thenReturn(availability);

        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));

        MvcResult result = mockMvc.perform(get("/course/{courseId}/resource-group-pools/{rgPoolId}/availability",
                        course1.getId(), resourceGroupPool1.getId())
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        List<ResourcesAvailabilityDto> listOfDtos = mapper.readValue(json, new TypeReference<>() {
        });

        assertNotNull(listOfDtos);
        assertFalse(listOfDtos.isEmpty());
        assertEquals(4, listOfDtos.size());

        for (ResourcesAvailabilityDto availabilityDto : listOfDtos) {
            assertNotNull(availabilityDto);
            assertNotNull(availabilityDto.time());
            assertTrue(availabilityDto.available());
        }

        verify(courseService, times(1)).getCourse(course1.getId());
        verify(resourceGroupPoolService, times(1)).getResourceGroupPool(resourceGroupPool1.getId());
        verify(reservationService, times(1)).checkResourceGroupPoolAvailability(eq(resourceGroupPool1),
                eq(course1), any(Integer.class), eq(start), eq(end));

        verify(userRepository, times(1)).findById(adminId);
    }

    @Test
    @WithMockUser(username = "62bddaa4-c9ad-4afc-a0af-3941bd6a0056", authorities = "administrator")
    public void Given_ExistingCourseAndRgPoolIdentifiersArePassedAndIntervalIs0AsAdmin_When_FindResourcesAvailabilityForResourceGroupPool_Then_ReturnsEmptyListOfResourcesAvailability() throws Exception {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(4);

        Map<LocalDateTime, Boolean> availability = new HashMap<>();

        when(courseService.getCourse(course1.getId())).thenReturn(course1);
        when(resourceGroupPoolService.getResourceGroupPool(resourceGroupPool1.getId())).thenReturn(resourceGroupPool1);
        when(reservationService.checkResourceGroupPoolAvailability(eq(resourceGroupPool1), eq(course1),
                any(Integer.class), eq(start), eq(end))).thenReturn(availability);

        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));

        mockMvc.perform(get("/course/{courseId}/resource-group-pools/{rgPoolId}/availability",
                        course1.getId(), resourceGroupPool1.getId())
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(courseService, times(1)).getCourse(course1.getId());
        verify(resourceGroupPoolService, times(1)).getResourceGroupPool(resourceGroupPool1.getId());
        verify(reservationService, times(1)).checkResourceGroupPoolAvailability(eq(resourceGroupPool1),
                eq(course1), any(Integer.class), eq(start), eq(end));

        verify(userRepository, times(1)).findById(adminId);
    }

    @Test
    @WithMockUser(username = "2aeb9120-1584-4f84-bc30-e6751650fcf8", authorities = "teacher")
    public void Given_ExisingCourseAndRgPoolIdentifiersArePassedAndIntervalIsNot0AsTeacherInCourse_When_FindResourcesAvailabilityForResourceGroupPool_Then_ReturnsListOfFoundResourcesAvailability() throws Exception {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(4);

        Map<LocalDateTime, Boolean> availability = new HashMap<>();
        availability.put(start, true);
        availability.put(start.plusMinutes(30), true);
        availability.put(start.plusHours(1), true);
        availability.put(start.plusHours(1).plusMinutes(30), true);

        when(courseService.getCourse(course1.getId())).thenReturn(course1);
        when(resourceGroupPoolService.getResourceGroupPool(resourceGroupPool1.getId())).thenReturn(resourceGroupPool1);
        when(reservationService.checkResourceGroupPoolAvailability(eq(resourceGroupPool1), eq(course1),
                any(Integer.class), eq(start), eq(end))).thenReturn(availability);

        when(userRepository.findById(teacherId1)).thenReturn(Optional.of(teacher1));

        MvcResult result = mockMvc.perform(get("/course/{courseId}/resource-group-pools/{rgPoolId}/availability",
                        course1.getId(), resourceGroupPool1.getId())
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        List<ResourcesAvailabilityDto> listOfDtos = mapper.readValue(json, new TypeReference<>() {
        });

        assertNotNull(listOfDtos);
        assertFalse(listOfDtos.isEmpty());
        assertEquals(4, listOfDtos.size());

        for (ResourcesAvailabilityDto availabilityDto : listOfDtos) {
            assertNotNull(availabilityDto);
            assertNotNull(availabilityDto.time());
            assertTrue(availabilityDto.available());
        }

        verify(courseService, times(1)).getCourse(course1.getId());
        verify(resourceGroupPoolService, times(1)).getResourceGroupPool(resourceGroupPool1.getId());
        verify(reservationService, times(1)).checkResourceGroupPoolAvailability(eq(resourceGroupPool1),
                eq(course1), any(Integer.class), eq(start), eq(end));

        verify(userRepository, times(1)).findById(teacherId1);
    }

    @Test
    @WithMockUser(username = "2aeb9120-1584-4f84-bc30-e6751650fcf8", authorities = "teacher")
    public void Given_ExisingCourseAndRgPoolIdentifiersArePassedAndIntervalIs0AsTeacherInCourse_When_FindResourcesAvailabilityForResourceGroupPool_Then_ReturnsEmptyListOfResourcesAvailability() throws Exception {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(4);

        Map<LocalDateTime, Boolean> availability = new HashMap<>();

        when(courseService.getCourse(course1.getId())).thenReturn(course1);
        when(resourceGroupPoolService.getResourceGroupPool(resourceGroupPool1.getId())).thenReturn(resourceGroupPool1);
        when(reservationService.checkResourceGroupPoolAvailability(eq(resourceGroupPool1), eq(course1),
                any(Integer.class), eq(start), eq(end))).thenReturn(availability);

        when(userRepository.findById(teacherId1)).thenReturn(Optional.of(teacher1));

        mockMvc.perform(get("/course/{courseId}/resource-group-pools/{rgPoolId}/availability",
                        course1.getId(), resourceGroupPool1.getId())
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(courseService, times(1)).getCourse(course1.getId());
        verify(resourceGroupPoolService, times(1)).getResourceGroupPool(resourceGroupPool1.getId());
        verify(reservationService, times(1)).checkResourceGroupPoolAvailability(eq(resourceGroupPool1),
                eq(course1), any(Integer.class), eq(start), eq(end));

        verify(userRepository, times(1)).findById(teacherId1);
    }

    @Test
    @WithMockUser(username = "a8a7e5b9-1a84-483e-bc5c-5587bc2c0517", authorities = "teacher")
    public void Given_ExisingCourseAndRgPoolIdentifiersArePassedAndIntervalIsNot0AsTeacherNotInCourse_When_FindResourcesAvailabilityForResourceGroupPool_Then_ReturnsListOfFoundResourcesAvailability() throws Exception {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(4);

        Map<LocalDateTime, Boolean> availability = new HashMap<>();
        availability.put(start, true);
        availability.put(start.plusMinutes(30), true);
        availability.put(start.plusHours(1), true);
        availability.put(start.plusHours(1).plusMinutes(30), true);

        when(courseService.getCourse(course1.getId())).thenReturn(course1);
        when(resourceGroupPoolService.getResourceGroupPool(resourceGroupPool1.getId())).thenReturn(resourceGroupPool1);
        when(reservationService.checkResourceGroupPoolAvailability(eq(resourceGroupPool1), eq(course1),
                any(Integer.class), eq(start), eq(end))).thenReturn(availability);

        when(userRepository.findById(teacherId2)).thenReturn(Optional.of(teacher2));

        mockMvc.perform(get("/course/{courseId}/resource-group-pools/{rgPoolId}/availability",
                        course1.getId(), resourceGroupPool1.getId())
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(courseService, times(1)).getCourse(course1.getId());
        verify(resourceGroupPoolService, times(1)).getResourceGroupPool(resourceGroupPool1.getId());
        verify(reservationService, times(1)).checkResourceGroupPoolAvailability(eq(resourceGroupPool1),
                eq(course1), any(Integer.class), eq(start), eq(end));

        verify(userRepository, times(1)).findById(teacherId2);
    }

    @Test
    @WithMockUser(username = "a8a7e5b9-1a84-483e-bc5c-5587bc2c0517", authorities = "teacher")
    public void Given_ExisingCourseAndRgPoolIdentifiersArePassedAndIntervalIs0AsTeacherNotInCourse_When_FindResourcesAvailabilityForResourceGroupPool_Then_ReturnsEmptyListOfResourcesAvailability() throws Exception {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(4);

        Map<LocalDateTime, Boolean> availability = new HashMap<>();

        when(courseService.getCourse(course1.getId())).thenReturn(course1);
        when(resourceGroupPoolService.getResourceGroupPool(resourceGroupPool1.getId())).thenReturn(resourceGroupPool1);
        when(reservationService.checkResourceGroupPoolAvailability(eq(resourceGroupPool1), eq(course1),
                any(Integer.class), eq(start), eq(end))).thenReturn(availability);

        when(userRepository.findById(teacherId2)).thenReturn(Optional.of(teacher2));

        mockMvc.perform(get("/course/{courseId}/resource-group-pools/{rgPoolId}/availability",
                        course1.getId(), resourceGroupPool1.getId())
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(courseService, times(1)).getCourse(course1.getId());
        verify(resourceGroupPoolService, times(1)).getResourceGroupPool(resourceGroupPool1.getId());
        verify(reservationService, times(1)).checkResourceGroupPoolAvailability(eq(resourceGroupPool1),
                eq(course1), any(Integer.class), eq(start), eq(end));

        verify(userRepository, times(1)).findById(teacherId2);
    }

    @Test
    @WithMockUser(username = "a5bf71c0-9e18-45cc-be57-6dabbb7cadb5", authorities = "student")
    public void Given_ExisingCourseAndRgPoolIdentifiersArePassedAndIntervalIsNot0AsStudentNotInCourse_When_FindResourcesAvailabilityForResourceGroupPool_Then_ReturnsListOfFoundResourcesAvailability() throws Exception {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(4);

        Map<LocalDateTime, Boolean> availability = new HashMap<>();
        availability.put(start, true);
        availability.put(start.plusMinutes(30), true);
        availability.put(start.plusHours(1), true);
        availability.put(start.plusHours(1).plusMinutes(30), true);

        when(courseService.getCourse(course1.getId())).thenReturn(course1);
        when(resourceGroupPoolService.getResourceGroupPool(resourceGroupPool1.getId())).thenReturn(resourceGroupPool1);
        when(reservationService.checkResourceGroupPoolAvailability(eq(resourceGroupPool1), eq(course1),
                any(Integer.class), eq(start), eq(end))).thenReturn(availability);

        when(userRepository.findById(studentId5)).thenReturn(Optional.of(student5));

        mockMvc.perform(get("/course/{courseId}/resource-group-pools/{rgPoolId}/availability",
                        course1.getId(), resourceGroupPool1.getId())
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(courseService, times(1)).getCourse(course1.getId());
        verify(resourceGroupPoolService, times(1)).getResourceGroupPool(resourceGroupPool1.getId());
        verify(reservationService, times(1)).checkResourceGroupPoolAvailability(eq(resourceGroupPool1),
                eq(course1), any(Integer.class), eq(start), eq(end));

        verify(userRepository, times(1)).findById(studentId5);
    }

    @Test
    @WithMockUser(username = "a5bf71c0-9e18-45cc-be57-6dabbb7cadb5", authorities = "student")
    public void Given_ExisingCourseAndRgPoolIdentifiersArePassedAndIntervalIs0AsStudentNotInCourse_When_FindResourcesAvailabilityForResourceGroupPool_Then_ReturnsEmptyListOfResourcesAvailability() throws Exception {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime start = currentTime.plusHours(2);
        LocalDateTime end = currentTime.plusHours(4);

        Map<LocalDateTime, Boolean> availability = new HashMap<>();

        when(courseService.getCourse(course1.getId())).thenReturn(course1);
        when(resourceGroupPoolService.getResourceGroupPool(resourceGroupPool1.getId())).thenReturn(resourceGroupPool1);
        when(reservationService.checkResourceGroupPoolAvailability(eq(resourceGroupPool1), eq(course1),
                any(Integer.class), eq(start), eq(end))).thenReturn(availability);

        when(userRepository.findById(studentId5)).thenReturn(Optional.of(student5));

        mockMvc.perform(get("/course/{courseId}/resource-group-pools/{rgPoolId}/availability",
                        course1.getId(), resourceGroupPool1.getId())
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(courseService, times(1)).getCourse(course1.getId());
        verify(resourceGroupPoolService, times(1)).getResourceGroupPool(resourceGroupPool1.getId());
        verify(reservationService, times(1)).checkResourceGroupPoolAvailability(
                eq(resourceGroupPool1), eq(course1), any(Integer.class), eq(start), eq(end));

        verify(userRepository, times(1)).findById(studentId5);
    }

    @Test
    @WithMockUser(username = "2aeb9120-1584-4f84-bc30-e6751650fcf8", authorities = "teacher")
    public void Given_TeacherInCourse_When_AddStudentToCourse_Then_Success() throws Exception {
        EmailDto emailDto = new EmailDto("newstudent@example.com");

        when(userRepository.findById(teacherId1)).thenReturn(Optional.of(teacher1));
        when(courseService.getCourse(course1.getId())).thenReturn(course1);
        doNothing().when(teamService).addStudentToCourse(course1, "newstudent@example.com");

        mockMvc.perform(post("/course/{courseId}/add-student", course1.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(emailDto)))
                .andExpect(status().isNoContent());

        verify(teamService).addStudentToCourse(course1, "newstudent@example.com");
    }

    @Test
    @WithMockUser(username = "a8a7e5b9-1a84-483e-bc5c-5587bc2c0517", authorities = "teacher")
    public void Given_TeacherNotInCourse_When_AddStudentToCourse_Then_Forbidden() throws Exception {
        EmailDto emailDto = new EmailDto("newstudent@example.com");

        when(userRepository.findById(teacherId2)).thenReturn(Optional.of(teacher2));
        when(courseService.getCourse(course1.getId())).thenReturn(course1);

        mockMvc.perform(post("/course/{courseId}/add-student", course1.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(emailDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "2aeb9120-1584-4f84-bc30-e6751650fcf8", authorities = "teacher")
    public void Given_TeacherInCourse_When_RemoveStudentFromCourse_Then_Success() throws Exception {
        EmailDto emailDto = new EmailDto("student1@example.com");

        when(userRepository.findById(teacherId1)).thenReturn(Optional.of(teacher1));
        when(courseService.getCourse(course1.getId())).thenReturn(course1);
        doNothing().when(teamService).removeStudentFromCourse(course1, "student1@example.com");

        mockMvc.perform(post("/course/{courseId}/remove-student", course1.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(emailDto)))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "2aeb9120-1584-4f84-bc30-e6751650fcf8", authorities = "teacher")
    public void Given_TeacherInCourse_When_AddTeacherToCourse_Then_Success() throws Exception {
        EmailDto emailDto = new EmailDto("newteacher@example.com");

        when(userRepository.findById(teacherId1)).thenReturn(Optional.of(teacher1));
        when(courseService.getCourse(course1.getId())).thenReturn(course1);
        doNothing().when(courseService).addTeacherToCourse(course1, "newteacher@example.com");

        mockMvc.perform(post("/course/{courseId}/add-teacher", course1.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(emailDto)))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "2aeb9120-1584-4f84-bc30-e6751650fcf8", authorities = "teacher")
    public void Given_TeacherSelfAdd_When_AddTeacherToCourse_Then_Forbidden() throws Exception {
        when(userRepository.findById(teacherId1)).thenReturn(Optional.of(teacher1));
        when(courseService.getCourse(course1.getId())).thenReturn(course1);
        EmailDto emailDto = new EmailDto("teacher1@example.com");

        when(userRepository.findById(teacherId1)).thenReturn(Optional.of(teacher1));
        when(courseService.getCourse(course1.getId())).thenReturn(course1);

        mockMvc.perform(post("/course/{courseId}/add-teacher", course1.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(emailDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "e7a9f27d-3ef9-4d65-b82c-d902acf3bd9c", authorities = "student")
    public void Given_StudentInCourse_When_GetTeachersForCourse_Then_Success() throws Exception {
        List<User> teachers = List.of(teacher1);
        when(userRepository.findById(studentId1)).thenReturn(Optional.of(student1));
        when(courseService.getCourse(course1.getId())).thenReturn(course1);
        when(courseService.getTeachersForCourse(course1.getId())).thenReturn(teachers);

        mockMvc.perform(get("/course/{courseId}/teachers", course1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("teacher1@example.com"));
    }

    @Test
    @WithMockUser(username = "2aeb9120-1584-4f84-bc30-e6751650fcf8", authorities = "teacher")
    public void Given_TeacherInCourse_When_GetStudentsInSoloCourse_Then_Success() throws Exception {
        List<User> students = List.of(student1, student2);
        when(userRepository.findById(teacherId1)).thenReturn(Optional.of(teacher1));
        when(courseService.getCourse(course1.getId())).thenReturn(course1);
        when(teamService.getStudentsInSoloCourse(course1)).thenReturn(students);

        mockMvc.perform(get("/course/{courseId}/students", course1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("student1@example.com"))
                .andExpect(jsonPath("$[1].email").value("student2@example.com"));
    }

    @Test
    @WithMockUser(username = "a8a7e5b9-1a84-483e-bc5c-5587bc2c0517", authorities = "teacher")
    public void Given_TeacherNotInCourse_When_GetStudentsInSoloCourse_Then_NoContent() throws Exception {
        when(userRepository.findById(teacherId2)).thenReturn(Optional.of(teacher2));
        when(courseService.getCourse(course1.getId())).thenReturn(course1);

        mockMvc.perform(get("/course/{courseId}/students", course1.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "a8a7e5b9-1a84-483e-bc5c-5587bc2c0517", authorities = "teacher")
    public void Given_TeacherNotInCourse_When_GetCourseStatistics_Then_NoContent() throws Exception {
        when(userRepository.findById(teacherId2)).thenReturn(Optional.of(teacher2));
        when(courseService.getCourse(course1.getId())).thenReturn(course1);

        mockMvc.perform(get("/course/{courseId}/statistics", course1.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "a8a7e5b9-1a84-483e-bc5c-5587bc2c0517", authorities = "teacher")
    public void Given_TeacherNotInCourse_When_GetTeamStatistics_Then_NoContent() throws Exception {
        Team testTeam = Team.builder()
                .name("Test Team")
                .course(course1)
                .maxSize(5)
                .active(true)
                .users(new ArrayList<>())
                .build();
        setEntityId(testTeam, UUID.fromString("55555555-5555-5555-5555-555555555555"));

        when(userRepository.findById(teacherId2)).thenReturn(Optional.of(teacher2));
        when(courseService.getCourse(course1.getId())).thenReturn(course1);
        when(teamService.getTeamById(testTeam.getId())).thenReturn(testTeam);

        mockMvc.perform(get("/course/{courseId}/teams/{teamId}/statistics",
                        course1.getId().toString(),
                        testTeam.getId().toString()))
                .andExpect(status().isNoContent());

        verify(userRepository).findById(teacherId2);
        verify(courseService).getCourse(course1.getId());
    }

    @Test
    @WithMockUser(username = "2aeb9120-1584-4f84-bc30-e6751650fcf8", authorities = "teacher")
    public void Given_TeacherSelfRemove_When_RemoveTeacherFromCourse_Then_Forbidden() throws Exception {
        when(userRepository.findById(teacherId1)).thenReturn(Optional.of(teacher1));
        when(courseService.getCourse(course1.getId())).thenReturn(course1);
        EmailDto emailDto = new EmailDto("teacher1@example.com");

        when(userRepository.findById(teacherId1)).thenReturn(Optional.of(teacher1));
        when(courseService.getCourse(course1.getId())).thenReturn(course1);

        mockMvc.perform(post("/course/{courseId}/remove-teacher", course1.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(emailDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "a8a7e5b9-1a84-483e-bc5c-5587bc2c0517", authorities = "teacher")
    public void Given_TeacherNotInCourse_When_RemoveTeacherFromCourse_Then_Forbidden() throws Exception {
        EmailDto emailDto = new EmailDto("otherteacher@example.com");

        when(userRepository.findById(teacherId2)).thenReturn(Optional.of(teacher2));
        when(courseService.getCourse(course1.getId())).thenReturn(course1);

        mockMvc.perform(post("/course/{courseId}/remove-teacher", course1.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(emailDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "a8a7e5b9-1a84-483e-bc5c-5587bc2c0517", authorities = "teacher")
    public void Given_TeacherNotInCourse_When_AddTeacherToCourse_Then_Forbidden() throws Exception {
        EmailDto emailDto = new EmailDto("newteacher@example.com");

        when(userRepository.findById(teacherId2)).thenReturn(Optional.of(teacher2));
        when(courseService.getCourse(course1.getId())).thenReturn(course1);

        mockMvc.perform(post("/course/{courseId}/add-teacher", course1.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(emailDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "a8a7e5b9-1a84-483e-bc5c-5587bc2c0517", authorities = "teacher")
    public void Given_TeacherNotInCourse_When_RemoveStudentFromCourse_Then_Forbidden() throws Exception {
        EmailDto emailDto = new EmailDto("student@example.com");

        when(userRepository.findById(teacherId2)).thenReturn(Optional.of(teacher2));
        when(courseService.getCourse(course1.getId())).thenReturn(course1);

        mockMvc.perform(post("/course/{courseId}/remove-student", course1.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(emailDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "e7a9f27d-3ef9-4d65-b82c-d902acf3bd9c", authorities = "student")
    public void Given_EmptyTeachersList_When_GetTeachersForCourse_Then_NoContent() throws Exception {
        when(userRepository.findById(studentId1)).thenReturn(Optional.of(student1));
        when(courseService.getCourse(course1.getId())).thenReturn(course1);
        when(courseService.getTeachersForCourse(course1.getId())).thenReturn(List.of());

        mockMvc.perform(get("/course/{courseId}/teachers", course1.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "2aeb9120-1584-4f84-bc30-e6751650fcf8", authorities = "teacher")
    public void Given_TeacherInCourse_When_GetCourseStatistics_Then_Success() throws Exception {
        BaseCourseStatsDto statsDto = new CourseStatsDto(
                course1.getId(),
                course1.getName(),
                10,
                15.0,
                2.5,
                2,
                Map.of("Team1", 5, "Team2", 5),
                Map.of("Team1", 7.5, "Team2", 7.5)
        );

        when(userRepository.findById(teacherId1)).thenReturn(Optional.of(teacher1));
        when(courseService.getCourse(course1.getId())).thenReturn(course1);
        when(reservationStatisticsService.getCourseStatistics(course1.getId())).thenReturn(statsDto);

        mockMvc.perform(get("/course/{courseId}/statistics", course1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalReservations").value(10))
                .andExpect(jsonPath("$.totalHours").value(15.0))
                .andExpect(jsonPath("$.averageLength").value(2.5));
    }

    @Test
    @WithMockUser(username = "2aeb9120-1584-4f84-bc30-e6751650fcf8", authorities = "teacher")
    public void Given_TeacherInCourse_When_GetTeamStatistics_Then_Success() throws Exception {
        UUID teamId = UUID.randomUUID();
        setEntityId(team1, teamId);

        TeamStatsDto statsDto = new TeamStatsDto(
                team1.getId(),
                team1.getName(),
                10,
                15L,
                2.5,
                3,
                2,
                List.of()
        );

        when(userRepository.findById(teacherId1)).thenReturn(Optional.of(teacher1));
        when(courseService.getCourse(course1.getId())).thenReturn(course1);
        when(teamService.getTeamById(team1.getId())).thenReturn(team1);
        when(reservationStatisticsService.getTeamStatistics(course1.getId(), team1.getId()))
                .thenReturn(statsDto);

        mockMvc.perform(get("/course/{courseId}/teams/{teamId}/statistics",
                        course1.getId(), team1.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalReservations").value(10))
                .andExpect(jsonPath("$.totalHours").value(15))
                .andExpect(jsonPath("$.averageLength").value(2.5));
    }

    @Test
    @WithMockUser(username = "2aeb9120-1584-4f84-bc30-e6751650fcf8", authorities = "teacher")
    public void Given_TeacherInCourse_When_RemoveTeacher_Then_Success() throws Exception {
        EmailDto emailDto = new EmailDto("teacher2@example.com");

        when(userRepository.findById(teacherId1)).thenReturn(Optional.of(teacher1));
        when(courseService.getCourse(course1.getId())).thenReturn(course1);
        doNothing().when(courseService).removeTeacherFromCourse(course1, "teacher2@example.com");

        mockMvc.perform(post("/course/{courseId}/remove-teacher", course1.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(emailDto)))
                .andExpect(status().isNoContent());

        verify(courseService).removeTeacherFromCourse(course1, "teacher2@example.com");
    }

    @Test
    @WithMockUser(username = "e7a9f27d-3ef9-4d65-b82c-d902acf3bd9c", authorities = "student")
    public void Given_StudentNotInCourse_When_GetTeachersForCourse_Then_NoContent() throws Exception {
        when(userRepository.findById(studentId1)).thenReturn(Optional.of(student1));
        when(courseService.getCourse(course2.getId())).thenReturn(course2);
        when(courseService.getTeachersForCourse(course2.getId())).thenReturn(List.of(teacher1));

        mockMvc.perform(get("/course/{courseId}/teachers", course2.getId()))
                .andExpect(status().isNoContent());
    }

    /* Helper methods */

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
