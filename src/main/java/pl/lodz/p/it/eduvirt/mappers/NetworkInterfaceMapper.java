package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import pl.lodz.p.it.eduvirt.dto.resource_group_network.NetworkInterfaceDto;
import pl.lodz.p.it.eduvirt.entity.NetworkInterface;

import java.util.List;
import java.util.stream.Stream;

@Mapper(componentModel = "spring")
public interface NetworkInterfaceMapper {
    NetworkInterfaceDto toDto(NetworkInterface networkInterface);

    List<NetworkInterfaceDto> toDto(Stream<NetworkInterface> networkInterfaces);
}
