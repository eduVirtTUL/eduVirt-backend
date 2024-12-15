package pl.lodz.p.it.eduvirt.controller;

import lombok.RequiredArgsConstructor;
import org.ovirt.engine.sdk4.types.Permission;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.dto.permission.OvirtUserPermissionDto;
import pl.lodz.p.it.eduvirt.dto.permission.VmPermissionDto;
import pl.lodz.p.it.eduvirt.mappers.PermissionMapper;
import pl.lodz.p.it.eduvirt.service.OVirtPermissionService;

import java.util.List;
import java.util.UUID;

@RestController
@LoggerInterceptor
@RequestMapping("/resources/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final OVirtPermissionService oVirtPermissionService;
    private final PermissionMapper permissionMapper;

    @GetMapping(path = "/vm/{vmId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findPermissionByVmId(@PathVariable("vmId") UUID vmId) {
        List<Permission> foundPermissions = oVirtPermissionService.findPermissionsByVmId(vmId);
        List<VmPermissionDto> permissionDtos = foundPermissions.stream()
                .filter(permission -> permission.user() != null)
                .map(permissionMapper::ovirtVmPermissionToPermissionDto)
                .toList();

        if (permissionDtos.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(permissionDtos);
    }

    @GetMapping(path = "/user/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findPermissionByUserId(@PathVariable("userId") UUID userId) {
        List<Permission> foundPermissions = oVirtPermissionService.findPermissionsByUserId(userId);
        List<OvirtUserPermissionDto> permissionDtos = foundPermissions.stream()
                .filter(permission -> permission.vm() != null)
                .map(permissionMapper::ovirtUserPermissionToPermissionDto)
                .toList();

        if (permissionDtos.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(permissionDtos);
    }

    @PostMapping(path = "/vm/{vmId}/user/{userId}")
    public ResponseEntity<?> assignPermissionToVmToUser(@PathVariable("vmId") UUID vmId, @PathVariable("userId") UUID userId) {
        try {
            oVirtPermissionService.assignPermissionToVmToUser(vmId, userId, "00000000-0000-0000-0001-000000000001"); // id for UserRole (or ENGINE_USER)
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping(path = "/{permissionId}")
    public ResponseEntity<?> revokePermissionToVmFromUser(@PathVariable("permissionId") UUID permissionId) {
        try {
            oVirtPermissionService.revokePermissionToVmFromUser(permissionId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
