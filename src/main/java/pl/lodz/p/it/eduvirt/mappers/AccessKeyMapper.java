package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import pl.lodz.p.it.eduvirt.dto.access_key.CourseAccessKeyDto;
import pl.lodz.p.it.eduvirt.dto.access_key.TeamAccessKeyDto;
import pl.lodz.p.it.eduvirt.entity.key.CourseAccessKey;
import pl.lodz.p.it.eduvirt.entity.key.TeamAccessKey;

@Mapper(componentModel = "spring")
public interface AccessKeyMapper {
    
    @Mapping(source = "course.id", target = "courseId")
    CourseAccessKeyDto toCourseKeyDto(CourseAccessKey key);
    
    TeamAccessKeyDto toTeamKeyDto(TeamAccessKey key);

//    default AccessKeyDto toDto(AccessKey key) {
//        if (key instanceof CourseAccessKey) {
//            return toCourseKeyDto((CourseAccessKey) key);
//        } else {
//            return toTeamKeyDto((TeamAccessKey) key);
//        }
//    }
}