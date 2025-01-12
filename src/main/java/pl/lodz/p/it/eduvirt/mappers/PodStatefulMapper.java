package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.lodz.p.it.eduvirt.dto.course.CourseBasicDto;
import pl.lodz.p.it.eduvirt.dto.pod.CreatePodStatefulDto;
import pl.lodz.p.it.eduvirt.dto.pod.PodStatefulDetailsDto;
import pl.lodz.p.it.eduvirt.dto.pod.PodStatefulDto;
import pl.lodz.p.it.eduvirt.dto.resource_group.ResourceGroupDto;
import pl.lodz.p.it.eduvirt.dto.team.TeamDto;
import pl.lodz.p.it.eduvirt.dto.user.UserDto;
import pl.lodz.p.it.eduvirt.entity.PodStateful;

@Mapper(componentModel = "spring")
public interface PodStatefulMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "team", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "resourceGroup", ignore = true)
    PodStateful createPodStatefulDtoToPodStateful(CreatePodStatefulDto dto);

    @Mapping(target = "teamId", source = "team.id")
    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "resourceGroupId", source = "resourceGroup.id")
    PodStatefulDto podStatefulToDto(PodStateful pod);

    default PodStatefulDetailsDto podStatefulToDetailsDto(PodStateful pod) {
        return new PodStatefulDetailsDto(
                pod.getId(),
                new ResourceGroupDto(
                        pod.getResourceGroup().getId().toString(),
                        pod.getResourceGroup().getName(),
                        pod.getResourceGroup().getDescription(),
                        pod.getResourceGroup().isStateless(),
                        pod.getResourceGroup().getMaxRentTime()
                ),
                new CourseBasicDto(
                        pod.getCourse().getId(),
                        pod.getCourse().getName(),
                        pod.getCourse().getDescription(),
                        pod.getCourse().getCourseType().name(),
                        pod.getCourse().getClusterId().toString()
                ),
                new TeamDto(
                        pod.getTeam().getId(),
                        pod.getTeam().getName(),
                        pod.getTeam().isActive(),
                        pod.getTeam().getMaxSize(),
                        pod.getTeam().getUsers().stream().map(user -> new UserDto(
                                user.getId().toString(),
                                user.getOVirtId().toString(),
                                user.getEmail(),
                                user.getUserName(),
                                user.getFirstName(),
                                user.getLastName()
                        )).toList()
                )
        );
    }
}
