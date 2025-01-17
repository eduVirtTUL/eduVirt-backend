package pl.lodz.p.it.eduvirt.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "metric")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Metric extends AbstractEntity {

    public enum MetricCategory {
        MEMORY,
        COUNTABLE,
    }

    @Column(name = "name", nullable = false, unique = true, length = 64)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 32)
    private MetricCategory category;
}
