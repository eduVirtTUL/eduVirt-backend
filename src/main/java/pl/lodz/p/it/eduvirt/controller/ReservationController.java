package pl.lodz.p.it.eduvirt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.dto.pagination.PageDto;
import pl.lodz.p.it.eduvirt.dto.reservation.CreateReservationDto;
import pl.lodz.p.it.eduvirt.dto.reservation.ReservationDetailsDto;
import pl.lodz.p.it.eduvirt.dto.reservation.ReservationDto;
import pl.lodz.p.it.eduvirt.entity.reservation.Reservation;
import pl.lodz.p.it.eduvirt.exceptions.general.OperationNotImplementedException;
import pl.lodz.p.it.eduvirt.exceptions.ReservationNotFoundException;
import pl.lodz.p.it.eduvirt.mappers.ReservationMapper;
import pl.lodz.p.it.eduvirt.service.ReservationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@LoggerInterceptor
@RequiredArgsConstructor
@RequestMapping(path = "/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    private final ReservationMapper reservationMapper;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> createNewReservation(@RequestBody CreateReservationDto createDto) {
        reservationService.createReservation(createDto.resourceGroupId(), createDto.start(),
                createDto.end(), createDto.automaticStartup());

        return ResponseEntity.noContent().build();
    }

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

    @GetMapping(path = "/active", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PageDto<ReservationDto>> getActiveReservations(@RequestParam(name = "pageNumber", defaultValue = "0", required = false) int pageNumber,
                                                                  @RequestParam(name = "pageSize", defaultValue = "10", required = false) int pageSize) {
        throw new OperationNotImplementedException();
    }

    @GetMapping(path = "/historic", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PageDto<ReservationDto>> getHistoricReservations(@RequestParam(name = "pageNumber", defaultValue = "0", required = false) int pageNumber,
                                                                    @RequestParam(name = "pageSize", defaultValue = "10", required = false) int pageSize) {
        throw new OperationNotImplementedException();
    }

    @PostMapping(path = "/{reservationId}/cancel")
    ResponseEntity<Void> finishReservation(@PathVariable("reservationId") UUID reservationId) {
        Reservation foundReservation = reservationService.findReservationById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        reservationService.finishReservation(foundReservation);

        return ResponseEntity.noContent().build();
    }
}
