package pl.lodz.p.it.eduvirt.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.dto.resource_group.ResourceGroupDto;
import pl.lodz.p.it.eduvirt.dto.resource_group.UpdateResourceGroupDto;
import pl.lodz.p.it.eduvirt.entity.ResourceGroup;
import pl.lodz.p.it.eduvirt.exceptions.handle.ExceptionResponse;
import pl.lodz.p.it.eduvirt.mappers.ResourceGroupMapper;
import pl.lodz.p.it.eduvirt.service.ResourceGroupService;
import pl.lodz.p.it.eduvirt.util.etag.ETagHelper;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/resource-group")
@RequiredArgsConstructor
public class ResourceGroupController {

    private final ResourceGroupService resourceGroupService;
    private final ResourceGroupMapper resourceGroupMapper;
    private final ETagHelper eTagHelper;

    @GetMapping
    @PreAuthorize("hasAuthority('administrator')")
    public ResponseEntity<List<ResourceGroupDto>> getResourceGroups(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size,
            @RequestParam(name = "search", required = false) String search
    ) {


        return ResponseEntity.ok(resourceGroupMapper.toDtos(resourceGroupService.getResourceGroups().stream()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('teacher', 'administrator')")
    public ResponseEntity<ResourceGroupDto> getResourceGroup(@PathVariable UUID id) {
        ResourceGroup resourceGroup = resourceGroupService.getResourceGroup(id);
        String etag = eTagHelper.generateEtag(resourceGroup);
        return ResponseEntity.ok().eTag(etag).body(resourceGroupMapper.toDto(resourceGroup));
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
    @PreAuthorize("hasAuthority('teacher')")
    public ResponseEntity<Void> deleteResourceGroup(@PathVariable UUID id) {
        resourceGroupService.deleteResourceGroup(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @ApiResponse(responseCode = "200", description = "Resource group updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid resource group data", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))})
    @PreAuthorize("hasAuthority('teacher')")
    public ResponseEntity<ResourceGroupDto> updateResourceGroup(@PathVariable UUID id,
                                                                @RequestBody @Validated UpdateResourceGroupDto resourceGroupDto,
                                                                @RequestHeader(HttpHeaders.IF_MATCH) String ifMatch) {
        ResourceGroup resourceGroup = resourceGroupMapper.toEntity(resourceGroupDto);
        return ResponseEntity.ok(
                resourceGroupMapper.toDto(resourceGroupService.updateResourceGroup(id, resourceGroup, ifMatch))
        );
    }
}
