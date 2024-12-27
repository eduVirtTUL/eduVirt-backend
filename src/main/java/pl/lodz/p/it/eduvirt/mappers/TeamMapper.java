package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.lodz.p.it.eduvirt.dto.team.CreateTeamDto;
import pl.lodz.p.it.eduvirt.dto.team.TeamDto;
import pl.lodz.p.it.eduvirt.entity.Team;
import pl.lodz.p.it.eduvirt.entity.key.AccessKey; //this import has to be here

@Mapper(componentModel = "spring", uses = {AccessKeyMapper.class})
public interface TeamMapper {
    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "keyValue", expression = "java(team.getKeys() != null && !team.getKeys().isEmpty() ? team.getKeys().get(0).getKeyValue() : null)")
    TeamDto toDto(Team team);

    @Mapping(target = "course", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "keys", expression = "java(new ArrayList<>())")
    @Mapping(target = "active", ignore = true)
    Team toEntity(CreateTeamDto createTeamDto);
}