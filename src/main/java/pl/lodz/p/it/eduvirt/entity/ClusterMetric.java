package pl.lodz.p.it.eduvirt.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
        name = "metric_cluster",
        indexes = @Index(name = "cluster_metric_metric_id_idx", columnList = "metric_id"),
        uniqueConstraints = @UniqueConstraint(name = "cluster_metric_cluster_id_unique",
                columnNames = {"cluster_id", "metric_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class ClusterMetric extends AbstractEntity {

    @NotNull(message = "metrics.validation.null.cluster.id")
    @Column(name = "cluster_id", nullable = false, updatable = false)
    private UUID clusterId;

    @NotNull(message = "metrics.validation.null.metric.id")
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(
            name = "metric_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "cluster_metric_metric_id_fk"),
            nullable = false, updatable = false
    )
    private Metric metric;

    @PositiveOrZero(message = "metrics.validation.value.negative")
    @Max(value = 9223372036854775807L, message = "metrics.validation.value.too.large")
    @Column(name = "metric_value")
    private Double value;

    /* Constructors */

    public ClusterMetric(UUID clusterId,
                         Metric metric,
                         Double value) {
        this.clusterId = clusterId;
        this.metric = metric;
        this.value = value;
    }
}
