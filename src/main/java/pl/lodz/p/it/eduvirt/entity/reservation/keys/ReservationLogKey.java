package pl.lodz.p.it.eduvirt.entity.reservation.keys;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.lodz.p.it.eduvirt.entity.reservation.Reservation;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ReservationLogKey {

    private Reservation reservation;
    private UUID eventId;
}
