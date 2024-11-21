package pl.lodz.p.it.eduvirt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.dto.pagination.PageDto;
import pl.lodz.p.it.eduvirt.dto.reservation.CreateReservationDto;
import pl.lodz.p.it.eduvirt.dto.reservation.ReservationDetailsDto;
import pl.lodz.p.it.eduvirt.dto.reservation.ReservationDto;
import pl.lodz.p.it.eduvirt.exceptions.ApplicationOperationNotImplementedException;

@RestController
@LoggerInterceptor
@RequiredArgsConstructor
@RequestMapping(path = "/reservations")
public class ReservationController {

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> createNewReservation(@RequestBody CreateReservationDto createDto) {
        throw new ApplicationOperationNotImplementedException();
    }

    @GetMapping(path = "/{reservationId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ReservationDetailsDto> getReservationDetails(@PathVariable("reservationId") String reservationId) {
        throw new ApplicationOperationNotImplementedException();
    }

    @GetMapping(path = "/active", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PageDto<ReservationDto>> getActiveReservations(@RequestParam(name = "pageNumber", defaultValue = "0", required = false) int pageNumber,
                                                                  @RequestParam(name = "pageSize", defaultValue = "10", required = false) int pageSize) {
        throw new ApplicationOperationNotImplementedException();
    }

    @GetMapping(path = "/historic", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PageDto<ReservationDto>> getHistoricReservations(@RequestParam(name = "pageNumber", defaultValue = "0", required = false) int pageNumber,
                                                                    @RequestParam(name = "pageSize", defaultValue = "10", required = false) int pageSize) {
        throw new ApplicationOperationNotImplementedException();
    }

    @PostMapping(path = "/{reservationId}/cancel")
    ResponseEntity<Void> cancelReservation(@PathVariable("reservationId") String reservationId) {
        throw new ApplicationOperationNotImplementedException();
    }
}
