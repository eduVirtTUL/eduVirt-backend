package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.ovirt.engine.sdk4.types.Network;
import pl.lodz.p.it.eduvirt.dto.NetworkDto;

@Mapper(componentModel = "spring")
public interface NetworkMapper {

    @Mapping(target = "id", expression = "java(network.id())")
    @Mapping(target = "name", expression = "java(network.name())")
    @Mapping(target = "description", expression = "java(network.description())")
    @Mapping(target = "comment", expression = "java(network.comment())")
    @Mapping(target = "status", expression = "java(network.status().value())")
    NetworkDto ovirtNetworkToDto(Network network);
}
