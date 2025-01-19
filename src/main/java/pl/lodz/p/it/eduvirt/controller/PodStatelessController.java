package pl.lodz.p.it.eduvirt.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.dto.pod.*;
import pl.lodz.p.it.eduvirt.entity.*;
import pl.lodz.p.it.eduvirt.exceptions.user.UserNotFoundException;
import pl.lodz.p.it.eduvirt.mappers.PodStatelessMapper;
import pl.lodz.p.it.eduvirt.repository.UserRepository;
import pl.lodz.p.it.eduvirt.service.CourseService;
import pl.lodz.p.it.eduvirt.service.PodStatelessService;
import pl.lodz.p.it.eduvirt.service.ResourceGroupPoolService;
import pl.lodz.p.it.eduvirt.service.TeamService;
import pl.lodz.p.it.eduvirt.util.RoleConstants;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/pods/stateless")
@RequiredArgsConstructor
@Transactional(propagation = Propagation.NEVER)
public class PodStatelessController {

    /* Services */

    private final PodStatelessService podStatelessService;
    private final TeamService teamService;
    private final CourseService courseService;
    private final ResourceGroupPoolService resourceGroupPoolService;

    /* Repositories */

    private final UserRepository userRepository;

    /* Mappers */

    private final PodStatelessMapper podStatelessMapper;


    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create new stateless pod", description = "Creates a new stateless pod for the specified team and resource group pool")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pod created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PodStatelessDto> createStatelessPod(@RequestBody @Validated CreatePodStatelessDto createDto) {
        PodStateless podToCreate = podStatelessMapper.createPodStatelessDtoToPodStateless(createDto);

        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        Team team = teamService.getTeamById(createDto.teamId());
        Course course = team.getCourse();

        List<String> authorities = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        if (authorities.contains(RoleConstants.ADMINISTRATOR) ||
                (authorities.contains(RoleConstants.TEACHER) && course.getTeachers().contains(user))) {

            PodStateless createdPod = podStatelessService.createStatelessPod(
                    podToCreate,
                    createDto.teamId(),
                    createDto.resourceGroupPoolId()
            );
            return ResponseEntity.ok(podStatelessMapper.podStatelessToDto(createdPod));
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @GetMapping(path = "/team/{teamId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get team pods", description = "Retrieves all stateless pods for a specific team")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pods retrieved successfully"),
            @ApiResponse(responseCode = "204", description = "No pods found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResponseEntity<List<PodStatelessDetailsDto>> getStatelessPodsByTeam(@PathVariable UUID teamId) {
        List<PodStatelessDetailsDto> listOfDTOs = podStatelessService.getStatelessPodsByTeam(teamId).stream()
                .map(podStatelessMapper::podStatelessToDetailsDto)
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

    @GetMapping(path = "/course/{courseId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get course pods", description = "Retrieves all stateless pods for a specific course")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pods retrieved successfully"),
            @ApiResponse(responseCode = "204", description = "No pods found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<List<PodStatelessDetailsDto>> getStatelessPodsByCourse(@PathVariable UUID courseId) {
        List<PodStatelessDetailsDto> listOfDTOs = podStatelessService.getStatelessPodsByCourse(courseId).stream()
                .map(podStatelessMapper::podStatelessToDetailsDto)
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

    @GetMapping(path = "/resource-group-pool/{poolId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get resource group pool pods", description = "Retrieves all stateless pods for a specific resource group pool")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pods retrieved successfully"),
            @ApiResponse(responseCode = "204", description = "No pods found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<List<PodStatelessDto>> getStatelessPodsByResourceGroupPool(@PathVariable UUID poolId) {
        List<PodStatelessDto> listOfDTOs = podStatelessService.getStatelessPodsByResourceGroupPool(poolId).stream()
                .map(podStatelessMapper::podStatelessToDto)
                .toList();

        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        ResourceGroupPool resourceGroupPool = resourceGroupPoolService.getResourceGroupPool(poolId);
        Course course = resourceGroupPool.getCourse();

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

    @DeleteMapping("/{podId}")
    @Operation(summary = "Delete pod", description = "Deletes a specific stateless pod")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pod deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Pod not found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })

    public ResponseEntity<Void> deleteStatelessPod(@PathVariable UUID podId) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        PodStateless podStateless = podStatelessService.getStatelessPodById(podId);
        Course course = podStateless.getCourse();

        List<String> authorities = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        if (authorities.contains(RoleConstants.ADMINISTRATOR) ||
                (authorities.contains(RoleConstants.TEACHER) && course.getTeachers().contains(user))) {
            podStatelessService.deleteStatelessPod(podId);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
}
