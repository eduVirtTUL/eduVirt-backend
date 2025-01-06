package pl.lodz.p.it.eduvirt.controller;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.dto.resource_group.CreateResourceGroupDto;
import pl.lodz.p.it.eduvirt.dto.resource_group_pool.CreateRGPoolDto;
import pl.lodz.p.it.eduvirt.dto.resource_group_pool.DetailedResourceGroupPoolDto;
import pl.lodz.p.it.eduvirt.dto.resource_group_pool.ResourceGroupPoolDto;
import pl.lodz.p.it.eduvirt.dto.resource_group_pool.UpdateResourceGroupPoolDto;
import pl.lodz.p.it.eduvirt.entity.ResourceGroup;
import pl.lodz.p.it.eduvirt.entity.ResourceGroupPool;
import pl.lodz.p.it.eduvirt.mappers.RGPoolMapper;
import pl.lodz.p.it.eduvirt.mappers.ResourceGroupMapper;
import pl.lodz.p.it.eduvirt.service.ResourceGroupPoolService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/resource-group-pool")
@RequiredArgsConstructor
public class ResourceGroupPoolController {
    private final ResourceGroupPoolService resourceGroupPoolService;
    private final RGPoolMapper rgPoolMapper;
    private final ResourceGroupMapper resourceGroupMapper;

    @PostMapping
    public ResponseEntity<ResourceGroupPoolDto> createResourceGroupPool(@RequestBody @Validated CreateRGPoolDto createRGPoolDto) {
        ResourceGroupPool resourceGroupPool = rgPoolMapper.toRGPool(createRGPoolDto);
        resourceGroupPoolService.addResourceGroupPool(resourceGroupPool, createRGPoolDto.courseId());
        return ResponseEntity.ok(rgPoolMapper.toRGPoolDto(resourceGroupPool));
    }

    @GetMapping("/{id}")
    @Transactional
    public ResponseEntity<DetailedResourceGroupPoolDto> getResourceGroupPool(@PathVariable UUID id) {
        return ResponseEntity.ok(rgPoolMapper.toDetailedRGPoolDto(resourceGroupPoolService.getResourceGroupPool(id)));
    }

    @GetMapping
    @Transactional
    @ApiResponse(responseCode = "200", description = "Returns list of resource group pools")
    public ResponseEntity<List<DetailedResourceGroupPoolDto>> getResourceGroupPools() {
        List<ResourceGroupPool> resourceGroupPools = resourceGroupPoolService.getResourceGroupPools();
        return ResponseEntity.ok(rgPoolMapper.toDetailedRGPoolDtoList(resourceGroupPools.stream()));
    }

    @PostMapping("/{id}/resourceGroup")
    public ResponseEntity<Void> addResourceGroupToPool(@PathVariable UUID id, @RequestBody CreateResourceGroupDto createResourceGroupDto) {
        ResourceGroup resourceGroup = resourceGroupMapper.toEntity(createResourceGroupDto);
        resourceGroupPoolService.addResourceGroupToPool(id, resourceGroup);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResourceGroupPoolDto> updateResourceGroupPool(@PathVariable UUID id, @RequestBody UpdateResourceGroupPoolDto updateResourceGroupPoolDto) {
        ResourceGroupPool resourceGroupPool = rgPoolMapper.toRGPool(updateResourceGroupPoolDto);
        return ResponseEntity.ok(rgPoolMapper.toRGPoolDto(resourceGroupPoolService.updateResourceGroupPool(id, resourceGroupPool)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResourceGroupPool(@PathVariable UUID id) {
        resourceGroupPoolService.deleteResourceGroupPool(id);
        return ResponseEntity.ok().build();
    }
}

