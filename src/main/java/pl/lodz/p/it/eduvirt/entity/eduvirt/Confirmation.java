package pl.lodz.p.it.eduvirt.entity.eduvirt;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pl.lodz.p.it.eduvirt.entity.eduvirt.reservation.Reservation;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "i72_confirmation",
        indexes = @Index(name = "confirmation_reservation_id_idx", columnList = "reservation_id", unique = true)
)
@Getter
@ToString
@NoArgsConstructor
public class Confirmation extends Updatable {

    //TODO: Consider the idea of replacing own id, with only reservation_id as the PK

    @OneToOne(optional = false)
    @JoinColumn(
            name = "reservation_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "confirmation_reservation_id_fk"),
            unique = true, nullable = false, updatable = false
    )
    private Reservation reservation;

    @Column(name = "num_of_hours_before", nullable = false, updatable = false)
    private Integer numOfHoursBefore;

    @Setter
    @Column(name = "last_confirmation_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime lastConfirmationTime;

    @Setter
    @Column(name = "token", nullable = false, length = 36)
    private String token;

    @Column(name = "confirmation_max", nullable = false, updatable = false)
    private Integer confirmationMax;

    @Setter
    @Column(name = "confirmation_counter", nullable = false)
    private Integer confirmationCounter = 0;

    public Confirmation(Reservation reservation,
                        Integer numOfHoursBefore,
                        LocalDateTime lastConfirmationTime,
                        String token,
                        Integer confirmationMax,
                        Integer confirmationCounter) {
        this.reservation = reservation;
        this.numOfHoursBefore = numOfHoursBefore;
        this.lastConfirmationTime = lastConfirmationTime;
        this.token = token;
        this.confirmationMax = confirmationMax;
        this.confirmationCounter = confirmationCounter;
    }
}
