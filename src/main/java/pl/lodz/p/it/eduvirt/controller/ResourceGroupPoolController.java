package pl.lodz.p.it.eduvirt.controller;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.dto.resource_group_pool.CreateRGPoolDto;
import pl.lodz.p.it.eduvirt.dto.resource_group_pool.DetailedResourceGroupPoolDto;
import pl.lodz.p.it.eduvirt.dto.resource_group_pool.ResourceGroupPoolDto;
import pl.lodz.p.it.eduvirt.entity.eduvirt.ResourceGroupPool;
import pl.lodz.p.it.eduvirt.mappers.RGPoolMapper;
import pl.lodz.p.it.eduvirt.service.ResourceGroupPoolService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/resource-group-pool")
@RequiredArgsConstructor
public class ResourceGroupPoolController {
    private final ResourceGroupPoolService resourceGroupPoolService;
    private final RGPoolMapper rgPoolMapper;

    @PostMapping
    public ResponseEntity<ResourceGroupPoolDto> createResourceGroupPool(@RequestBody CreateRGPoolDto createRGPoolDto) {
        ResourceGroupPool resourceGroupPool = rgPoolMapper.toRGPool(createRGPoolDto);
        resourceGroupPoolService.addResourceGroupPool(resourceGroupPool, createRGPoolDto.courseId());
        return ResponseEntity.ok(rgPoolMapper.toRGPoolDto(resourceGroupPool));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getResourceGroupPool(@PathVariable UUID id) {
        return null;
    }

    @GetMapping
    @ApiResponse(responseCode = "200", description = "Returns list of resource group pools")
    public ResponseEntity<List<DetailedResourceGroupPoolDto>> getResourceGroupPools() {
        List<ResourceGroupPool> resourceGroupPools = resourceGroupPoolService.getResourceGroupPools();
        return ResponseEntity.ok(rgPoolMapper.toDetailedRGPoolDtoList(resourceGroupPools.stream()));
    }
}
