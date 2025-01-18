package pl.lodz.p.it.eduvirt.executor.entity.mails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.lodz.p.it.eduvirt.entity.AbstractEntity;
import pl.lodz.p.it.eduvirt.entity.Reservation;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;


@Entity
@Table(
        name = "mail_notification",
        indexes = @Index(name = "mail_notification_reservation_id_idx", columnList = "reservation_id", unique = true)
//        ,
//        uniqueConstraints = @UniqueConstraint(name = "reservation_notification_type_unique", columnNames = {"reservation_id", "type"})
)
@Getter
@NoArgsConstructor
public class MailNotification extends AbstractEntity {

    public enum NotificationType {RESERVATION_START, RESERVATION_END}

    @ManyToOne(optional = false)
    @JoinColumn(
            name = "reservation_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "mail_notification_reservation_id_fk"),
            unique = false, nullable = false, updatable = false
    )
    private Reservation reservation;

    @Column(name = "type", updatable = false, nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Column(name = "_created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;

    /* Constructors */

    public MailNotification(Reservation reservation,
                            NotificationType type) {
        this.reservation = reservation;
        this.type = type;
    }

    /* Static factory methods */

    public static MailNotification reservationStartNotification(Reservation reservation) {
        return new MailNotification(reservation, NotificationType.RESERVATION_START);
    }

    public static MailNotification reservationEndNotification(Reservation reservation) {
        return new MailNotification(reservation, NotificationType.RESERVATION_END);
    }

    /* Other methods */

    @PrePersist
    public void changeCreateData() {
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
    }
}
