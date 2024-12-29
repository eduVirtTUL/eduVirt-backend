package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import pl.lodz.p.it.eduvirt.dto.pod.CreatePodStatefulDto;
import pl.lodz.p.it.eduvirt.dto.pod.PodStatefulDto;
import pl.lodz.p.it.eduvirt.dto.pod.PodStatelessDto;
import pl.lodz.p.it.eduvirt.entity.PodStateful;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface PodMapper {

    default PodStatefulDto podStatefulToDto(PodStateful pod) {
        return new PodStatefulDto(
            pod.getId(),
            pod.getResourceGroup() != null ? pod.getResourceGroup().getId() : null,
            pod.getTeam() != null ? pod.getTeam().getId() : null,
            pod.getCourse() != null ? pod.getCourse().getId() : null
        );
    }
    
    default PodStatelessDto toStatelessDto(UUID teamId, UUID resourceGroupPoolId) {
        return new PodStatelessDto(teamId, resourceGroupPoolId);
    }

    default PodStateful toPodStatefulEntity(CreatePodStatefulDto dto) {
        return new PodStateful();
    }

    default List<PodStatelessDto> toStatelessDtoList(List<UUID> resourceGroupPoolIds, UUID teamId) {
        return resourceGroupPoolIds.stream()
            .map(poolId -> toStatelessDto(teamId, poolId))
            .toList();
    }
}