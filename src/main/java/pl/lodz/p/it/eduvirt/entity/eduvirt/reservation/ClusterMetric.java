package pl.lodz.p.it.eduvirt.entity.eduvirt.reservation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.lodz.p.it.eduvirt.entity.eduvirt.general.Metric;
import pl.lodz.p.it.eduvirt.entity.eduvirt.reservation.keys.ClusterMetricKey;

import java.util.UUID;

@Entity
@Table(
        name = "i72_metric_cluster",
        indexes = @Index(name = "cluster_metric_metric_id_idx", columnList = "metric_id"),
        uniqueConstraints = @UniqueConstraint(name = "cluster_metric_cluster_id_unique",
                columnNames = {"cluster_id", "metric_id"})
)
@IdClass(ClusterMetricKey.class)
@Getter @Setter
@NoArgsConstructor
public class ClusterMetric {

    @Id
    @Column(name = "cluster_id", nullable = false, updatable = false)
    private UUID clusterId;

    @Id
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(
            name = "metric_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "cluster_metric_metric_id_fk"),
            nullable = false, updatable = false
    )
    private Metric metric;

    @Column(name = "metric_value")
    private Double value;

    // Constructors

    public ClusterMetric(UUID clusterId,
                         Metric metric,
                         Double value) {
        this.clusterId = clusterId;
        this.metric = metric;
        this.value = value;
    }
}
