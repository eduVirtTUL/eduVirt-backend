
package pl.lodz.p.it.eduvirt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.lodz.p.it.eduvirt.dto.VnicProfileDto;
import pl.lodz.p.it.eduvirt.dto.VnicProfilePoolDto;
import pl.lodz.p.it.eduvirt.service.OVirtVnicProfileService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/resources/vnic-profiles")
@RequiredArgsConstructor
public class VnicProfileController {

    private final OVirtVnicProfileService vnicProfileService;

    @GetMapping(path = "/ovirt", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getFetchedVnicProfiles() {
        List<VnicProfileDto> vnicProfileDtoList = vnicProfileService.fetchVnicProfiles();

        if (vnicProfileDtoList.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(vnicProfileDtoList);
    }

    @GetMapping(path = "/eduvirt", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getVnicProfilesPool() {
        List<VnicProfilePoolDto> vnicProfileDtoList = vnicProfileService.showVnicProfilePool();

        if (vnicProfileDtoList.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(vnicProfileDtoList);
    }

    @PostMapping(path = "/eduvirt/add-to-pool/{vnicProfileId}")
    public ResponseEntity<?> addVnicProfileToPool(@PathVariable("vnicProfileId") String vnicProfileId) {
        //todo: handle mapping exception
        boolean success = vnicProfileService.expandVnicProfilePool(UUID.fromString(vnicProfileId));

        if (success) return ResponseEntity.noContent().build();
        return ResponseEntity.badRequest().build();
    }
}
