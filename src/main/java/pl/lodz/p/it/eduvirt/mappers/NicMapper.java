package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.ovirt.engine.sdk4.types.Nic;
import pl.lodz.p.it.eduvirt.dto.nic.NicDto;

import java.util.List;
import java.util.stream.Stream;

@Mapper(componentModel = "spring")
public interface NicMapper {

    @Mapping(target = "id", expression = "java(nic.id())")
    @Mapping(target = "name", expression = "java(nic.name())")
    @Mapping(target = "profileName", expression = "java(nic.vnicProfile().name())")
    NicDto nicToDto(Nic nic);

    List<NicDto> nicsToDtos(Stream<Nic> nics);
}
