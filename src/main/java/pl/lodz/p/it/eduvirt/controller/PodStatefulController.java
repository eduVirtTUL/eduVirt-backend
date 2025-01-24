package pl.lodz.p.it.eduvirt.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.dto.pod.CreatePodStatefulDto;
import pl.lodz.p.it.eduvirt.dto.pod.PodStatefulDetailsDto;
import pl.lodz.p.it.eduvirt.dto.pod.PodStatefulDto;
import pl.lodz.p.it.eduvirt.entity.*;
import pl.lodz.p.it.eduvirt.exceptions.user.UserNotFoundException;
import pl.lodz.p.it.eduvirt.mappers.PodStatefulMapper;
import pl.lodz.p.it.eduvirt.repository.UserRepository;
import pl.lodz.p.it.eduvirt.service.CourseService;
import pl.lodz.p.it.eduvirt.service.PodStatefulService;
import pl.lodz.p.it.eduvirt.service.ResourceGroupService;
import pl.lodz.p.it.eduvirt.service.TeamService;
import pl.lodz.p.it.eduvirt.util.RoleConstants;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/pods/stateful")
@RequiredArgsConstructor
@Transactional(propagation = Propagation.NEVER)
public class PodStatefulController {

    /* Services */

    private final PodStatefulService podStatefulService;
    private final TeamService teamService;
    private final CourseService courseService;

    /* Repositories */

    private final UserRepository userRepository;

    /* Mappers */

    private final PodStatefulMapper podStatefulMapper;
    private final ResourceGroupService resourceGroupService;


    @Transactional
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('teacher')")
    @Operation(summary = "Create new stateful pod", description = "Creates a new stateful pod for the specified team and resource group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pod created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PodStatefulDto> createStatefulPod(@RequestBody @Validated CreatePodStatefulDto createDto) {
        PodStateful podToCreate = podStatefulMapper.createPodStatefulDtoToPodStateful(createDto);

        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        Team team = teamService.getTeamById(createDto.teamId());
        Course course = team.getCourse();

        List<String> authorities = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        if ((authorities.contains(RoleConstants.TEACHER) && course.getTeachers().contains(user))) {
            PodStateful createdPod = podStatefulService.createStatefulPod(
                    podToCreate,
                    createDto.teamId(),
                    createDto.resourceGroupId()
            );
            return ResponseEntity.ok(podStatefulMapper.podStatefulToDto(createdPod));
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @GetMapping(path = "/team/{teamId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get team pods", description = "Retrieves all stateful pods for a specific team")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pods retrieved successfully"),
            @ApiResponse(responseCode = "204", description = "No pods found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<List<PodStatefulDetailsDto>> getStatefulPodsByTeam(@PathVariable UUID teamId) {
        List<PodStatefulDetailsDto> listOfDTOs = podStatefulService.getStatefulPodsByTeam(teamId).stream()
                .map(podStatefulMapper::podStatefulToDetailsDto)
                .toList();

        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        Team team = teamService.getTeamById(teamId);
        Course course = team.getCourse();

        List<String> authorities = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        if (authorities.contains(RoleConstants.ADMINISTRATOR) ||
                (authorities.contains(RoleConstants.TEACHER) && course.getTeachers().contains(user)) ||
                (authorities.contains(RoleConstants.STUDENT) && team.getUsers().contains(user)) &&
                        !listOfDTOs.isEmpty()) {
            return ResponseEntity.ok(listOfDTOs);
        }

        return ResponseEntity.noContent().build();
    }

    @Transactional
    @GetMapping(path = "/course/{courseId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority('teacher', 'administrator')")
    @Operation(summary = "Get course pods", description = "Retrieves all stateful pods for a specific course")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pods retrieved successfully"),
            @ApiResponse(responseCode = "204", description = "No pods found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<List<PodStatefulDetailsDto>> getStatefulPodsByCourse(@PathVariable UUID courseId) {
        List<PodStatefulDetailsDto> listOfDTOs = podStatefulService.getStatefulPodsByCourse(courseId).stream()
                .map(podStatefulMapper::podStatefulToDetailsDto)
                .toList();

        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        Course course = courseService.getCourse(courseId);

        List<String> authorities = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        if (authorities.contains(RoleConstants.ADMINISTRATOR) ||
                (authorities.contains(RoleConstants.TEACHER) && course.getTeachers().contains(user)) &&
                        !listOfDTOs.isEmpty()) {
            return ResponseEntity.ok(listOfDTOs);
        }

        return ResponseEntity.noContent().build();
    }

    @Transactional
    @DeleteMapping("/{podId}")
    @PreAuthorize("hasAuthority('teacher')")
    @Operation(summary = "Delete stateful pod", description = "Deletes a specific stateful pod")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pod deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Pod not found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<Void> deleteStatefulPod(@PathVariable UUID podId) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        PodStateful podStateful = podStatefulService.getStatefulPodById(podId);
        Course course = podStateful.getCourse();

        List<String> authorities = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        if ((authorities.contains(RoleConstants.TEACHER) && course.getTeachers().contains(user))) {
            podStatefulService.deleteStatefulPod(podId);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
}
