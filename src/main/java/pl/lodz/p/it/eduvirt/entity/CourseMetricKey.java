package pl.lodz.p.it.eduvirt.entity;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class CourseMetricKey {
    private Course course;
    private Metric metric;
}
