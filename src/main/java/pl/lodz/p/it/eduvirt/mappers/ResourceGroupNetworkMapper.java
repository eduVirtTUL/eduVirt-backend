package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import pl.lodz.p.it.eduvirt.dto.resource_group_network.CreateResourceGroupNetworkDto;
import pl.lodz.p.it.eduvirt.dto.resource_group_network.ResourceGroupNetworkDto;
import pl.lodz.p.it.eduvirt.entity.eduvirt.ResourceGroupNetwork;

import java.util.List;
import java.util.stream.Stream;

@Mapper(componentModel = "spring")
public interface ResourceGroupNetworkMapper {
    ResourceGroupNetworkDto toResourceGroupNetworkDto(ResourceGroupNetwork resourceGroupNetwork);

    List<ResourceGroupNetworkDto> toResourceGroupNetworkDtos(Stream<ResourceGroupNetwork> resourceGroupNetworks);

    ResourceGroupNetwork toResourceGroupNetwork(CreateResourceGroupNetworkDto resourceGroupNetworkDto);
}
