package pl.lodz.p.it.eduvirt.entity.eduvirt.reservation.keys;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.lodz.p.it.eduvirt.entity.eduvirt.reservation.Reservation;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ReservationLogKey {

    private Reservation reservation;
    private UUID eventId;
}
