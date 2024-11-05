package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import org.ovirt.engine.sdk4.types.DataCenter;
import pl.lodz.p.it.eduvirt.dto.DataCenterDto;

@Mapper(componentModel = "spring")
public interface DataCenterMapper {

    default DataCenterDto ovirtDataCenterToDto(DataCenter dataCenter) {
        return new DataCenterDto(
                dataCenter.id(),
                dataCenter.name(),
                dataCenter.description(),
                dataCenter.comment(),
                dataCenter.status().value(),
                "%s.%s".formatted(dataCenter.version().major(), dataCenter.version().minor())
        );
    }
}
