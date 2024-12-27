package pl.lodz.p.it.eduvirt.entity;

import jakarta.persistence.*;
import lombok.*;
import pl.lodz.p.it.eduvirt.entity.general.Metric;

@Builder
@Entity
@Table(name = "course_metric")
@IdClass(CourseMetricKey.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseMetric {
    @Id
    @ManyToOne
    private Course course;
    @Id
    @ManyToOne
    private Metric metric;

    private double value;
}
