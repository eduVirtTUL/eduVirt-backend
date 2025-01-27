package pl.lodz.p.it.eduvirt.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.dto.EmailDto;
import pl.lodz.p.it.eduvirt.dto.course.CourseDto;
import pl.lodz.p.it.eduvirt.dto.course.CreateCourseDto;
import pl.lodz.p.it.eduvirt.dto.course.UpdateCourseDto;
import pl.lodz.p.it.eduvirt.dto.pagination.PageDto;
import pl.lodz.p.it.eduvirt.dto.pagination.PageInfoDto;
import pl.lodz.p.it.eduvirt.dto.reservation.ReservationDto;
import pl.lodz.p.it.eduvirt.dto.resource_group.CreateResourceGroupDto;
import pl.lodz.p.it.eduvirt.dto.resource_group.ResourceGroupDto;
import pl.lodz.p.it.eduvirt.dto.resource_group_pool.ResourceGroupPoolDto;
import pl.lodz.p.it.eduvirt.dto.resources.ResourcesAvailabilityDto;
import pl.lodz.p.it.eduvirt.dto.user.UserDto;
import pl.lodz.p.it.eduvirt.entity.*;
import pl.lodz.p.it.eduvirt.exceptions.TeacherSelfModificationException;
import pl.lodz.p.it.eduvirt.exceptions.handle.ExceptionResponse;
import pl.lodz.p.it.eduvirt.exceptions.user.UserNotFoundException;
import pl.lodz.p.it.eduvirt.mappers.CourseMapper;
import pl.lodz.p.it.eduvirt.mappers.RGPoolMapper;
import pl.lodz.p.it.eduvirt.mappers.ReservationMapper;
import pl.lodz.p.it.eduvirt.mappers.ResourceGroupMapper;
import pl.lodz.p.it.eduvirt.mappers.UserMapper;
import pl.lodz.p.it.eduvirt.repository.UserRepository;
import pl.lodz.p.it.eduvirt.service.*;
import pl.lodz.p.it.eduvirt.util.RoleConstants;
import pl.lodz.p.it.eduvirt.util.etag.ETagHelper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
@LoggerInterceptor
public class CourseController {

    @Value("${window.length}")
    private int windowLength;

    @PostConstruct
    public void validateProperty() {
        if (windowLength < 10) windowLength = 10;
        if (windowLength > 60) windowLength = 60;
    }

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
    private final ReservationMapper reservationMapper;

    /* Repositories */

    private final UserRepository userRepository;
    private final ETagHelper etagHelper;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('administrator')")
    public ResponseEntity<PageDto<CourseDto>> getCourses(@RequestParam(name = "page", required = false) final Integer page,
                                                         @RequestParam(name = "size", required = false) final Integer size,
                                                         @RequestParam(name = "search", required = false) final String search,
                                                         @RequestParam(name = "sort", required = false, defaultValue = "ASC") String sortOrder) {

        if (!(sortOrder.equals("ASC") || sortOrder.equals("DESC"))) {
            sortOrder = "ASC";
        }

        if (page == null || size == null) {
            List<Course> courses = courseService.getCourses();

            return ResponseEntity.ok(PageDto.<CourseDto>builder()
                    .items(courseMapper.toCourseDtoList(courses.stream()))
                    .page(new PageInfoDto(0, courses.size(), 1, courses.size()))
                    .build());
        }


        Page<Course> courses = courseService.getCourses(page, size, search, sortOrder);

        return ResponseEntity.ok(PageDto.<CourseDto>builder()
                .items(courseMapper.toCourseDtoList(courses.getContent().stream()))
                .page(new PageInfoDto(courses.getNumber(), courses.getNumberOfElements(), courses.getTotalPages(), courses.getTotalElements()))
                .build());
    }

    @GetMapping(path = "/teacher", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('teacher')")
    public ResponseEntity<PageDto<CourseDto>> getCoursesForTeacher(@RequestParam(name = "page", required = false) Integer page,
                                                                   @RequestParam(name = "size", required = false) Integer size,
                                                                   @RequestParam(name = "search", required = false) String search) {

        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());

        if (page == null || size == null) {
            List<Course> courses = courseService.getCourses(userId);

            return ResponseEntity.ok(PageDto.<CourseDto>builder()
                    .items(courseMapper.toCourseDtoList(courses.stream()))
                    .page(new PageInfoDto(0, courses.size(), 1, courses.size()))
                    .build());
        }

        Page<Course> courses = courseService.getCoursesForTeacher(userId, page, size, search);

        return ResponseEntity.ok(PageDto.<CourseDto>builder()
                .items(courseMapper.toCourseDtoList(courses.getContent().stream()))
                .page(new PageInfoDto(courses.getNumber(), courses.getNumberOfElements(), courses.getTotalPages(), courses.getTotalElements()))
                .build());
    }

