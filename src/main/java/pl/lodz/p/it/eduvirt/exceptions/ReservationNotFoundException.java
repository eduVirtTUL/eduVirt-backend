package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.NotFoundException;
import pl.lodz.p.it.eduvirt.util.I18n;

import java.util.UUID;

public class ReservationNotFoundException extends NotFoundException {

    public ReservationNotFoundException(UUID reservationId) {
        super("Reservation with id %s could not be found".formatted(reservationId), I18n.RESERVATION_NOT_FOUND);
    }
}
