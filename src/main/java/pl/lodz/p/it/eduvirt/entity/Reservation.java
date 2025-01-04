package pl.lodz.p.it.eduvirt.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "reservation",
        indexes = {
                @Index(name = "reservation_rg_id_idx", columnList = "rg_id"),
                @Index(name = "reservation_team_id_idx", columnList = "team_id")
        }
)
@Getter @Setter
@NoArgsConstructor
public class Reservation extends HistoricalData {

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

    /* Constructors */

    public Reservation(ResourceGroup resourceGroup,
                       Team team,
                       LocalDateTime startTime,
                       LocalDateTime endTime,
                       Boolean automaticStartup) {
        this.resourceGroup = resourceGroup;
        this.team = team;
        this.startTime = startTime;
        this.endTime = endTime;
        this.automaticStartup = automaticStartup;
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
