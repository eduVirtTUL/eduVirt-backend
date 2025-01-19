package pl.lodz.p.it.eduvirt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.dto.access_key.JoinTeamKeyDto;
import pl.lodz.p.it.eduvirt.dto.pagination.PageDto;
import pl.lodz.p.it.eduvirt.dto.pagination.PageInfoDto;
import pl.lodz.p.it.eduvirt.dto.team.CreateTeamDto;
import pl.lodz.p.it.eduvirt.dto.team.TeamWithCourseDto;
import pl.lodz.p.it.eduvirt.dto.team.TeamWithKeyDto;
import pl.lodz.p.it.eduvirt.dto.team.UpdateTeamDto;
import pl.lodz.p.it.eduvirt.dto.user.UserDto;
import pl.lodz.p.it.eduvirt.entity.Course;
import pl.lodz.p.it.eduvirt.entity.Team;
import pl.lodz.p.it.eduvirt.entity.User;
import pl.lodz.p.it.eduvirt.entity.key.CourseType;
import pl.lodz.p.it.eduvirt.entity.key.TeamAccessKey;
import pl.lodz.p.it.eduvirt.exceptions.handle.ExceptionResponse;
import pl.lodz.p.it.eduvirt.exceptions.user.UserNotFoundException;
import pl.lodz.p.it.eduvirt.mappers.TeamMapper;
import pl.lodz.p.it.eduvirt.repository.UserRepository;
import pl.lodz.p.it.eduvirt.repository.key.TeamAccessKeyRepository;
import pl.lodz.p.it.eduvirt.service.CourseService;
import pl.lodz.p.it.eduvirt.service.TeamService;
import pl.lodz.p.it.eduvirt.util.RoleConstants;
import pl.lodz.p.it.eduvirt.util.etag.ETagHelper;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@LoggerInterceptor
@RequestMapping("/teams")
@RequiredArgsConstructor
public class TeamController {

    /* Services */

    private final TeamService teamService;
    private final CourseService courseService;

    /* Repositories */

    private final UserRepository userRepository;
    private final TeamAccessKeyRepository teamAccessKeyRepository;

    /* Mappers */

    private final TeamMapper teamMapper;

    /* Helpers */

    private final ETagHelper etagHelper;

    @PostMapping
    @Transactional
    @PreAuthorize("hasAuthority('teacher')")
    public ResponseEntity<TeamWithCourseDto> createTeam(@RequestBody @Validated CreateTeamDto createTeamDto) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        Course course = courseService.getCourse(createTeamDto.getCourseId());

