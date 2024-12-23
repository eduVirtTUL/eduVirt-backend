package pl.lodz.p.it.eduvirt.controller;

import lombok.RequiredArgsConstructor;
import org.ovirt.engine.sdk4.types.Cluster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.dto.maintenance_interval.CreateMaintenanceIntervalDto;
import pl.lodz.p.it.eduvirt.dto.maintenance_interval.MaintenanceIntervalDetailsDto;
import pl.lodz.p.it.eduvirt.dto.maintenance_interval.MaintenanceIntervalDto;
import pl.lodz.p.it.eduvirt.dto.pagination.PageDto;
import pl.lodz.p.it.eduvirt.dto.pagination.PageInfoDto;
import pl.lodz.p.it.eduvirt.entity.reservation.MaintenanceInterval;
import pl.lodz.p.it.eduvirt.exceptions.maintenance_interval.MaintenanceIntervalNotFound;
import pl.lodz.p.it.eduvirt.mappers.MaintenanceIntervalMapper;
import pl.lodz.p.it.eduvirt.service.MaintenanceIntervalService;
import pl.lodz.p.it.eduvirt.service.OVirtClusterService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@LoggerInterceptor
@RequiredArgsConstructor
@RequestMapping(path = "/maintenance-intervals")
public class MaintenanceIntervalController {

    private final OVirtClusterService clusterService;
    private final MaintenanceIntervalService maintenanceIntervalService;

    private final MaintenanceIntervalMapper maintenanceIntervalMapper;

    @PostMapping(path = "/cluster/{clusterId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createNewClusterMaintenanceInterval(@PathVariable("clusterId") UUID clusterId,
                                                                    @RequestBody CreateMaintenanceIntervalDto createDto) {
        Cluster foundCluster = clusterService.findClusterById(clusterId);

        maintenanceIntervalService.createClusterMaintenanceInterval(
                foundCluster,
                createDto.cause(),
                createDto.description(),
                createDto.beginAt(),
                createDto.endAt()
        );

        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/system", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createNewSystemMaintenanceInterval(@RequestBody CreateMaintenanceIntervalDto createDto) {
        maintenanceIntervalService.createSystemMaintenanceInterval(
                createDto.cause(),
                createDto.description(),
                createDto.beginAt(),
                createDto.endAt()
        );

        return ResponseEntity.noContent().build();
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PageDto<MaintenanceIntervalDto>> getAllMaintenanceIntervals(
            @RequestParam(name = "pageNumber", defaultValue = "0", required = false) int pageNumber,
            @RequestParam(name = "pageSize", defaultValue = "10", required = false) int pageSize,
            @RequestParam(name = "clusterId", required = false) UUID clusterId,
            @RequestParam(name = "active", required = false, defaultValue = "true") boolean active) {
        try {
            Page<MaintenanceInterval> maintenanceIntervalPage = maintenanceIntervalService
                    .findAllMaintenanceIntervals(clusterId, active, PageRequest.of(pageNumber, pageSize));

            PageDto<MaintenanceIntervalDto> listOfDtos = new PageDto<>(
                    maintenanceIntervalPage.getContent().stream().map(maintenanceIntervalMapper::maintenanceIntervalToDto).toList(),
                    new PageInfoDto(maintenanceIntervalPage.getNumber(), maintenanceIntervalPage.getNumberOfElements(),
                            maintenanceIntervalPage.getTotalPages(), maintenanceIntervalPage.getTotalElements())
            );

            if (maintenanceIntervalPage.getContent().isEmpty()) return ResponseEntity.noContent().build();
            return ResponseEntity.ok(listOfDtos);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.noContent().build();
        }
    }

    @GetMapping(path = "/time-period")
    public ResponseEntity<List<MaintenanceIntervalDto>> getMaintenanceIntervalsWithinTimePeriod(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<MaintenanceInterval> foundIntervals = maintenanceIntervalService
                .findAllMaintenanceIntervalsInTimePeriod(start, end);

        List<MaintenanceIntervalDto> listOfDtos = foundIntervals.stream()
                .map(maintenanceIntervalMapper::maintenanceIntervalToDto).toList();

        if (foundIntervals.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(listOfDtos);
    }

    @GetMapping(path = "/{intervalId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MaintenanceIntervalDetailsDto> getMaintenanceInterval(@PathVariable("intervalId") UUID intervalId) {
        try {
            MaintenanceInterval foundInterval = maintenanceIntervalService.findMaintenanceInterval(intervalId)
                    .orElseThrow(MaintenanceIntervalNotFound::new);

            MaintenanceIntervalDetailsDto outputDto = maintenanceIntervalMapper
                    .maintenanceIntervalToDetailsDto(foundInterval);

            return ResponseEntity.ok(outputDto);
        } catch (MaintenanceIntervalNotFound exception) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping(path = "/{intervalId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> finishMaintenanceInterval(@PathVariable("intervalId") UUID intervalId) {
        maintenanceIntervalService.finishMaintenanceInterval(intervalId);
        return ResponseEntity.noContent().build();
    }
}
