package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.lodz.p.it.eduvirt.dto.pod.CreatePodStatefulDto;
import pl.lodz.p.it.eduvirt.dto.pod.PodStatefulDto;
import pl.lodz.p.it.eduvirt.entity.PodStateful;

@Mapper(componentModel = "spring")
public abstract class PodStatefulMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "resourceGroup", ignore = true)
    @Mapping(target = "team", ignore = true)
    @Mapping(target = "course", ignore = true)
    public abstract PodStateful toEntity(CreatePodStatefulDto dto);

    @Mapping(target = "resourceGroupId", source = "resourceGroup.id")
    @Mapping(target = "teamId", source = "team.id")
    @Mapping(target = "courseId", source = "course.id")
    public abstract PodStatefulDto toDto(PodStateful pod);
}