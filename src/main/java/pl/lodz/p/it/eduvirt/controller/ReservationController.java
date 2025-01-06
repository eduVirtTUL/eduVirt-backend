package pl.lodz.p.it.eduvirt.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.dto.pagination.PageDto;
import pl.lodz.p.it.eduvirt.dto.pagination.PageInfoDto;
import pl.lodz.p.it.eduvirt.dto.reservation.CreateReservationDto;
import pl.lodz.p.it.eduvirt.dto.reservation.ReservationDetailsDto;
import pl.lodz.p.it.eduvirt.dto.reservation.ReservationDto;
import pl.lodz.p.it.eduvirt.entity.Reservation;
import pl.lodz.p.it.eduvirt.exceptions.ReservationNotFoundException;
import pl.lodz.p.it.eduvirt.mappers.ReservationMapper;
import pl.lodz.p.it.eduvirt.service.ReservationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/reservations")
@Transactional(propagation = Propagation.NEVER)
public class ReservationController {

    /* Services */

    private final ReservationService reservationService;

    /* Mappers */

    private final ReservationMapper reservationMapper;

    /* Create methods */

    @PreAuthorize("hasRole('student')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create new reservation", description = "This endpoint can be used to create a new reservation for the team they are a part of.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "New reservation, for given resource group and team, that the current user is a part of was created successfully."),
            @ApiResponse(responseCode = "400", description = "New reservation, for given resource group and team, that the current user is a part of was created successfully."),
            @ApiResponse(responseCode = "500", description = "Some other, unknown error occurred while processing the request.")
    })
    ResponseEntity<Void> createNewReservation(@RequestBody @Validated CreateReservationDto createDto) {
        reservationService.createReservation(createDto.resourceGroupId(), createDto.start(),
                createDto.end(), createDto.automaticStartup());

        return ResponseEntity.noContent().build();
    }

    /* Read methods */

    @PreAuthorize("isAuthenticated()")
    @GetMapping(path = "/{reservationId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ReservationDetailsDto> getReservationDetails(@PathVariable("reservationId") UUID reservationId) {
        try {
            Reservation foundReservation = reservationService.findReservationById(reservationId)
                    .orElseThrow(() -> new ReservationNotFoundException(reservationId));

            return ResponseEntity.ok(reservationMapper.reservationToDetailsDto(foundReservation));
        } catch (ReservationNotFoundException exception) {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(path = "/period/{rgId}")
    ResponseEntity<List<ReservationDto>> getReservationsForGivenPeriodForResourceGroup(
            @PathVariable("rgId") UUID resourceGroupId,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<Reservation> foundReservations = reservationService
                .findReservationsForGivenPeriod(resourceGroupId, start, end);

        List<ReservationDto> listOfDtos = foundReservations.stream().map(reservation -> {
            LocalDateTime currentStartTime = reservation.getStartTime();
            LocalDateTime currentEndTime = reservation.getEndTime();

            return new ReservationDto(
                    reservation.getId(),
                    reservation.getTeam().getId(),
                    currentStartTime,
                    currentEndTime
            );
        }).toList();

        if (listOfDtos.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(listOfDtos);
    }

    @PreAuthorize("hasRole('student')")
    @GetMapping(path = "/active/courses/{courseId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PageDto<ReservationDto>> getActiveReservations(
            @PathVariable("courseId") UUID courseId,
            @RequestParam(name = "pageNumber", defaultValue = "0", required = false) int pageNumber,
            @RequestParam(name = "pageSize", defaultValue = "10", required = false) int pageSize) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Reservation> reservationPage = reservationService.findActiveReservations(userId, courseId, pageable);

        List<ReservationDto> listOfDTOs = reservationPage.getContent().stream()
                .map(reservationMapper::reservationToDto).toList();

        PageDto<ReservationDto> outputDto = new PageDto<>(listOfDTOs,
                new PageInfoDto(reservationPage.getNumber(), reservationPage.getNumberOfElements(),
                        reservationPage.getTotalPages(), reservationPage.getTotalElements()));

        if (listOfDTOs.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(outputDto);
    }

    @PreAuthorize("hasRole('student')")
    @GetMapping(path = "/historic/courses/{courseId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PageDto<ReservationDto>> getHistoricReservations(
            @PathVariable("courseId") UUID courseId,
            @RequestParam(name = "pageNumber", defaultValue = "0", required = false) int pageNumber,
            @RequestParam(name = "pageSize", defaultValue = "10", required = false) int pageSize) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Reservation> reservationPage = reservationService.findHistoricalReservations(userId, courseId, pageable);

        List<ReservationDto> listOfDTOs = reservationPage.getContent().stream()
                .map(reservationMapper::reservationToDto).toList();

        PageDto<ReservationDto> outputDto = new PageDto<>(listOfDTOs,
                new PageInfoDto(reservationPage.getNumber(), reservationPage.getNumberOfElements(),
                        reservationPage.getTotalPages(), reservationPage.getTotalElements()));

        if (listOfDTOs.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(outputDto);
    }

    @PreAuthorize("hasAnyRole('teacher', 'administrator')")
    @GetMapping(path = "/active/teams/{teamId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PageDto<ReservationDto>> getActiveReservationsForTeam(
            @PathVariable("teamId") UUID teamId,
            @RequestParam(name = "pageNumber", defaultValue = "0", required = false) int pageNumber,
            @RequestParam(name = "pageSize", defaultValue = "10", required = false) int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Reservation> reservationPage = reservationService.findActiveReservations(teamId, pageable);

        List<ReservationDto> listOfDTOs = reservationPage.getContent().stream()
                .map(reservationMapper::reservationToDto).toList();

        PageDto<ReservationDto> outputDto = new PageDto<>(listOfDTOs,
                new PageInfoDto(reservationPage.getNumber(), reservationPage.getNumberOfElements(),
                        reservationPage.getTotalPages(), reservationPage.getTotalElements()));

        if (listOfDTOs.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(outputDto);
    }

    @PreAuthorize("hasAnyRole('teacher', 'administrator')")
    @GetMapping(path = "/historic/teams/{teamId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PageDto<ReservationDto>> getHistoricReservationsForTeam(
            @PathVariable("teamId") UUID teamId,
            @RequestParam(name = "pageNumber", defaultValue = "0", required = false) int pageNumber,
            @RequestParam(name = "pageSize", defaultValue = "10", required = false) int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Reservation> reservationPage = reservationService.findHistoricalReservations(teamId, pageable);

        List<ReservationDto> listOfDTOs = reservationPage.getContent().stream()
                .map(reservationMapper::reservationToDto).toList();

        PageDto<ReservationDto> outputDto = new PageDto<>(listOfDTOs,
                new PageInfoDto(reservationPage.getNumber(), reservationPage.getNumberOfElements(),
                        reservationPage.getTotalPages(), reservationPage.getTotalElements()));

        if (listOfDTOs.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(outputDto);
    }

    /* Update / delete methods */

    @PreAuthorize("isAuthenticated()")
    @PostMapping(path = "/{reservationId}/cancel")
    ResponseEntity<Void> finishReservation(@PathVariable("reservationId") UUID reservationId) {
        Reservation foundReservation = reservationService.findReservationById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        reservationService.finishReservation(foundReservation);

        return ResponseEntity.noContent().build();
    }
}
