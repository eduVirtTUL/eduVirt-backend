package pl.lodz.p.it.eduvirt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.dto.pagination.PageDto;
import pl.lodz.p.it.eduvirt.dto.pagination.PageInfoDto;
import pl.lodz.p.it.eduvirt.dto.team.CreateTeamDto;
import pl.lodz.p.it.eduvirt.dto.team.TeamDto;
import pl.lodz.p.it.eduvirt.dto.team.TeamWithCourseDto;
import pl.lodz.p.it.eduvirt.dto.team.UpdateTeamDto;
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
        Team team = teamMapper.fromCreateDto(createTeamDto);
        Team createdTeam = teamService.createTeam(team, createTeamDto.getCourseId(), createTeamDto.getKeyValue());
        return ResponseEntity.ok(teamMapper.teamToTeamWithCourseDto(createdTeam));
    }

    @GetMapping
    public ResponseEntity<PageDto<TeamWithCourseDto>> getTeams(
            @RequestParam(name = "pageNumber", defaultValue = "0", required = false) int pageNumber,
            @RequestParam(name = "pageSize", defaultValue = "10", required = false) int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Team> teamsPage = teamService.getAllTeams(pageable);
        
        List<TeamWithCourseDto> teamDtos = teamsPage.getContent().stream()
                .map(teamMapper::teamToTeamWithCourseDto)
                .collect(Collectors.toList());

        PageDto<TeamWithCourseDto> pageDto = new PageDto<>(teamDtos,
                new PageInfoDto(teamsPage.getNumber(), teamsPage.getNumberOfElements(),
                        teamsPage.getTotalPages(), teamsPage.getTotalElements()));

        if (teamDtos.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(pageDto);
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<TeamWithCourseDto> getTeamDetails(@PathVariable UUID teamId) {
        Team team = teamService.getTeamById(teamId);
        return ResponseEntity.ok(teamMapper.teamToTeamWithCourseDto(team));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeamWithCourseDto> updateTeam(@PathVariable UUID id, @RequestBody UpdateTeamDto updateTeamDto) {
        Team team = teamMapper.fromUpdateDto(updateTeamDto);
        Team updatedTeam = teamService.updateTeam(team, id);
        return ResponseEntity.ok(teamMapper.teamToTeamWithCourseDto(updatedTeam));
    }

    @GetMapping("/user/{userId}")
    @Transactional
    public ResponseEntity<PageDto<TeamWithCourseDto>> getTeamsByUser(
            @PathVariable UUID userId,
            @RequestParam(name = "pageNumber", defaultValue = "0", required = false) int pageNumber,
            @RequestParam(name = "pageSize", defaultValue = "10", required = false) int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Team> teamsPage = teamService.getTeamsByUser(userId, pageable);
        
        List<TeamWithCourseDto> teamDtos = teamsPage.getContent().stream()
                .map(teamMapper::teamToTeamWithCourseDto)
                .collect(Collectors.toList());

        PageDto<TeamWithCourseDto> pageDto = new PageDto<>(teamDtos,
                new PageInfoDto(teamsPage.getNumber(), teamsPage.getNumberOfElements(),
                        teamsPage.getTotalPages(), teamsPage.getTotalElements()));

        if (teamDtos.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(pageDto);
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<PageDto<TeamDto>> getTeamsByCourse(
            @PathVariable UUID courseId,
            @RequestParam(name = "pageNumber", defaultValue = "0", required = false) int pageNumber,
            @RequestParam(name = "pageSize", defaultValue = "10", required = false) int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Team> teamsPage = teamService.getTeamsByCourse(courseId, pageable);
        
        List<TeamDto> teamDtos = teamsPage.getContent().stream()
                .map(teamMapper::teamToTeamDto)
                .toList();

        PageDto<TeamDto> pageDto = new PageDto<>(teamDtos,
                new PageInfoDto(teamsPage.getNumber(), teamsPage.getNumberOfElements(),
                        teamsPage.getTotalPages(), teamsPage.getTotalElements()));

        if (teamDtos.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(pageDto);
    }

    @PostMapping("/join")
    public ResponseEntity<Void> joinUsingKey(@RequestParam String keyValue) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        teamService.joinUsingKey(keyValue, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/leave")
    public ResponseEntity<Void> leaveTeam(@RequestParam UUID teamId) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        teamService.removeUserFromTeam(teamId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/add/{teamId}/{userId}")
    public ResponseEntity<Void> addUserToTeam(@PathVariable UUID teamId, @PathVariable UUID userId) {
        teamService.addUserToTeam(teamId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/remove/{teamId}/{userId}")
    public ResponseEntity<Void> removeUserFromTeam(@PathVariable UUID teamId, @PathVariable UUID userId) {
        teamService.removeUserFromTeam(teamId, userId);
        return ResponseEntity.noContent().build();
    }
}