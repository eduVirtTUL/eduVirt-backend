package pl.lodz.p.it.eduvirt.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.dto.pod.*;
import pl.lodz.p.it.eduvirt.entity.PodStateless;
import pl.lodz.p.it.eduvirt.mappers.PodStatelessMapper;
import pl.lodz.p.it.eduvirt.service.PodStatelessService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/pods/stateless")
@RequiredArgsConstructor
@Transactional(propagation = Propagation.NEVER)
public class PodStatelessController {
    private final PodStatelessService podStatelessService;
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
        PodStateless createdPod = podStatelessService.createStatelessPod(podToCreate, createDto.teamId(), createDto.resourceGroupPoolId());
        return ResponseEntity.ok(podStatelessMapper.podStatelessToDto(createdPod));
    }

    @GetMapping(path = "/{podId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get pod details", description = "Retrieves detailed information about a specific stateless pod")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pod details retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Pod not found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<PodStatelessDetailsDto> getStatelessPod(@PathVariable UUID podId) {
        return ResponseEntity.ok(podStatelessMapper.podStatelessToDetailsDto(podStatelessService.getStatelessPod(podId)));
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
        List<PodStatelessDetailsDto> pods = podStatelessService.getStatelessPodsByTeam(teamId).stream()
                .map(podStatelessMapper::podStatelessToDetailsDto)
                .toList();
        
        if (pods.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(pods);
    }

    @GetMapping(path = "/course/{courseId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get course pods", description = "Retrieves all stateless pods for a specific course")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pods retrieved successfully"),
            @ApiResponse(responseCode = "204", description = "No pods found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<List<PodStatelessDto>> getStatelessPodsByCourse(@PathVariable UUID courseId) {
        List<PodStatelessDto> pods = podStatelessService.getStatelessPodsByCourse(courseId).stream()
                .map(podStatelessMapper::podStatelessToDto)
                .toList();
        
        if (pods.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(pods);
    }

    @GetMapping(path = "/resource-group-pool/{poolId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get resource group pool pods", description = "Retrieves all stateless pods for a specific resource group pool")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pods retrieved successfully"),
            @ApiResponse(responseCode = "204", description = "No pods found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<List<PodStatelessDto>> getStatelessPodsByResourceGroupPool(@PathVariable UUID poolId) {
        List<PodStatelessDto> pods = podStatelessService.getStatelessPodsByResourceGroupPool(poolId).stream()
                .map(podStatelessMapper::podStatelessToDto)
                .toList();
        
        if (pods.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(pods);
    }

    @DeleteMapping("/{podId}")
    @Operation(summary = "Delete pod", description = "Deletes a specific stateless pod")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pod deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Pod not found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<Void> deleteStatelessPod(@PathVariable UUID podId) {
        podStatelessService.deleteStatelessPod(podId);
        return ResponseEntity.noContent().build();
    }
}
