package pl.lodz.p.it.eduvirt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.dto.maintenance_interval.CreateMaintenanceIntervalDto;
import pl.lodz.p.it.eduvirt.dto.maintenance_interval.MaintenanceIntervalDetailsDto;
import pl.lodz.p.it.eduvirt.dto.maintenance_interval.MaintenanceIntervalDto;
import pl.lodz.p.it.eduvirt.dto.pagination.PageDto;
import pl.lodz.p.it.eduvirt.dto.pagination.PageInfoDto;
import pl.lodz.p.it.eduvirt.entity.eduvirt.reservation.MaintenanceInterval;
import pl.lodz.p.it.eduvirt.exceptions.maintenance_interval.MaintenanceIntervalNotFound;
import pl.lodz.p.it.eduvirt.mappers.MaintenanceIntervalMapper;
import pl.lodz.p.it.eduvirt.service.MaintenanceIntervalService;

import java.util.UUID;

@RestController
@LoggerInterceptor
@RequiredArgsConstructor
@RequestMapping(path = "/maintenance-intervals")
public class MaintenanceIntervalController {

    private final MaintenanceIntervalService maintenanceIntervalService;

    private final MaintenanceIntervalMapper maintenanceIntervalMapper;

    @PostMapping(path = "/cluster/{clusterId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createNewClusterMaintenanceInterval(@PathVariable("clusterId") UUID clusterId,
                                                                    @RequestBody CreateMaintenanceIntervalDto createDto) {
        maintenanceIntervalService.createClusterMaintenanceInterval(
                clusterId,
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

            if (listOfDtos.items().isEmpty()) return ResponseEntity.noContent().build();
            return ResponseEntity.ok(listOfDtos);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.noContent().build();
        }
    }

    @GetMapping(path = "/{intervalId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MaintenanceIntervalDetailsDto> getMaintenanceInterval(@PathVariable("intervalId") UUID intervalId) {
        MaintenanceInterval foundInterval = maintenanceIntervalService.findMaintenanceInterval(intervalId)
                .orElseThrow(MaintenanceIntervalNotFound::new);

        MaintenanceIntervalDetailsDto outputDto = maintenanceIntervalMapper
                .maintenanceIntervalToDetailsDto(foundInterval);
        return ResponseEntity.ok(outputDto);
    }

    @DeleteMapping(path = "/{intervalId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> cancelMaintenanceInterval(@PathVariable("intervalId") UUID intervalId) {
        maintenanceIntervalService.cancelMaintenanceInterval(intervalId);
        return ResponseEntity.noContent().build();
    }
}