        List<String> authorities = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        if (authorities.contains(RoleConstants.TEACHER) && course.getTeachers().contains(user)) {
            return ResponseEntity.ok(teamMapper
                    .teamToTeamWithCourseDto(teamService
                            .createTeam(teamMapper
                                            .fromCreateDto(createTeamDto),
                                    course,
                                    createTeamDto.getKeyValue())));
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @PutMapping("/{id}")
    @Transactional
    @PreAuthorize("hasAuthority('teacher')")
    public ResponseEntity<TeamWithCourseDto> updateTeam(
            @PathVariable UUID id,
            @RequestBody @Validated UpdateTeamDto updateTeamDto,
            @RequestHeader(HttpHeaders.IF_MATCH) String ifMatch) {

        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        Team team = teamService.getTeamById(id);
        Course course = team.getCourse();

        List<String> authorities = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        if (authorities.contains(RoleConstants.TEACHER) && course.getTeachers().contains(user)) {
            Team mapped = teamMapper.fromUpdateDto(updateTeamDto);
            Team updated = teamService.updateTeam(mapped, id, ifMatch);
            return ResponseEntity.ok(teamMapper.teamToTeamWithCourseDto(updated));
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('administrator')")
    public ResponseEntity<PageDto<TeamWithCourseDto>> getTeams(
            @RequestParam(name = "pageNumber", defaultValue = "0", required = false) int pageNumber,
            @RequestParam(name = "pageSize", defaultValue = "10", required = false) int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Team> teamsPage = teamService.getAllTeams(pageable);

        List<TeamWithCourseDto> listOfDTOs = teamsPage.getContent().stream()
                .map(teamMapper::teamToTeamWithCourseDto)
                .collect(Collectors.toList());

        PageDto<TeamWithCourseDto> pageDto = new PageDto<>(listOfDTOs,
                new PageInfoDto(teamsPage.getNumber(), teamsPage.getNumberOfElements(),
                        teamsPage.getTotalPages(), teamsPage.getTotalElements()));

        if (listOfDTOs.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(pageDto);
    }

    @GetMapping("/{teamId}")
    @Transactional
    @PreAuthorize("isAuthenticated()")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    headers = @Header(name = "ETag", description = "ETag value", schema = @Schema(implementation = String.class)),
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = TeamWithCourseDto.class))}
            ),
            @ApiResponse(responseCode = "404", content = {@Content(mediaType = "application/json",
                    schema = @Schema(implementation = ExceptionResponse.class))})
    })
    public ResponseEntity<TeamWithCourseDto> getTeamDetails(@PathVariable UUID teamId) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        Team team = teamService.getTeamById(teamId);
        Course course = team.getCourse();

        List<String> authorities = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        if (authorities.contains(RoleConstants.TEACHER) && course.getTeachers().contains(user) ||
                authorities.contains(RoleConstants.STUDENT) && team.getUsers().contains(user)) {

            String etag = etagHelper.generateEtag(team);
            return ResponseEntity.ok()
                    .eTag(etag)
                    .body(teamMapper.teamToTeamWithCourseDto(team));
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/student")
    @Transactional
    @PreAuthorize("hasAuthority('student')")
    public ResponseEntity<PageDto<TeamWithCourseDto>> getTeamsByStudent(
            @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(name = "size", required = false, defaultValue = "10") Integer size,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "sort", required = false, defaultValue = "ASC") String sortOrder) {

        if (!(sortOrder.equals("ASC") || sortOrder.equals("DESC"))) {
            sortOrder = "ASC";
        }

        UUID studentId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        Page<Team> teamsPage = teamService.getTeamsByStudent(studentId, page, size, search, sortOrder);

        List<TeamWithCourseDto> listOfDTOs = teamsPage.getContent().stream()
                .map(teamMapper::teamToTeamWithCourseDto)
                .toList();

        PageDto<TeamWithCourseDto> pageDto = new PageDto<>(listOfDTOs,
                new PageInfoDto(teamsPage.getNumber(), teamsPage.getNumberOfElements(),
                        teamsPage.getTotalPages(), teamsPage.getTotalElements()));

        if (listOfDTOs.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(pageDto);
    }

    @GetMapping("/course/{courseId}")
    @Transactional
    @PreAuthorize("hasAuthority('teacher')")
    public ResponseEntity<PageDto<TeamWithKeyDto>> getTeamsByCourse(
            @PathVariable UUID courseId,
            @RequestParam(name = "pageNumber", defaultValue = "0", required = false) int pageNumber,
            @RequestParam(name = "pageSize", defaultValue = "10", required = false) int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Team> teamsPage = teamService.getTeamsByCourse(courseId, pageable);

        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        Course course = courseService.getCourse(courseId);

        if (course.getTeachers().contains(user)) {
            List<TeamWithKeyDto> listOfDTOs = teamsPage.getContent().stream()
                    .map(team -> {
                        String keyValue = null;
                        if (course.getCourseType() == CourseType.TEAM_BASED) {
                            keyValue = teamAccessKeyRepository.findByTeamId(team.getId())
                                    .map(TeamAccessKey::getKeyValue)
                                    .orElse(null);
                        }
                        return TeamWithKeyDto.builder()
                                .id(team.getId())
                                .name(team.getName())
                                .active(team.isActive())
                                .maxSize(team.getMaxSize())
                                .users(team.getUsers().stream()
                                        .map(u -> new UserDto(
                                                u.getId().toString(),
                                                u.getOVirtId().toString(),
                                                u.getEmail(),
                                                u.getUserName(),
                                                u.getFirstName(),
                                                u.getLastName()
                                        )).toList())
                                .keyValue(keyValue)
                                .build();
                    })
                    .toList();

            PageDto<TeamWithKeyDto> pageDto = new PageDto<>(listOfDTOs,
                    new PageInfoDto(teamsPage.getNumber(), teamsPage.getNumberOfElements(),
                            teamsPage.getTotalPages(), teamsPage.getTotalElements()));
            if (!listOfDTOs.isEmpty()) return ResponseEntity.ok(pageDto);
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/join")
    @PreAuthorize("hasAuthority('student')")
    public ResponseEntity<Void> joinUsingKey(@RequestBody @Validated JoinTeamKeyDto joinRequest) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        teamService.joinUsingKey(joinRequest.getKeyValue(), user);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/leave")
    @PreAuthorize("hasAuthority('student')")
    public ResponseEntity<Void> leaveTeam(@RequestParam UUID teamId) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        teamService.leaveTeam(teamId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{teamId}/add-student")
    @PreAuthorize("hasAuthority('teacher')")
    public ResponseEntity<Void> addStudentToTeam(@PathVariable UUID teamId, @RequestParam String email) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        Team team = teamService.getTeamById(teamId);
        Course course = team.getCourse();

        List<String> authorities = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        if (authorities.contains(RoleConstants.TEACHER) && course.getTeachers().contains(user)) {
            teamService.addStudentToTeam(team, email);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @PostMapping("/{teamId}/remove-student")
    @PreAuthorize("hasAuthority('teacher')")
    public ResponseEntity<Void> removeStudentFromTeam(@PathVariable UUID teamId, @RequestParam String email) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        Team team = teamService.getTeamById(teamId);
        Course course = team.getCourse();

        List<String> authorities = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        if (authorities.contains(RoleConstants.TEACHER) && course.getTeachers().contains(user)) {
            teamService.removeStudentFromTeam(team, email);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @DeleteMapping("/{teamId}")
    @Transactional
    @PreAuthorize("hasAuthority('teacher')")
    public ResponseEntity<Void> deleteTeam(@PathVariable UUID teamId) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        Team team = teamService.getTeamById(teamId);
        Course course = team.getCourse();

        List<String> authorities = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        if (authorities.contains(RoleConstants.TEACHER) && course.getTeachers().contains(user)) {
            teamService.deleteTeam(team);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
}