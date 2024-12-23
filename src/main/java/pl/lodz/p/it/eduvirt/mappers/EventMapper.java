package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.ovirt.engine.sdk4.types.Event;
import pl.lodz.p.it.eduvirt.dto.EventGeneralDTO;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "id", expression = "java(event.id())")
    @Mapping(target = "message", expression = "java(event.description())")
    @Mapping(target = "severity", expression = "java(event.severity().value())")
    @Mapping(target = "registeredAt", expression = "java(event.time().toString())")
    EventGeneralDTO ovirtEventToGeneralDTO(Event event);
}
