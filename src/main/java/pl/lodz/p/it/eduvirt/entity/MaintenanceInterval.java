package pl.lodz.p.it.eduvirt.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "administrative_break")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceInterval extends Updatable {

    public enum IntervalType {
        SYSTEM,
        CLUSTER
    }

    @NotBlank(message = "maintenanceIntervals.validation.null.cause.blank")
    @Size(min = 8, message = "maintenanceIntervals.validation.cause.too.short")
    @Size(max = 128, message = "maintenanceIntervals.validation.cause.too.long")
    @Column(name = "cause", nullable = false, length = 64)
    private String cause;

    @Size(max = 256, message = "maintenanceIntervals.validation.description.too.long")
    @Column(name = "description", length = 256)
    private String description;

    @Column(name = "type", nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    private IntervalType type;

    @Column(name = "cluster_id")
    private UUID clusterId;

    @NotNull(message = "maintenanceIntervals.validation.null.begin.at")
    @Column(name = "begin_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime beginAt;

    @NotNull(message = "maintenanceIntervals.validation.null.end.at")
    @Column(name = "end_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime endAt;

    /* Constructors */

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
