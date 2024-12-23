package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import pl.lodz.p.it.eduvirt.dto.metric.MetricDto;
import pl.lodz.p.it.eduvirt.entity.general.Metric;

@Mapper(componentModel = "spring")
public interface MetricMapper {

    MetricDto metricToDto(Metric metric);
}
