package pl.lodz.p.it.eduvirt.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CourseMetricKey {
    private Course course;
    private Metric metric;
}
