package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;

import pl.lodz.p.it.eduvirt.dto.course.CourseBasicDto;
import pl.lodz.p.it.eduvirt.dto.team.CreateTeamDto;
import pl.lodz.p.it.eduvirt.dto.team.TeamDto;
import pl.lodz.p.it.eduvirt.dto.team.TeamWithCourseDto;
import pl.lodz.p.it.eduvirt.dto.team.UpdateTeamDto;
import pl.lodz.p.it.eduvirt.entity.Team;

@Mapper(componentModel = "spring")
public interface TeamMapper {

    default TeamWithCourseDto teamToTeamWithCourseDto(Team team) {
        CourseBasicDto courseBasicDto = team.getCourse() != null ? new CourseBasicDto(
                team.getCourse().getId(),
                team.getCourse().getName(),
                team.getCourse().getDescription(),
                team.getCourse().getCourseType().toString(),
                team.getCourse().getClusterId().toString()
        ) : null;

        return new TeamWithCourseDto(
                team.getId(),
                team.getName(),
                team.isActive(),
                team.getMaxSize(),
                team.getUsers(),
                courseBasicDto
        );
    }

    default TeamDto teamToTeamDto(Team team) {
        return new TeamDto(
                team.getId(),
                team.getName(),
                team.isActive(),
                team.getMaxSize(),
                team.getUsers()
        );
    }

    default Team fromCreateDto(CreateTeamDto dto) {
        Team team = new Team();
        team.setName(dto.getName());
        team.setMaxSize(dto.getMaxSize());
        team.setActive(true);
        return team;
    }

    default Team fromUpdateDto(UpdateTeamDto dto) {
        Team team = new Team();
        team.setName(dto.getName());
        team.setMaxSize(dto.getMaxSize());
        team.setActive(dto.isActive());
        return team;
    }
}