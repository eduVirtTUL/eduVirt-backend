package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.ovirt.engine.sdk4.types.Event;
import pl.lodz.p.it.eduvirt.dto.EventGeneralDto;

import java.time.ZoneId;

@Mapper(componentModel = "spring", imports = {ZoneId.class})
public interface EventMapper {

    @Mapping(target = "id", expression = "java(event.id())")
    @Mapping(target = "message", expression = "java(event.description())")
    @Mapping(target = "severity", expression = "java(event.severity().name())")
    @Mapping(target = "registeredAt", expression = "java(event.time().toInstant().atZone(ZoneId.of(\"UTC\")).toLocalDateTime())")
    EventGeneralDto ovirtEventToGeneralDTO(Event event);
}
