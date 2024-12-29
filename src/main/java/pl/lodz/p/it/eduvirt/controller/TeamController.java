package pl.lodz.p.it.eduvirt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.dto.team.CreateTeamDto;
import pl.lodz.p.it.eduvirt.dto.team.TeamDto;
import pl.lodz.p.it.eduvirt.dto.team.TeamWithCourseDto;
import pl.lodz.p.it.eduvirt.entity.Team;
import pl.lodz.p.it.eduvirt.mappers.TeamMapper;
import pl.lodz.p.it.eduvirt.service.TeamService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@LoggerInterceptor
@RequestMapping("/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;
    private final TeamMapper teamMapper;

   @PostMapping
   public ResponseEntity<TeamWithCourseDto> createTeam(@RequestBody CreateTeamDto createTeamDto) {
       Team team = teamMapper.toEntity(createTeamDto);
       Team createdTeam = teamService.createTeam(team, createTeamDto.getCourseId(), createTeamDto.getKeyValue());
       return ResponseEntity.ok(teamMapper.teamToTeamWithCourseDto(createdTeam));
   }

    @GetMapping
    public ResponseEntity<List<TeamWithCourseDto>> getTeams() {
        List<Team> teams = teamService.getAllTeams();
        List<TeamWithCourseDto> teamWithCourseDtos = teams.stream()
                .map(teamMapper::teamToTeamWithCourseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(teamWithCourseDtos);
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<TeamWithCourseDto> getTeamDetails(@PathVariable UUID teamId) {
        Team team = teamService.getTeamById(teamId);
        return ResponseEntity.ok(teamMapper.teamToTeamWithCourseDto(team));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TeamWithCourseDto>> getTeamsByUser(@PathVariable UUID userId) {
        List<Team> teams = teamService.getTeamsByUser(userId);
        List<TeamWithCourseDto> teamWithCourseDtos = teams.stream()
                .map(teamMapper::teamToTeamWithCourseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(teamWithCourseDtos);
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<TeamDto>> getTeamsByCourse(@PathVariable UUID courseId) {
        List<Team> teams = teamService.getTeamsByCourse(courseId);
        List<TeamDto> teamDtos = teams.stream()
                .map(teamMapper::teamToTeamDto)
                .toList();
        return ResponseEntity.ok(teamDtos);
    }

    //TODO: make it so it takes the user from the context
    @PostMapping("/join")
    public ResponseEntity<Void> joinTeam(@RequestParam String keyValue, @RequestParam UUID userId) {
        teamService.joinTeamOrCourse(keyValue, userId);
        return ResponseEntity.noContent().build();
    }

    //TODO: make it so it takes the user from the context
    @PostMapping("/leave")
    public ResponseEntity<Void> leaveTeam(@RequestParam UUID teamId, @RequestParam UUID userId) {
        teamService.removeUserFromTeam(teamId, userId);
        return ResponseEntity.noContent().build();
    }

}