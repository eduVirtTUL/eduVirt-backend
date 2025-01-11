package pl.lodz.p.it.eduvirt.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.dto.pod.*;
import pl.lodz.p.it.eduvirt.entity.PodStateful;
import pl.lodz.p.it.eduvirt.mappers.PodStatefulMapper;
import pl.lodz.p.it.eduvirt.service.PodStatefulService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/pods/stateful")
@RequiredArgsConstructor
@Transactional(propagation = Propagation.NEVER)
public class PodStatefulController {
    private final PodStatefulService podStatefulService;
    private final PodStatefulMapper podStatefulMapper;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//    @PreAuthorize("hasRole('student')")
    @Operation(summary = "Create new stateful pod", description = "Creates a new stateful pod for the specified team and resource group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pod created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PodStatefulDto> createStatefulPod(@RequestBody @Validated CreatePodStatefulDto createDto) {
        PodStateful podToCreate = podStatefulMapper.createPodStatefulDtoToPodStateful(createDto);
        PodStateful createdPod = podStatefulService.createStatefulPod(
                podToCreate,
                createDto.teamId(),
                createDto.resourceGroupId()
        );
        return ResponseEntity.ok(podStatefulMapper.podStatefulToDto(createdPod));
    }

    @GetMapping(path = "/{podId}", produces = MediaType.APPLICATION_JSON_VALUE)
//    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get pod details", description = "Retrieves detailed information about a specific stateful pod")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pod details retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Pod not found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<PodStatefulDetailsDto> getStatefulPod(@PathVariable UUID podId) {
        return ResponseEntity.ok(podStatefulMapper.podStatefulToDetailsDto(podStatefulService.getStatefulPod(podId)));
    }

    @GetMapping(path = "/team/{teamId}", produces = MediaType.APPLICATION_JSON_VALUE)
//    @PreAuthorize("hasAnyRole('teacher', 'administrator')")
    @Operation(summary = "Get team pods", description = "Retrieves all stateful pods for a specific team")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pods retrieved successfully"),
            @ApiResponse(responseCode = "204", description = "No pods found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResponseEntity<List<PodStatefulDetailsDto>> getStatefulPodsByTeam(@PathVariable UUID teamId) {
        List<PodStatefulDetailsDto> pods = podStatefulService.getStatefulPodsByTeam(teamId).stream()
                .map(podStatefulMapper::podStatefulToDetailsDto)
                .toList();
        
        if (pods.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(pods);
    }

    @GetMapping(path = "/course/{courseId}", produces = MediaType.APPLICATION_JSON_VALUE)
//    @PreAuthorize("hasAnyRole('teacher', 'administrator')")
    @Operation(summary = "Get course pods", description = "Retrieves all stateful pods for a specific course")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pods retrieved successfully"),
            @ApiResponse(responseCode = "204", description = "No pods found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<List<PodStatefulDto>> getStatefulPodsByCourse(@PathVariable UUID courseId) {
        List<PodStatefulDto> pods = podStatefulService.getStatefulPodsByCourse(courseId).stream()
                .map(podStatefulMapper::podStatefulToDto)
                .toList();
        
        if (pods.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(pods);
    }

    @GetMapping(path = "/resource-group/{resourceGroupId}", produces = MediaType.APPLICATION_JSON_VALUE)
//    @PreAuthorize("hasAnyRole('teacher', 'administrator')")
    @Operation(summary = "Get resource group pods", description = "Retrieves all stateful pods for a specific resource group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pods retrieved successfully"),
            @ApiResponse(responseCode = "204", description = "No pods found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<List<PodStatefulDto>> getStatefulPodsByResourceGroup(@PathVariable UUID resourceGroupId) {
        List<PodStatefulDto> pods = podStatefulService.getStatefulPodsByResourceGroup(resourceGroupId).stream()
                .map(podStatefulMapper::podStatefulToDto)
                .toList();
        
        if (pods.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(pods);
    }

    @DeleteMapping("/{podId}")
//    @PreAuthorize("hasAnyRole('teacher', 'administrator')")
    @Operation(summary = "Delete pod", description = "Deletes a specific stateful pod")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pod deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Pod not found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<Void> deleteStatefulPod(@PathVariable UUID podId) {
        podStatefulService.deleteStatefulPod(podId);
        return ResponseEntity.noContent().build();
    }
}
