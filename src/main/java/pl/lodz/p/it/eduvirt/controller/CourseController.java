package pl.lodz.p.it.eduvirt.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.ovirt.engine.sdk4.types.Cluster;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.dto.resource_group_pool.ResourceGroupPoolDto;
import pl.lodz.p.it.eduvirt.dto.course.CourseDto;
import pl.lodz.p.it.eduvirt.dto.course.CreateCourseDto;
import pl.lodz.p.it.eduvirt.dto.course.SetCourseKeyDto;
import pl.lodz.p.it.eduvirt.dto.resources.ResourcesAvailabilityDto;
import pl.lodz.p.it.eduvirt.entity.*;
import pl.lodz.p.it.eduvirt.entity.reservation.ClusterMetric;
import pl.lodz.p.it.eduvirt.entity.reservation.Reservation;
import pl.lodz.p.it.eduvirt.exceptions.handle.ExceptionResponse;
import pl.lodz.p.it.eduvirt.mappers.CourseMapper;
import pl.lodz.p.it.eduvirt.mappers.RGPoolMapper;
import pl.lodz.p.it.eduvirt.service.*;
import pl.lodz.p.it.eduvirt.util.BankerAlgorithm;
import pl.lodz.p.it.eduvirt.util.MetricUtil;

import java.time.*;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
public class CourseController {

    /* Services */

    private final ResourceGroupPoolService resourceGroupPoolService;
    private final ReservationService reservationService;
    private final CourseMetricService courseMetricService;
    private final ClusterMetricService clusterMetricService;

    private final OVirtClusterService clusterService;
    private final CourseService courseService;

    /* Mappers */

    private final CourseMapper courseMapper;
    private final RGPoolMapper rgPoolMapper;

    /* Util */

    private final MetricUtil metricUtil;
    private final BankerAlgorithm bankerAlgorithm;

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

    @GetMapping(path = "/{id}/availability")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResponseEntity<List<ResourcesAvailabilityDto>> findCourseResourcesAvailability(
            @PathVariable("id") UUID courseId,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        Course foundCourse = courseService.getCourse(courseId);
        UUID clusterId = foundCourse.getClusterId();
        Cluster cluster = clusterService.findClusterById(clusterId);

        List<CourseMetric> courseMetrics = courseMetricService.getAllCourseMetricsForCourse(foundCourse.getId());
        List<ClusterMetric> clusterMetrics = clusterMetricService.findAllMetricValuesForCluster(cluster);

        List<ResourcesAvailabilityDto> resourcesAvailabilityDtos = new LinkedList<>();

        LocalDateTime currentTime = startTime;
        while (currentTime.isBefore(endTime)) {
            List<Reservation> currentCourseReservations = reservationService
                    .findCurrentReservationsForCourse(foundCourse, currentTime);

            List<Reservation> currentClusterReservations = reservationService
                    .findCurrentReservationsForCluster(clusterId, currentTime);

            if (bankerAlgorithm.process(() -> metricUtil.extractCourseMetricValues(courseMetrics), currentCourseReservations, cluster) &&
                    bankerAlgorithm.process(() -> metricUtil.extractClusterMetricValues(clusterMetrics), currentClusterReservations, cluster))
                resourcesAvailabilityDtos.add(new ResourcesAvailabilityDto(currentTime, true));
            else resourcesAvailabilityDtos.add(new ResourcesAvailabilityDto(currentTime, false));

            currentTime = currentTime.plusMinutes(30);
        }

        if (resourcesAvailabilityDtos.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(resourcesAvailabilityDtos);
    }
}
