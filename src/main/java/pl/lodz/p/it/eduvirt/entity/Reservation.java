package pl.lodz.p.it.eduvirt.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.lodz.p.it.eduvirt.executor.entity.mails.MailNotification;
import pl.lodz.p.it.eduvirt.executor.entity.tasks.ExecutorTask;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(
        name = "reservation",
        indexes = {
                @Index(name = "reservation_rg_id_idx", columnList = "rg_id"),
                @Index(name = "reservation_team_id_idx", columnList = "team_id"),
                @Index(name = "reservation_status_idx", columnList = "status"),
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Reservation extends HistoricalData {

    public enum ReservationStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED
    }

    @NotNull(message = "reservations.validation.null.resource.group.id")
    @ManyToOne
    @JoinColumn(
            name = "rg_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "reservation_rg_id_fk"),
            nullable = false, updatable = false
    )
    private ResourceGroup resourceGroup;

    @NotNull(message = "reservations.validation.null.team.id")
    @ManyToOne
    @JoinColumn(
            name = "team_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "reservation_team_id_fk"),
            nullable = false, updatable = false
    )
    private Team team;

    @NotNull(message = "reservations.validation.null.start.time")
    @Column(name = "reservation_start", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime startTime;

    @NotNull(message = "reservations.validation.null.end.time")
    @Column(name = "reservation_end", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime endTime;

    @NotNull(message = "reservations.validation.null.automatic.startup")
    @Column(name = "automatic_startup", nullable = false)
    private Boolean automaticStartup = true;

    @PositiveOrZero(message = "reservations.validation.notification.time.negative")
    @Column(name = "notification_time", nullable = false)
    private int notificationTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReservationStatus status = ReservationStatus.PENDING;

    @OneToMany(mappedBy = "reservation", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ExecutorTask> executorTasks;

    @OneToMany(mappedBy = "reservation", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<MailNotification> mailNotifications;

    /* Constructors */

    public Reservation(ResourceGroup resourceGroup,
                       Team team,
                       LocalDateTime startTime,
                       LocalDateTime endTime,
                       Boolean automaticStartup,
                       int notificationTime) {
        this.resourceGroup = resourceGroup;
        this.team = team;
        this.startTime = startTime;
        this.endTime = endTime;
        this.automaticStartup = automaticStartup;
        this.notificationTime = notificationTime;
    }

    @Builder
    public Reservation(Long version,
                       LocalDateTime startTime,
                       LocalDateTime endTime,
                       Boolean automaticStartup) {
        super(version);
        this.startTime = startTime;
        this.endTime = endTime;
        this.automaticStartup = automaticStartup;
    }
}
