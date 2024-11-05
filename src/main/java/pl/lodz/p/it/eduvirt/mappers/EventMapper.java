package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import org.ovirt.engine.sdk4.types.Event;
import pl.lodz.p.it.eduvirt.dto.EventGeneralDTO;

@Mapper(componentModel = "spring")
public interface EventMapper {

    default EventGeneralDTO ovirtEventToGeneralDTO(Event event) {
        return new EventGeneralDTO(
                event.id(),
                event.description(),
                event.severity().value(),
                event.time().toString()
        );
    }
}
