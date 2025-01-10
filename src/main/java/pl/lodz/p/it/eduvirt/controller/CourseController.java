package pl.lodz.p.it.eduvirt.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.dto.resource_group_pool.ResourceGroupPoolDto;
import pl.lodz.p.it.eduvirt.dto.course.CourseDto;
import pl.lodz.p.it.eduvirt.dto.course.CreateCourseDto;
import pl.lodz.p.it.eduvirt.dto.resources.ResourcesAvailabilityDto;
import pl.lodz.p.it.eduvirt.entity.*;
import pl.lodz.p.it.eduvirt.exceptions.handle.ExceptionResponse;
import pl.lodz.p.it.eduvirt.mappers.CourseMapper;
import pl.lodz.p.it.eduvirt.mappers.RGPoolMapper;
import pl.lodz.p.it.eduvirt.service.*;

import java.time.*;
import java.util.*;

@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
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

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<CourseDto>> getCourses() {
        return ResponseEntity.ok(courseMapper.toCourseDtoList(courseService.getCourses().stream()));
    }

    // @PreAuthorize("hasRole('student')")
    @GetMapping(path = "/member", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CourseDto>> getCoursesForStudent(Pageable pageable) {
        UUID studentId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        List<Course> foundCourses = courseService.getCoursesForStudent(studentId, pageable);

        List<CourseDto> listOfDTOs = foundCourses.stream()
                .map(courseMapper::courseToCourseDto).toList();

        if (foundCourses.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(listOfDTOs);
    }

    @GetMapping("/{id}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = CourseDto.class))}),
            @ApiResponse(responseCode = "404", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))})})
    public ResponseEntity<CourseDto> getCourse(@PathVariable UUID id) {
        var course = courseService.getCourse(id);

        return ResponseEntity.ok(courseMapper.courseToCourseDto(course));
    }

    @PostMapping
    public ResponseEntity<CourseDto> addCourse(@RequestBody CreateCourseDto createCourseDto) {
        Course course = courseService.addCourse(courseMapper.courseCreateDtoToCourse(createCourseDto));

        return ResponseEntity.ok(courseMapper.courseToCourseDto(course));
    }

    @GetMapping("/{id}/resource-group-pools")
    public ResponseEntity<List<ResourceGroupPoolDto>> getCourseResourceGroupPools(@PathVariable UUID id) {
        List<ResourceGroupPool> resourceGroupPools = resourceGroupPoolService.getResourceGroupPoolsByCourse(id);
        return ResponseEntity.ok(rgPoolMapper.toRGPoolDtoList(resourceGroupPools.stream()));
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

        if (listOfDTOs.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(listOfDTOs);
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

        if (listOfDTOs.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(listOfDTOs);
    }
}
