package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.ovirt.engine.sdk4.types.DataCenter;
import pl.lodz.p.it.eduvirt.dto.DataCenterDto;

@Mapper(componentModel = "spring")
public interface DataCenterMapper {

    @Mapping(target = "status", expression = "java(dataCenter.status().value())")
    @Mapping(target = "compatibilityVersion", expression = "java(\"%s.%s\".formatted(dataCenter.version().major(), dataCenter.version().minor()))")
    DataCenterDto ovirtDataCenterToDto(DataCenter dataCenter);
}
