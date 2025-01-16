package pl.lodz.p.it.eduvirt.controller;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.dto.pagination.PageDto;
import pl.lodz.p.it.eduvirt.dto.pagination.PageInfoDto;
import pl.lodz.p.it.eduvirt.dto.resource_group.CreateResourceGroupDto;
import pl.lodz.p.it.eduvirt.dto.resource_group_pool.CreateRGPoolDto;
import pl.lodz.p.it.eduvirt.dto.resource_group_pool.DetailedResourceGroupPoolDto;
import pl.lodz.p.it.eduvirt.dto.resource_group_pool.ResourceGroupPoolDto;
import pl.lodz.p.it.eduvirt.dto.resource_group_pool.UpdateResourceGroupPoolDto;
import pl.lodz.p.it.eduvirt.dto.search.SearchDto;
import pl.lodz.p.it.eduvirt.entity.ResourceGroup;
import pl.lodz.p.it.eduvirt.entity.ResourceGroupPool;
import pl.lodz.p.it.eduvirt.mappers.RGPoolMapper;
import pl.lodz.p.it.eduvirt.mappers.ResourceGroupMapper;
import pl.lodz.p.it.eduvirt.service.ResourceGroupPoolService;
import pl.lodz.p.it.eduvirt.util.etag.ETagHelper;
import pl.lodz.p.it.eduvirt.util.search.RgPoolSpecificationBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/resource-group-pool")
@RequiredArgsConstructor
public class ResourceGroupPoolController {
    private final ResourceGroupPoolService resourceGroupPoolService;
    private final RGPoolMapper rgPoolMapper;
    private final ResourceGroupMapper resourceGroupMapper;
    private final ETagHelper eTagHelper;

    @PostMapping
    @PreAuthorize("hasAuthority('teacher')")
    public ResponseEntity<ResourceGroupPoolDto> createResourceGroupPool(@RequestBody @Validated CreateRGPoolDto createRGPoolDto) {
        ResourceGroupPool resourceGroupPool = rgPoolMapper.toRGPool(createRGPoolDto);
        resourceGroupPoolService.addResourceGroupPool(resourceGroupPool, createRGPoolDto.courseId());
        return ResponseEntity.ok(rgPoolMapper.toRGPoolDto(resourceGroupPool));
    }

    @GetMapping("/{id}")
    @Transactional
    @PreAuthorize("hasAnyAuthority('teacher', 'administrator')")
    public ResponseEntity<DetailedResourceGroupPoolDto> getResourceGroupPool(@PathVariable UUID id) {
        ResourceGroupPool pool = resourceGroupPoolService.getResourceGroupPool(id);

        String etag = eTagHelper.generateEtag(pool);

        return ResponseEntity.ok().eTag(etag).body(rgPoolMapper.toDetailedRGPoolDto(pool));
    }

    private void addDefaultSearchDtos(List<SearchDto> searchDtos, String name, UUID courseId, RgPoolSpecificationBuilder builder) {
        if (name != null) {
            searchDtos.add(new SearchDto("name", "cn", name));
        }

        if (courseId != null) {
            searchDtos.add(new SearchDto("courseId", "eq", courseId));
        }

        searchDtos.forEach(search ->
        {
            search.setDataOption("all");
            builder.with(search);
        });
    }

    @GetMapping
    @Transactional
    @ApiResponse(responseCode = "200", description = "Returns list of resource group pools")
    @PreAuthorize("hasAuthority('administrator')")
    public ResponseEntity<PageDto<DetailedResourceGroupPoolDto>> getResourceGroupPools(
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10") int size,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "courseId", required = false) UUID courseId
    ) {
        RgPoolSpecificationBuilder builder = new RgPoolSpecificationBuilder();
        List<SearchDto> searchDtos = new ArrayList<>();

        addDefaultSearchDtos(searchDtos, name, courseId, builder);

        Page<ResourceGroupPool> resourceGroupPools = resourceGroupPoolService.getResourceGroupPools(builder.build(), page, size);

        return ResponseEntity.ok(PageDto.<DetailedResourceGroupPoolDto>builder()
                .items(rgPoolMapper.toDetailedRGPoolDtoList(resourceGroupPools.getContent().stream()))
                .page(new PageInfoDto(resourceGroupPools.getNumber(), resourceGroupPools.getNumberOfElements(), resourceGroupPools.getTotalPages(), resourceGroupPools.getTotalElements()))
                .build());
    }

    @GetMapping("/teacher")
    @Transactional
    @ApiResponse(responseCode = "200", description = "Returns list of resource group pools")
    @PreAuthorize("hasAuthority('teacher')")
    public ResponseEntity<PageDto<DetailedResourceGroupPoolDto>> getResourceGroupPoolsForTeacher(
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10") int size,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "courseId", required = false) UUID courseId
    ) {
        RgPoolSpecificationBuilder builder = new RgPoolSpecificationBuilder();
        List<SearchDto> searchDtos = new ArrayList<>();

        UUID teacherId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        searchDtos.add(new SearchDto("teachers", "eq", teacherId));

        addDefaultSearchDtos(searchDtos, name, courseId, builder);

        Page<ResourceGroupPool> resourceGroupPools = resourceGroupPoolService.getResourceGroupPools(builder.build(), page, size);

        return ResponseEntity.ok(PageDto.<DetailedResourceGroupPoolDto>builder()
                .items(rgPoolMapper.toDetailedRGPoolDtoList(resourceGroupPools.getContent().stream()))
                .page(new PageInfoDto(resourceGroupPools.getNumber(), resourceGroupPools.getNumberOfElements(), resourceGroupPools.getTotalPages(), resourceGroupPools.getTotalElements()))
                .build());
    }

    @PostMapping("/{id}/resourceGroup")
    @PreAuthorize("hasAuthority('teacher')")
    public ResponseEntity<Void> addResourceGroupToPool(@PathVariable UUID id, @RequestBody CreateResourceGroupDto createResourceGroupDto) {
        ResourceGroup resourceGroup = resourceGroupMapper.toEntity(createResourceGroupDto);
        resourceGroupPoolService.addResourceGroupToPool(id, resourceGroup);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('teacher')")
    public ResponseEntity<ResourceGroupPoolDto> updateResourceGroupPool(@PathVariable UUID id,
                                                                        @RequestBody @Validated UpdateResourceGroupPoolDto updateResourceGroupPoolDto,
                                                                        @RequestHeader(HttpHeaders.IF_MATCH) String ifMatch) {
        ResourceGroupPool resourceGroupPool = rgPoolMapper.toRGPool(updateResourceGroupPoolDto);
        return ResponseEntity.ok(rgPoolMapper.toRGPoolDto(resourceGroupPoolService.updateResourceGroupPool(id, resourceGroupPool, ifMatch)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('teacher')")
    public ResponseEntity<Void> deleteResourceGroupPool(@PathVariable UUID id) {
        resourceGroupPoolService.deleteResourceGroupPool(id);
        return ResponseEntity.ok().build();
    }
}

