package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.lodz.p.it.eduvirt.dto.access_key.AccessKeyDto;
import pl.lodz.p.it.eduvirt.entity.key.AccessKey;

@Mapper(componentModel = "spring")
public interface AccessKeyMapper {
    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "teamId", source = "team.id")
    AccessKeyDto toDto(AccessKey accessKey);
}