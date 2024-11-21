package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import pl.lodz.p.it.eduvirt.dto.resource_group.CreateResourceGroupDto;
import pl.lodz.p.it.eduvirt.dto.resource_group.ResourceGroupDto;
import pl.lodz.p.it.eduvirt.entity.eduvirt.ResourceGroup;

import java.util.List;
import java.util.stream.Stream;

@Mapper(componentModel = "spring")
public interface ResourceGroupMapper {
    ResourceGroupDto toDto(ResourceGroup resourceGroup);

    List<ResourceGroupDto> toDtos(Stream<ResourceGroup> resourceGroup);

    ResourceGroup toEntity(CreateResourceGroupDto resourceGroupDto);
}
