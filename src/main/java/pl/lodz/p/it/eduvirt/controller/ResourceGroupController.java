package pl.lodz.p.it.eduvirt.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.dto.resource_group.ResourceGroupDto;
import pl.lodz.p.it.eduvirt.dto.resource_group.UpdateResourceGroupDto;
import pl.lodz.p.it.eduvirt.entity.ResourceGroup;
import pl.lodz.p.it.eduvirt.exceptions.handle.ExceptionResponse;
import pl.lodz.p.it.eduvirt.mappers.ResourceGroupMapper;
import pl.lodz.p.it.eduvirt.service.ResourceGroupService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/resource-group")
@RequiredArgsConstructor
public class ResourceGroupController {

    private final ResourceGroupService resourceGroupService;
    private final ResourceGroupMapper resourceGroupMapper;

    @GetMapping
    public ResponseEntity<List<ResourceGroupDto>> getResourceGroups() {
        return ResponseEntity.ok(resourceGroupMapper.toDtos(resourceGroupService.getResourceGroups().stream()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResourceGroupDto> getResourceGroup(@PathVariable UUID id) {
        return ResponseEntity.ok(resourceGroupMapper.toDto(resourceGroupService.getResourceGroup(id)));
    }

    @GetMapping("/assigned")
    public ResponseEntity<List<ResourceGroupDto>> getAssignedStatefulResourceGroups() {
        return ResponseEntity.ok(
                resourceGroupMapper.toDtos(
                        resourceGroupService.getAssignedStatefulResourceGroups().stream()
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResourceGroup(@PathVariable UUID id) {
        resourceGroupService.deleteResourceGroup(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @ApiResponse(responseCode = "200", description = "Resource group updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid resource group data", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))})
    public ResponseEntity<ResourceGroupDto> updateResourceGroup(@PathVariable UUID id, @RequestBody @Validated UpdateResourceGroupDto resourceGroupDto) {
        ResourceGroup resourceGroup = resourceGroupMapper.toEntity(resourceGroupDto);
        return ResponseEntity.ok(
                resourceGroupMapper.toDto(resourceGroupService.updateResourceGroup(id, resourceGroup))
        );
    }
}
