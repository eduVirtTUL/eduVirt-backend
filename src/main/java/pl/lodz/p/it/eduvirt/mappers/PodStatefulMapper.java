package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.lodz.p.it.eduvirt.dto.pod.CreatePodStatefulDto;
import pl.lodz.p.it.eduvirt.dto.pod.PodStatefulDto;
import pl.lodz.p.it.eduvirt.entity.eduvirt.PodStateful;

@Mapper(componentModel = "spring")
public interface PodStatefulMapper {

    @Mapping(target = "id", ignore = true)
    PodStateful toEntity(CreatePodStatefulDto dto);

    @Mapping(target = "resourceGroupId", source = "resourceGroup.id")
    @Mapping(target = "teamId", source = "team.id")
    @Mapping(target = "courseId", source = "course.id")
    PodStatefulDto toDto(PodStateful pod);
}