package pl.lodz.p.it.eduvirt.controller;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.dto.team.CreateTeamDto;
import pl.lodz.p.it.eduvirt.dto.team.TeamDto;
import pl.lodz.p.it.eduvirt.entity.eduvirt.Team;
import pl.lodz.p.it.eduvirt.mappers.TeamMapper;
import pl.lodz.p.it.eduvirt.service.TeamService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@LoggerInterceptor
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;
    private final TeamMapper teamMapper;

    @PostMapping
    public ResponseEntity<TeamDto> createTeam(@Valid @RequestBody CreateTeamDto createTeamDto) {
        Team team = teamMapper.toEntity(createTeamDto);
        Team createdTeam = teamService.createTeam(team, createTeamDto.getCourseId());
        return ResponseEntity.ok(teamMapper.toDto(createdTeam));
    }

    @GetMapping
    @Transactional
    public ResponseEntity<List<TeamDto>> getTeams() {
        List<Team> teams = teamService.getTeams();
        List<TeamDto> teamDtos = teams.stream()
                .map(teamMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(teamDtos);
    }

    @GetMapping("/{teamId}")
    @Transactional
    public ResponseEntity<TeamDto> getTeam(@PathVariable UUID teamId) {
        Team team = teamService.getTeam(teamId);
        return ResponseEntity.ok(teamMapper.toDto(team));
    }

    @GetMapping("/user/{userId}")
    @Transactional
    public ResponseEntity<List<TeamDto>> getTeamsByUser(@PathVariable UUID userId) {
        List<Team> teams = teamService.getTeamsByUser(userId);
        List<TeamDto> teamDtos = teams.stream()
                .map(teamMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(teamDtos);
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<TeamDto>> getTeamsByCourse(@PathVariable UUID courseId) {
        List<Team> teams = teamService.getTeamsByCourse(courseId);
        List<TeamDto> teamDtos = teams.stream()
                .map(teamMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(teamDtos);
    }

    @PostMapping("/join/{key}")
    public ResponseEntity<TeamDto> joinTeam(@PathVariable String key, @RequestParam UUID userId) {
        Team team = teamService.joinTeamOrCreate(key, userId);
        return ResponseEntity.ok(teamMapper.toDto(team));
    }
}