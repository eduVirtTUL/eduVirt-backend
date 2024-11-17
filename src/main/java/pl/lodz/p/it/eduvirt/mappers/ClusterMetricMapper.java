package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.lodz.p.it.eduvirt.dto.metric.MetricValueDto;
import pl.lodz.p.it.eduvirt.entity.eduvirt.reservation.ClusterMetric;

@Mapper(componentModel = "spring")
public interface ClusterMetricMapper {

    @Mapping(target = "id", expression = "java(clusterMetric.getMetric().getId())")
    @Mapping(target = "name", expression = "java(clusterMetric.getMetric().getName())")
    @Mapping(target = "value", expression = "java(clusterMetric.getValue())")
    MetricValueDto clusterMetricToDto(ClusterMetric clusterMetric);
}
