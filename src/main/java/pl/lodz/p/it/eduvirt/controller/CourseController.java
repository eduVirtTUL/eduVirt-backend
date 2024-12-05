package pl.lodz.p.it.eduvirt.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.dto.resource_group_pool.ResourceGroupPoolDto;
import pl.lodz.p.it.eduvirt.dto.course.CourseDto;
import pl.lodz.p.it.eduvirt.dto.course.CreateCourseDto;
import pl.lodz.p.it.eduvirt.dto.course.SetCourseKeyDto;
import pl.lodz.p.it.eduvirt.entity.eduvirt.Course;
import pl.lodz.p.it.eduvirt.entity.eduvirt.ResourceGroupPool;
import pl.lodz.p.it.eduvirt.exceptions.handle.ExceptionResponse;
import pl.lodz.p.it.eduvirt.mappers.CourseMapper;
import pl.lodz.p.it.eduvirt.mappers.RGPoolMapper;
import pl.lodz.p.it.eduvirt.service.CourseService;
import pl.lodz.p.it.eduvirt.service.ResourceGroupPoolService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
public class CourseController {
    private final ResourceGroupPoolService resourceGroupPoolService;
    private final CourseService courseService;
    private final CourseMapper courseMapper;
    private final RGPoolMapper rgPoolMapper;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<CourseDto>> getCourses() {
        return ResponseEntity.ok(courseMapper.toCourseDtoList(courseService.getCourses().stream()));
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

    @PatchMapping("/{id}/key")
    public ResponseEntity<CourseDto> setCourseKey(@PathVariable UUID id, @RequestBody SetCourseKeyDto keyDto) {
        Course course = courseService.setCourseKey(id, keyDto.key());
        return ResponseEntity.ok(courseMapper.courseToCourseDto(course));
    }
}