    @PreAuthorize("hasAuthority('student')")
    @GetMapping(path = "/student", produces = MediaType.APPLICATION_JSON_VALUE)
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
    @PreAuthorize("hasAnyAuthority('teacher', 'administrator')")
    public ResponseEntity<CourseDto> getCourse(@PathVariable UUID id) {
        Course course = courseService.getCourse(id);

        String etag = etagHelper.generateEtag(course);

        return ResponseEntity.ok().eTag(etag).body(courseMapper.courseToCourseDto(course));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('administrator')")
    public ResponseEntity<CourseDto> createCourse(@Valid @RequestBody CreateCourseDto createCourseDto) {
        Course course = courseMapper.courseCreateDtoToCourse(createCourseDto);
        Course savedCourse = courseService.addCourse(course, createCourseDto.teacherEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(courseMapper.courseToCourseDto(savedCourse));
    }

    @GetMapping("/{id}/stateful")
    @Transactional
    public ResponseEntity<List<ResourceGroupDto>> getCourseStatefulResourceGroups(@PathVariable UUID id) {
        List<ResourceGroup> resourceGroups = courseService.getStateFullResourceGroups(id);
        return ResponseEntity.ok(resourceGroupMapper.toDtos(resourceGroups.stream()));
    }

    @GetMapping("/{id}/resource-group-pools")
    @Transactional
    @PreAuthorize("hasAnyAuthority('administrator', 'teacher')")
    public ResponseEntity<List<ResourceGroupPoolDto>> getCourseResourceGroupPools(@PathVariable UUID id) {
        List<ResourceGroupPool> resourceGroupPools = resourceGroupPoolService.getResourceGroupPoolsByCourse(id);
        return ResponseEntity.ok(rgPoolMapper.toRGPoolDtoList(resourceGroupPools.stream()));
    }

    @PostMapping("/{id}/resource-group")
    @PreAuthorize("hasAuthority('teacher')")
    public ResponseEntity<Void> createResourceGroup(@PathVariable UUID id, @RequestBody @Validated CreateResourceGroupDto createResourceGroupDto) {
        ResourceGroup resourceGroup = resourceGroupMapper.toEntity(createResourceGroupDto);
        courseService.addResourceGroupToCourse(id, resourceGroup);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('administrator')")
    public ResponseEntity<Void> deleteCourse(@PathVariable UUID id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('administrator', 'teacher')")
    public ResponseEntity<CourseDto> updateCourse(@PathVariable UUID id,
                                                  @RequestBody @Validated UpdateCourseDto updateCourceDto,
                                                  @RequestHeader(HttpHeaders.IF_MATCH) String ifMatch) {
        Course course = courseMapper.toEntity(updateCourceDto);
        course = courseService.updateCourse(id, course, ifMatch);
        return ResponseEntity.ok(courseMapper.courseToCourseDto(course));
    }

    @Operation(
            method = "GET", summary = "Check availability of certain resource group in given course during specified time window",
            description = """
                    This endpoint can be used to establish availability of certain resource group in given course during
                    specified time window. That endpoint is specifically used by the calendar component in the UI.""",
            parameters = {
                    @Parameter(name = "id", in = ParameterIn.PATH, description = "Identifier of the course, which contains the resource group, which availability is to be established.", required = true),
                    @Parameter(name = "rgId", in = ParameterIn.PATH, description = "Identifier of the resource group, which availability is to be established by the web application.", required = true),
                    @Parameter(name = "start", in = ParameterIn.QUERY, description = "Start of the time window, which the availability will be established for.", required = true),
                    @Parameter(name = "end", in = ParameterIn.QUERY, description = "End of the time window, which the availability will be established for.", required = true),
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Availability of given resource group from given course was established for certain timestamp, each time window apart from each other."),
                    @ApiResponse(responseCode = "204", description = "Specified time window length is shorter than the required minimum of window length.",
                            content = @Content(schema = @Schema())),
                    @ApiResponse(responseCode = "404", description = "Course identified with given identifier or resource group could not be found, or currently authenticated user did not have privileges to access it.",
                            content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Some other, unknown error occurred while processing the request.",
                            content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @PreAuthorize("isAuthenticated()")
    @GetMapping(path = "/{id}/resource-groups/{rgId}/availability")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResponseEntity<List<ResourcesAvailabilityDto>> findResourcesAvailabilityForResourceGroup(
            @PathVariable("id") UUID courseId, @PathVariable("rgId") UUID rgId,
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

        if ((authorities.contains(RoleConstants.ADMINISTRATOR) ||
                (authorities.contains(RoleConstants.TEACHER) && course.getTeachers().contains(user)) ||
                (authorities.contains(RoleConstants.STUDENT) && users.contains(user))) &&
                !listOfDTOs.isEmpty()) {
            return ResponseEntity.ok(listOfDTOs);
        }

        return ResponseEntity.noContent().build();
    }

    @Operation(
            method = "GET", summary = "Check availability of certain resource group pool in given course during specified time window",
            description = """
                    This endpoint can be used to establish availability of certain resource group pool in given course during
                    specified time window. That endpoint is specifically used by the calendar component in the UI.""",
            parameters = {
                    @Parameter(name = "id", in = ParameterIn.PATH, description = "Identifier of the course, which contains the resource group pool, which availability is to be established.", required = true),
                    @Parameter(name = "rgId", in = ParameterIn.PATH, description = "Identifier of the resource group pool, which availability is to be established by the web application.", required = true),
                    @Parameter(name = "start", in = ParameterIn.QUERY, description = "Start of the time window, which the availability will be established for.", required = true),
                    @Parameter(name = "end", in = ParameterIn.QUERY, description = "End of the time window, which the availability will be established for.", required = true),
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Availability of given resource group pool from given course was established for certain timestamp, each time window apart from each other."),
                    @ApiResponse(responseCode = "204", description = "Specified time window length is shorter than the required minimum of window length.",
                            content = @Content(schema = @Schema())),
                    @ApiResponse(responseCode = "404", description = "Course identified with given identifier or resource group pool could not be found, or currently authenticated user did not have privileges to access it.",
                            content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Some other, unknown error occurred while processing the request.",
                            content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @PreAuthorize("isAuthenticated()")
    @GetMapping(path = "/{id}/resource-group-pools/{rgPoolId}/availability")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResponseEntity<List<ResourcesAvailabilityDto>> findResourcesAvailabilityForResourceGroupPool(
            @PathVariable("id") UUID courseId, @PathVariable("rgPoolId") UUID rgPoolId,
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

        if ((authorities.contains(RoleConstants.ADMINISTRATOR) ||
                (authorities.contains(RoleConstants.TEACHER) && course.getTeachers().contains(user)) ||
                (authorities.contains(RoleConstants.STUDENT) && users.contains(user))) &&
                !listOfDTOs.isEmpty()) {
            return ResponseEntity.ok(listOfDTOs);
        }

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyAuthority('administrator', 'teacher')")
    @Transactional
    @PostMapping("/{courseId}/add-student")
    public ResponseEntity<Void> addStudentToCourse(
            @PathVariable UUID courseId,
            @RequestBody @Validated EmailDto emailDto) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        Course course = courseService.getCourse(courseId);

        List<String> authorities = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        if (authorities.contains(RoleConstants.ADMINISTRATOR) ||
                (authorities.contains(RoleConstants.TEACHER) && course.getTeachers().contains(user))) {
            teamService.addStudentToCourse(course, emailDto.getEmail());
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @PreAuthorize("hasAnyAuthority('administrator', 'teacher')")
    @Transactional
    @PostMapping("/{courseId}/remove-student")
    public ResponseEntity<Void> removeStudentFromCourse(
            @PathVariable UUID courseId,
            @RequestBody @Validated EmailDto emailDto) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        Course course = courseService.getCourse(courseId);

        List<String> authorities = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        if (authorities.contains(RoleConstants.ADMINISTRATOR) ||
                (authorities.contains(RoleConstants.TEACHER) && course.getTeachers().contains(user))) {
            teamService.removeStudentFromCourse(course, emailDto.getEmail());
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @PreAuthorize("hasAnyAuthority('administrator', 'teacher')")
    @Transactional
    @PostMapping("/{courseId}/add-teacher")
    public ResponseEntity<Void> addTeacherToCourse(
            @PathVariable UUID courseId,
            @RequestBody @Validated EmailDto emailDto) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        if (user.getEmail().equals(emailDto.getEmail())) {
            throw new TeacherSelfModificationException("Teacher cannot add themselves to a course");
        }
        
        Course course = courseService.getCourse(courseId);

        List<String> authorities = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        if (authorities.contains(RoleConstants.ADMINISTRATOR) ||
                (authorities.contains(RoleConstants.TEACHER) && course.getTeachers().contains(user))) {
            courseService.addTeacherToCourse(course, emailDto.getEmail());
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @PreAuthorize("hasAnyAuthority('administrator', 'teacher')")
    @Transactional
    @PostMapping("/{courseId}/remove-teacher")
    public ResponseEntity<Void> removeTeacherFromCourse(
            @PathVariable UUID courseId,
            @RequestBody @Validated EmailDto emailDto) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        if (user.getEmail().equals(emailDto.getEmail())) {
            throw new TeacherSelfModificationException("Teacher cannot remove themselves from a course");
        }

        Course course = courseService.getCourse(courseId);

        List<String> authorities = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        if (authorities.contains(RoleConstants.ADMINISTRATOR) ||
                (authorities.contains(RoleConstants.TEACHER) && course.getTeachers().contains(user))) {
            courseService.removeTeacherFromCourse(course, emailDto.getEmail());
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    @GetMapping("/{courseId}/teachers")
    public ResponseEntity<List<UserDto>> getTeachersForCourse(@PathVariable UUID courseId) {
        List<User> teachers = courseService.getTeachersForCourse(courseId);
        List<UserDto> teacherDtos = teachers.stream()
                .map(userMapper::userToDto)
                .toList();

        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        Course course = courseService.getCourse(courseId);
        List<String> authorities = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        boolean isStudentInCourse = course.getTeams().stream()
                .flatMap(team -> team.getUsers().stream())
                .anyMatch(student -> student.equals(user));

        if (authorities.contains(RoleConstants.ADMINISTRATOR) ||
                (authorities.contains(RoleConstants.TEACHER) && course.getTeachers().contains(user)) ||
                (authorities.contains(RoleConstants.STUDENT) && isStudentInCourse)) {

            return teacherDtos.isEmpty() ?
                    ResponseEntity.noContent().build() :
                    ResponseEntity.ok(teacherDtos);
        }

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyAuthority('administrator', 'teacher')")
    @Transactional
    @GetMapping("/{courseId}/students")
    public ResponseEntity<List<UserDto>> getStudentsInSoloCourse(@PathVariable UUID courseId) {
        Course course = courseService.getCourse(courseId);
        List<User> students = teamService.getStudentsInSoloCourse(course);
        List<UserDto> listOfDTOs = students.stream()
                .map(userMapper::userToDto)
                .toList();

        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        List<String> authorities = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        if (authorities.contains(RoleConstants.ADMINISTRATOR) ||
                (authorities.contains(RoleConstants.TEACHER) && course.getTeachers().contains(user))) {
            return ResponseEntity.ok(listOfDTOs);
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{courseId}/reset")
    @PreAuthorize("hasAuthority('administrator')")
    public ResponseEntity<Void> resetCourse(@PathVariable UUID courseId) {
        courseService.resetCourse(courseId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('teacher')")
    @GetMapping(path = "/{courseId}/reservations", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    ResponseEntity<PageDto<ReservationDto>> getAllOngoingCourseReservations(
            @PathVariable("courseId") UUID courseId, @PageableDefault Pageable pageable) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());

        Course course = courseService.getCourse(courseId);

        boolean checkIsAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .anyMatch(grantedAuthority ->
                        grantedAuthority.getAuthority().equals(RoleConstants.ADMINISTRATOR));

        // Bypass for administrators
        if (!checkIsAdmin) {
            // Check if teacher is in this course
            course.getTeachers()
                    .stream()
                    .filter(u -> u.getId().equals(userId))
                    .findAny()
                    .orElseThrow(() -> new AccessDeniedException("Currently logged in teacher does not participate in the selected course"));
        }

        Page<Reservation> reservationPage = reservationService.findOngoingCourseReservations(course, pageable);

        List<ReservationDto> listOfDTOs = reservationPage.getContent().stream()
                .map(reservationMapper::reservationToDto).toList();

        PageDto<ReservationDto> outputDto = new PageDto<>(listOfDTOs,
                new PageInfoDto(reservationPage.getNumber(), reservationPage.getNumberOfElements(),
                        reservationPage.getTotalPages(), reservationPage.getTotalElements()));

        if (listOfDTOs.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(outputDto);
    }
}
