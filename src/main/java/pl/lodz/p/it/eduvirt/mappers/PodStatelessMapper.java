package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.lodz.p.it.eduvirt.dto.course.CourseBasicDto;
import pl.lodz.p.it.eduvirt.dto.pod.CreatePodStatelessDto;
import pl.lodz.p.it.eduvirt.dto.pod.PodStatelessDetailsDto;
import pl.lodz.p.it.eduvirt.dto.pod.PodStatelessDto;
import pl.lodz.p.it.eduvirt.dto.resource_group_pool.ResourceGroupPoolWithMaxRentTimeDto;
import pl.lodz.p.it.eduvirt.dto.team.TeamDto;
import pl.lodz.p.it.eduvirt.dto.user.UserDto;
import pl.lodz.p.it.eduvirt.entity.PodStateless;

@Mapper(componentModel = "spring")
public interface PodStatelessMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "team", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "resourceGroupPool", ignore = true)
    PodStateless createPodStatelessDtoToPodStateless(CreatePodStatelessDto dto);

    @Mapping(target = "teamId", source = "team.id")
    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "resourceGroupPoolId", source = "resourceGroupPool.id")
    PodStatelessDto podStatelessToDto(PodStateless pod);

    default PodStatelessDetailsDto podStatelessToDetailsDto(PodStateless pod) {
        return new PodStatelessDetailsDto(
                pod.getId(),
                new ResourceGroupPoolWithMaxRentTimeDto(
                        pod.getResourceGroupPool().getId(),
                        pod.getResourceGroupPool().getName(),
                        pod.getResourceGroupPool().getDescription(),
                        pod.getResourceGroupPool().getMaxRentTime()
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
                        pod.getTeam().getMaxSize(),
                        pod.getTeam().getUsers().stream().map(user -> new UserDto(
                                user.getId().toString(),
                                user.getOVirtId().toString(),
                                user.getEmail(),
                                user.getUserName(),
                                user.getFirstName(),
                                user.getLastName()
                        )).toList()
                ),
                pod.getResourceGroupPool().getMaxRent()
        );
    }
}
