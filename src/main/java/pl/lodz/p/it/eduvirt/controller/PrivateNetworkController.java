package pl.lodz.p.it.eduvirt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.dto.resource_group_network.NetworkVmConnectionDto;
import pl.lodz.p.it.eduvirt.service.ResourceGroupNetworkService;

import java.util.UUID;

@RestController
@RequestMapping("/network")
@RequiredArgsConstructor
public class PrivateNetworkController {
    private final ResourceGroupNetworkService resourceGroupNetworkService;

    @PostMapping("/{id}/attach")
    public ResponseEntity<Void> attachNicToNetwork(@PathVariable UUID id,
                                                   @RequestBody NetworkVmConnectionDto dto,
                                                   @RequestHeader(HttpHeaders.IF_MATCH) String etag) {
        resourceGroupNetworkService.attachNicToNetwork(id, dto.vmId(), dto.nicId(), etag);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/detach")
    public ResponseEntity<Void> detachNicFromNetwork(@RequestBody NetworkVmConnectionDto dto,
                                                     @RequestHeader(HttpHeaders.IF_MATCH) String etag) {
        resourceGroupNetworkService.detachNicFromNetwork(dto.vmId(), dto.nicId(), etag);
        return ResponseEntity.ok().build();
    }
}
