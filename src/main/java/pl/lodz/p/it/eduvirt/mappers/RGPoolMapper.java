package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import pl.lodz.p.it.eduvirt.dto.resource_group_pool.CreateRGPoolDto;
import pl.lodz.p.it.eduvirt.dto.resource_group_pool.DetailedResourceGroupPoolDto;
import pl.lodz.p.it.eduvirt.dto.resource_group_pool.ResourceGroupPoolDto;
import pl.lodz.p.it.eduvirt.entity.eduvirt.ResourceGroupPool;

import java.util.List;
import java.util.stream.Stream;

@Mapper(componentModel = "spring", uses = {CourseMapper.class})
public interface RGPoolMapper {
    List<ResourceGroupPoolDto> toRGPoolDtoList(Stream<ResourceGroupPool> rgPools);

    List<DetailedResourceGroupPoolDto> toDetailedRGPoolDtoList(Stream<ResourceGroupPool> rgPools);
    
    DetailedResourceGroupPoolDto toDetailedRGPoolDto(ResourceGroupPool rgPool);

    ResourceGroupPoolDto toRGPoolDto(ResourceGroupPool rgPool);

    ResourceGroupPool toRGPool(CreateRGPoolDto createRGPoolDto);
}
