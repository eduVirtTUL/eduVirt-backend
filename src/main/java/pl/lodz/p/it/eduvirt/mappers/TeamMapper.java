package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.lodz.p.it.eduvirt.dto.team.CreateTeamDto;
import pl.lodz.p.it.eduvirt.dto.team.TeamDto;
import pl.lodz.p.it.eduvirt.entity.eduvirt.Course;
import pl.lodz.p.it.eduvirt.entity.eduvirt.Team;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface TeamMapper {
    @Mapping(target = "course", source = "course")
    TeamDto toDto(Team team);

    @Mapping(target = "key", source = "key")
    @Mapping(target = "course", ignore = true)
    Team toEntity(CreateTeamDto createTeamDto);

    default UUID courseToUuid(Course course) {
        return course != null ? course.getId() : null;
    }
}