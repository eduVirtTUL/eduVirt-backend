package pl.lodz.p.it.eduvirt.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.entity.CourseMetric;
import pl.lodz.p.it.eduvirt.entity.reservation.ClusterMetric;

import java.util.*;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class MetricUtil {

    public Map<String, Object> extractCourseMetricValues(List<CourseMetric> listOfMetrics) {
        return extractMetricValues(
                listOfMetrics,
                courseMetric -> courseMetric.getMetric().getName(),
                CourseMetric::getValue
        );
    }

    public Map<String, Object> extractClusterMetricValues(List<ClusterMetric> listOfMetrics) {
        return extractMetricValues(
                listOfMetrics,
                clusterMetric -> clusterMetric.getMetric().getName(),
                ClusterMetric::getValue
        );
    }

    public <T> Map<String, Object> extractMetricValues(List<T> listOfMetrics, Function<T, String> getMetricName, Function<T, Object> getMetricValue) {
        Map<String, Object> metricValues = new HashMap<>();
        List<String> metricNames = Arrays.asList("cpu_count", "memory_size", "network_count");

        for (String metricName : metricNames) {
            Optional<T> metric = listOfMetrics.stream()
                    .filter(item -> getMetricName.apply(item).equals(metricName))
                    .findFirst();

            metricValues.put(metricName, metric.map(getMetricValue).orElse(0.0));
        }

        return metricValues;
    }
}
