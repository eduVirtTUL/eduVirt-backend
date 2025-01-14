package pl.lodz.p.it.eduvirt.controller;

import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.dto.course.CourseDto;
import pl.lodz.p.it.eduvirt.dto.course.CreateCourseDto;
import pl.lodz.p.it.eduvirt.dto.course.UpdateCourseDto;
import pl.lodz.p.it.eduvirt.dto.pagination.PageDto;
import pl.lodz.p.it.eduvirt.dto.pagination.PageInfoDto;
import pl.lodz.p.it.eduvirt.dto.resource_group.CreateResourceGroupDto;
import pl.lodz.p.it.eduvirt.dto.resource_group.ResourceGroupDto;
import pl.lodz.p.it.eduvirt.dto.resource_group_pool.ResourceGroupPoolDto;
import pl.lodz.p.it.eduvirt.dto.resources.ResourcesAvailabilityDto;
import pl.lodz.p.it.eduvirt.dto.user.UserDto;
import pl.lodz.p.it.eduvirt.entity.*;
import pl.lodz.p.it.eduvirt.exceptions.UserNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.handle.ExceptionResponse;
import pl.lodz.p.it.eduvirt.mappers.CourseMapper;
import pl.lodz.p.it.eduvirt.mappers.RGPoolMapper;
import pl.lodz.p.it.eduvirt.mappers.ResourceGroupMapper;
import pl.lodz.p.it.eduvirt.mappers.UserMapper;
import pl.lodz.p.it.eduvirt.repository.UserRepository;
import pl.lodz.p.it.eduvirt.service.*;
import pl.lodz.p.it.eduvirt.util.etag.ETagHelper;
import pl.lodz.p.it.eduvirt.util.etag.EtagPayload;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
@LoggerInterceptor
public class CourseController {

    /* Services */

    private final ReservationService reservationService;
    private final ResourceGroupService resourceGroupService;
    private final ResourceGroupPoolService resourceGroupPoolService;
    private final TeamService teamService;
    private final CourseService courseService;

    /* Mappers */

    private final CourseMapper courseMapper;
    private final RGPoolMapper rgPoolMapper;
    private final ResourceGroupMapper resourceGroupMapper;
    private final UserMapper userMapper;

    /* Repositories */

    private final UserRepository userRepository;

    private final ETagHelper etagHelper;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PageDto<CourseDto>> getCourses(@RequestParam(name = "page", required = false) Integer page,
                                                         @RequestParam(name = "size", required = false) Integer size,
                                                         @RequestParam(name = "search", required = false) String search) {


        if (page == null || size == null) {
            List<Course> courses = courseService.getCourses();

            return ResponseEntity.ok(PageDto.<CourseDto>builder()
                    .items(courseMapper.toCourseDtoList(courses.stream()))
                    .page(new PageInfoDto(0, courses.size(), 1, courses.size()))
                    .build());
        }

        Page<Course> courses;

        if (search == null) {
            courses = courseService.getCourses(page, size);
        } else {
            courses = courseService.getCourses(page, size, search);
        }


        return ResponseEntity.ok(PageDto.<CourseDto>builder()
                .items(courseMapper.toCourseDtoList(courses.getContent().stream()))
                .page(new PageInfoDto(courses.getNumber(), courses.getNumberOfElements(), courses.getTotalPages(), courses.getTotalElements()))
                .build());
    }

