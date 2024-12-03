package pl.lodz.p.it.eduvirt.entity.eduvirt.reservation;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.lodz.p.it.eduvirt.entity.eduvirt.HistoricalData;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reservation")
@Getter
@Setter
@NoArgsConstructor
public class Reservation extends HistoricalData {

    @Column(name = "rg_id", nullable = false, updatable = false)
    private UUID resourceGroupId;

    @Column(name = "team_id", nullable = false, updatable = false)
    private UUID teamId;

    @Column(name = "reservation_start", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime startTime;

    @Column(name = "reservation_end", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime endTime;

    @Column(name = "automatic_startup", nullable = false)
    private Boolean automaticStartup = true;

    // Constructors

    public Reservation(UUID resourceGroupId,
                       UUID teamId,
                       LocalDateTime startTime,
                       LocalDateTime endTime,
                       Boolean automaticStartup) {
        this.resourceGroupId = resourceGroupId;
        this.teamId = teamId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.automaticStartup = automaticStartup;
    }

    @Builder
    public Reservation(Long version,
                       LocalDateTime startTime,
                       LocalDateTime endTime,
                       Boolean automaticStartup) {
        //super(version);
        this.startTime = startTime;
        this.endTime = endTime;
        this.automaticStartup = automaticStartup;
    }
}
