package pl.lodz.p.it.eduvirt.entity.eduvirt.reservation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.lodz.p.it.eduvirt.entity.eduvirt.reservation.keys.ReservationLogKey;

import java.util.UUID;

@Entity
@Table(
        name = "i72_reservation_audit_log",
        indexes = @Index(name = "reservation_audit_log_reservation_id_idx", columnList = "reservation_id"),
        uniqueConstraints = @UniqueConstraint(name = "reservation_id_event_id_unique",
                columnNames = {"reservation_id", "event_id"})
)
@IdClass(ReservationLogKey.class)
@Getter @Setter
@NoArgsConstructor
public class ReservationLog {

    @Id
    @ManyToOne
    @JoinColumn(
            name = "reservation_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "reservation_audit_log_reservation_id_fk"),
            nullable = false, updatable = false
    )
    private Reservation reservation;

    @Id
    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;
}
