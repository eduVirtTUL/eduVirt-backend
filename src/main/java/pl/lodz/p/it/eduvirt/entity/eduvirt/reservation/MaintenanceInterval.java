package pl.lodz.p.it.eduvirt.entity.eduvirt.reservation;

import jakarta.persistence.*;
import lombok.*;
import pl.lodz.p.it.eduvirt.entity.eduvirt.Updatable;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "i72_administrative_break")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceInterval extends Updatable {

    public enum IntervalType {
        SYSTEM,
        CLUSTER
    }

    @Column(name = "cause", nullable = false, length = 64)
    private String cause;

    @Column(name = "description", length = 256)
    private String description;

    @Column(name = "type", nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    private IntervalType type;

    @Column(name = "cluster_id")
    private UUID clusterId;

    @Column(name = "begin_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime beginAt;

    @Column(name = "end_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime endAt;

    // Constructors

    @Builder
    public MaintenanceInterval(Long version,
                               String cause,
                               String description,
                               LocalDateTime beginAt,
                               LocalDateTime endAt) {
        super(version);
        this.cause = cause;
        this.description = description;
        this.beginAt = beginAt;
        this.endAt = endAt;
    }
}