    // @PreAuthorize("hasRole('student')")
    @GetMapping(path = "/member", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CourseDto>> getCoursesForStudent(Pageable pageable) {
        UUID studentId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        User student = userRepository.findById(studentId).orElseThrow(
                () -> new UserNotFoundException("User with id %s could not be found!".formatted(studentId)));

        List<Course> foundCourses = courseService.getCoursesForStudent(student, pageable);

        List<CourseDto> listOfDTOs = foundCourses.stream()
                .map(courseMapper::courseToCourseDto).toList();

        if (foundCourses.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(listOfDTOs);
    }

    @GetMapping("/{id}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    headers = @Header(name = "Etag", description = "Etag value", schema = @Schema(implementation = String.class)),
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = CourseDto.class))}
            ),
            @ApiResponse(responseCode = "404", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))})})
    public ResponseEntity<CourseDto> getCourse(@PathVariable UUID id) {
        Course course = courseService.getCourse(id);

        String etag = etagHelper.generateEtag(new EtagPayload(course));

        return ResponseEntity.ok().eTag(etag).body(courseMapper.courseToCourseDto(course));
    }

    @PostMapping
    public ResponseEntity<CourseDto> addCourse(@RequestBody @Validated CreateCourseDto createCourseDto) {
        Course course = courseService.addCourse(courseMapper.courseCreateDtoToCourse(createCourseDto));

        return ResponseEntity.ok(courseMapper.courseToCourseDto(course));
    }

    @GetMapping("/{id}/stateful")
    @Transactional
    public ResponseEntity<List<ResourceGroupDto>> getCourseStatefulResourceGroups(@PathVariable UUID id) {
        List<ResourceGroup> resourceGroups = courseService.getStateFullResourceGroups(id);
        return ResponseEntity.ok(resourceGroupMapper.toDtos(resourceGroups.stream()));
    }

    @GetMapping("/{id}/resource-group-pools")
    public ResponseEntity<List<ResourceGroupPoolDto>> getCourseResourceGroupPools(@PathVariable UUID id) {
        List<ResourceGroupPool> resourceGroupPools = resourceGroupPoolService.getResourceGroupPoolsByCourse(id);
        return ResponseEntity.ok(rgPoolMapper.toRGPoolDtoList(resourceGroupPools.stream()));
    }

    @PostMapping("/{id}/resource-group")
    public ResponseEntity<Void> createResourceGroup(@PathVariable UUID id, @RequestBody @Validated CreateResourceGroupDto createResourceGroupDto) {
        ResourceGroup resourceGroup = resourceGroupMapper.toEntity(createResourceGroupDto);
        courseService.addResourceGroupToCourse(id, resourceGroup);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable UUID id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseDto> updateCourse(@PathVariable UUID id,
                                                  @RequestBody @Validated UpdateCourseDto updateCourceDto,
                                                  @RequestHeader(HttpHeaders.IF_MATCH) String ifMatch) {
        Course course = courseMapper.toEntity(updateCourceDto);
        course = courseService.updateCourse(id, course, ifMatch);
        return ResponseEntity.ok(courseMapper.courseToCourseDto(course));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(path = "/{id}/resource-groups/{rgId}/availability")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResponseEntity<List<ResourcesAvailabilityDto>> findResourcesAvailabilityForResourceGroup(
            @PathVariable("id") UUID courseId, @PathVariable("rgId") UUID rgId,
            @RequestParam("window") int windowLength,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        Course course = courseService.getCourse(courseId);
        ResourceGroup resourceGroup = resourceGroupService.getResourceGroup(rgId);

        Map<LocalDateTime, Boolean> availability = reservationService
                .checkResourceGroupAvailability(resourceGroup, course, windowLength, start, end);

        List<ResourcesAvailabilityDto> listOfDTOs = new LinkedList<>();
        for (LocalDateTime localDateTime : availability.keySet()) {
            listOfDTOs.add(new ResourcesAvailabilityDto(localDateTime, availability.get(localDateTime)));
        }

        /* Check authorization */

        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId.toString()));
        List<User> users = course.getTeams().stream().map(Team::getUsers).flatMap(Collection::stream).toList();
        List<String> authorities = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        if ((authorities.contains("administrator") ||
                (authorities.contains("teacher") && true) ||
                (authorities.contains("student") && users.contains(user))) &&
                !listOfDTOs.isEmpty()) {
            return ResponseEntity.ok(listOfDTOs);
        }

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(path = "/{id}/resource-group-pools/{rgPoolId}/availability")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResponseEntity<List<ResourcesAvailabilityDto>> findResourcesAvailabilityForResourceGroupPool(
            @PathVariable("id") UUID courseId, @PathVariable("rgPoolId") UUID rgPoolId,
            @RequestParam("window") int windowLength,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        Course course = courseService.getCourse(courseId);
        ResourceGroupPool resourceGroupPool = resourceGroupPoolService.getResourceGroupPool(rgPoolId);

        Map<LocalDateTime, Boolean> availability = reservationService
                .checkResourceGroupPoolAvailability(resourceGroupPool, course, windowLength, start, end);

        List<ResourcesAvailabilityDto> listOfDTOs = new LinkedList<>();
        for (LocalDateTime localDateTime : availability.keySet()) {
            listOfDTOs.add(new ResourcesAvailabilityDto(localDateTime, availability.get(localDateTime)));
        }

        /* Check authorization */

        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId.toString()));
        List<User> users = course.getTeams().stream().map(Team::getUsers).flatMap(Collection::stream).toList();
        List<String> authorities = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        if ((authorities.contains("administrator") ||
                (authorities.contains("teacher") && true) ||
                (authorities.contains("student") && users.contains(userId))) &&
                !listOfDTOs.isEmpty()) {
            return ResponseEntity.ok(listOfDTOs);
        }

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{courseId}/add-student")
    public ResponseEntity<Void> addStudentToCourse(@PathVariable UUID courseId, @RequestParam String email) {
        teamService.addStudentToCourse(courseId, email);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{courseId}/remove-student")
    public ResponseEntity<Void> removeStudentFromCourse(@PathVariable UUID courseId, @RequestParam String email) {
        teamService.removeStudentFromCourse(courseId, email);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{courseId}/add-teacher")
    public ResponseEntity<Void> addTeacherToCourse(@PathVariable UUID courseId, @RequestParam String email) {
        courseService.addTeacherToCourse(courseId, email);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{courseId}/remove-teacher")
    public ResponseEntity<Void> removeTeacherFromCourse(@PathVariable UUID courseId, @RequestParam String email) {
        courseService.removeTeacherFromCourse(courseId, email);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{courseId}/teachers")
    public ResponseEntity<List<UserDto>> getTeachersForCourse(@PathVariable UUID courseId) {

        List<User> teachers = courseService.getTeachersForCourse(courseId);

        List<UserDto> userDtos = teachers.stream()
                .map(userMapper::userToDto)
                .toList();

        if (userDtos.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(userDtos);
    }

    @GetMapping("/{courseId}/students")
//    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<UserDto>> getStudentsInSoloCourse(@PathVariable UUID courseId) {
        List<User> users = teamService.getStudentsInSoloCourse(courseId);
        List<UserDto> userDtos = users.stream()
                .map(userMapper::userToDto)
                .toList();

        if (userDtos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(userDtos);
    }

    @PostMapping("/{courseId}/reset")
    public ResponseEntity<Void> resetCourse(@PathVariable UUID courseId) {
        courseService.resetCourse(courseId);
        return ResponseEntity.noContent().build();
    }

}
