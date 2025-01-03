package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import pl.lodz.p.it.eduvirt.dto.course.CourseBasicDto;
import pl.lodz.p.it.eduvirt.dto.pod.CreatePodStatefulDto;
import pl.lodz.p.it.eduvirt.dto.pod.PodDetailsDto;
import pl.lodz.p.it.eduvirt.dto.pod.PodStatefulDto;
import pl.lodz.p.it.eduvirt.dto.pod.PodStatelessDto;
import pl.lodz.p.it.eduvirt.dto.resource_group.ResourceGroupDto;
import pl.lodz.p.it.eduvirt.dto.team.TeamDto;
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
                new CourseBasicDto(
                        pod.getCourse().getId(),
                        pod.getCourse().getName(),
                        pod.getCourse().getDescription(),
                        pod.getCourse().getCourseType().name()
                )
        );
    }

    default PodDetailsDto podStatefulToDetailsDto(PodStateful pod) {
        return new PodDetailsDto(
                pod.getId(),
                new ResourceGroupDto(
                        pod.getResourceGroup().getId().toString(),
                        pod.getResourceGroup().getName(),
                        pod.getResourceGroup().isStateless()
                ),
                new CourseBasicDto(
                        pod.getCourse().getId(),
                        pod.getCourse().getName(),
                        pod.getCourse().getDescription(),
                        pod.getCourse().getCourseType().name()
                ),
                new TeamDto(
                        pod.getTeam().getId(),
                        pod.getTeam().getName(),
                        pod.getTeam().isActive(),
                        pod.getTeam().getMaxSize(),
                        pod.getTeam().getUsers()
                )

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