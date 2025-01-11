package pl.lodz.p.it.eduvirt.entity;

import jakarta.persistence.*;
import lombok.*;

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
    @ManyToOne(fetch = FetchType.EAGER)
    private Metric metric;

    private double value;
}
