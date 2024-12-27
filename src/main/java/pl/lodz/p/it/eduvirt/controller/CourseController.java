package pl.lodz.p.it.eduvirt.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.dto.resource_group_pool.ResourceGroupPoolDto;
import pl.lodz.p.it.eduvirt.dto.course.CourseDto;
import pl.lodz.p.it.eduvirt.dto.course.CreateCourseDto;
import pl.lodz.p.it.eduvirt.dto.resources.ResourcesAvailabilityDto;
import pl.lodz.p.it.eduvirt.entity.Course;
import pl.lodz.p.it.eduvirt.entity.ResourceGroupPool;
import pl.lodz.p.it.eduvirt.exceptions.ApplicationOperationNotImplementedException;
import pl.lodz.p.it.eduvirt.exceptions.handle.ExceptionResponse;
import pl.lodz.p.it.eduvirt.mappers.CourseMapper;
import pl.lodz.p.it.eduvirt.mappers.RGPoolMapper;
import pl.lodz.p.it.eduvirt.service.*;

import java.time.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
public class CourseController {

    private final ResourceGroupPoolService resourceGroupPoolService;
    private final ReservationService reservationService;
    private final VnicProfilePoolService vnicProfilePoolService;

    private final OVirtVmService vmService;
    private final OVirtClusterService clusterService;
    private final OVirtHostService hostService;

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

    @GetMapping(path = "/{id}/availability")
    public ResponseEntity<List<ResourcesAvailabilityDto>> findCourseResourcesAvailability(
            @PathVariable("id") UUID courseId,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam("timezone") String timeZone) {
        ZoneId clientTimeZone = ZoneId.of(timeZone);
        ZonedDateTime clientStartTime = start.atStartOfDay().atZone(clientTimeZone);
        LocalDateTime utcStart = clientStartTime.withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();

        ZonedDateTime clientEndTime = end.atStartOfDay().atZone(clientTimeZone);
        LocalDateTime utcEnd = clientEndTime.withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();

        Course foundCourse = courseService.getCourse(courseId);

        // TODO: Require metric values for course in order to finish it

//        List<CourseMetric> courseMetrics = courseMetricService.findAllMetricsValuesForCourse(foundCourse);
//
//        int cpuCount = courseMetrics.stream().filter(courseMetric ->
//                courseMetric.getMetric().getName().equals("cpu_count")).getFirst().getValue();
//        long memorySize = courseMetrics.stream().filter(courseMetric ->
//                courseMetric.getMetric().getName().equals("memory_size")).getFirst().getValue();
//        int networkCount = courseMetrics.stream().filter(courseMetric ->
//                courseMetric.getMetric().getName().equals("network_count")).getFirst().getValue();
//
//        int durationHours = (int) Duration.between(utcStart, utcEnd).get(ChronoUnit.HOURS);
//
//        List<ResourcesAvailabilityDto> resourcesAvailabilityDtos = new LinkedList<>();
//
//        UUID clusterId = foundCourse.getClusterId();
//        Cluster cluster = clusterService.findClusterById(clusterId);
//
//        LocalDateTime currentTime = utcStart;
//        while (currentTime.isBefore(utcEnd)) {
//            int requiredCpus = 0;
//            long requiredMemory = 0;
//            int requiredNetworks = 0;
//
//            List<Reservation> currentReservations = reservationService
//                    .findCurrentReservationsForCourse(foundCourse, currentTime);
//
//            for (Reservation reservation : currentReservations) {
//                ResourceGroup resourceGroup = reservation.getResourceGroup();
//                List<VirtualMachine> vms = resourceGroup.getVms();
//                for (VirtualMachine vm : vms) {
//                    Vm oVirtVM = vmService.findVmById(vm.getId().toString());
//                    List<Host> oVirtHosts = clusterService.findAllHostsInCluster(cluster);
//                    Map<String, Object> resources = vmService.findVmResources(oVirtVM, oVirtHosts.getFirst(), cluster);
//
//                    requiredCpus += (int) resources.get("cpu");
//                    requiredMemory += (long) resources.get("memory");
//                }
//                requiredCpus += resourceGroup.getNetworks().size();
//            }
//
//            if (requiredCpus > cpuCount || requiredMemory > memorySize && requiredNetworks > networkCount)
//                resourcesAvailabilityDtos.add(new ResourcesAvailabilityDto(currentTime, false));
//            else resourcesAvailabilityDtos.add(new ResourcesAvailabilityDto(currentTime, true));
//
//            currentTime = currentTime.plusMinutes(15);
//        }

        throw new ApplicationOperationNotImplementedException();
    }
}
