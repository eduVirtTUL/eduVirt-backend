package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.lodz.p.it.eduvirt.dto.metric.GeneralMetricValueDto;
import pl.lodz.p.it.eduvirt.dto.metric.MetricValueDto;
import pl.lodz.p.it.eduvirt.dto.metric.ValueDto;
import pl.lodz.p.it.eduvirt.entity.ClusterMetric;
import pl.lodz.p.it.eduvirt.entity.Metric;

import java.util.UUID;

@Mapper(componentModel = "spring", uses = {MetricMapper.class})
public interface ClusterMetricMapper {

    @Mapping(target = "id", expression = "java(clusterMetric.getMetric().getId())")
    @Mapping(target = "name", expression = "java(clusterMetric.getMetric().getName())")
    @Mapping(target = "category", expression = "java(clusterMetric.getMetric().getCategory())")
    @Mapping(target = "value", expression = "java(clusterMetric.getValue())")
    MetricValueDto clusterMetricToDto(ClusterMetric clusterMetric);

    @Mapping(target = "id", expression = "java(clusterMetric.getId())")
    @Mapping(target = "version", expression = "java(clusterMetric.getVersion())")
    @Mapping(target = "value", expression = "java(clusterMetric.getValue())")
    GeneralMetricValueDto clusterMetricToGeneralDto(ClusterMetric clusterMetric);

    @Mapping(target = "clusterId", source = "clusterId")
    @Mapping(target = "metric", source = "metric")
    @Mapping(target = "version", expression = "java(valueDto.version())")
    @Mapping(target = "value", expression = "java(valueDto.value())")
    ClusterMetric valueDtoToClusterMetric(ValueDto valueDto, UUID clusterId, Metric metric);
}
