package pl.lodz.p.it.eduvirt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.lodz.p.it.eduvirt.dto.vnic_profile.OvirtVnicProfileDto;
import pl.lodz.p.it.eduvirt.dto.vnic_profile.VnicProfilePoolMemberDto;
import pl.lodz.p.it.eduvirt.entity.eduvirt.network.VnicProfilePoolMember;
import pl.lodz.p.it.eduvirt.mappers.VnicProfileMapper;
import pl.lodz.p.it.eduvirt.service.OVirtVnicProfileService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/resources/vnic-profiles")
@RequiredArgsConstructor
public class VnicProfileController {

    private final OVirtVnicProfileService vnicProfileService;
    private final VnicProfileMapper vnicProfileMapper;

    @GetMapping(path = "/ovirt", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<OvirtVnicProfileDto>> showOvirtVnicProfiles() {
        List<OvirtVnicProfileDto> vnicProfileDtoList = vnicProfileService.fetchVnicProfiles();

        if (vnicProfileDtoList.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(vnicProfileDtoList);
    }

    @GetMapping(path = "/eduvirt", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<VnicProfilePoolMemberDto>> showVnicProfilesPool() {
        List<VnicProfilePoolMemberDto> vnicProfileDtoList = vnicProfileService.getVnicProfilesPool()
                .stream()
                .map(vnicProfileMapper::vnicProfileToDto)
                .toList();

        if (vnicProfileDtoList.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(vnicProfileDtoList);
    }

    @PostMapping(path = "/eduvirt/add-to-pool/{vnicProfileId}")
    public ResponseEntity<VnicProfilePoolMemberDto> extendVnicProfilesPool(@PathVariable("vnicProfileId") UUID vnicProfileId) {
        VnicProfilePoolMember vnicProfile = vnicProfileService.addVnicProfileToPool(vnicProfileId);

        return ResponseEntity.ok(vnicProfileMapper.vnicProfileToDto(vnicProfile));
    }

    @DeleteMapping(path = "/eduvirt/remove-from-pool/{vnicProfileId}")
    public ResponseEntity<Void> reduceVnicProfilesPool(@PathVariable("vnicProfileId") UUID vnicProfileId) {
        vnicProfileService.removeVnicProfileFromPool(vnicProfileId);

        return ResponseEntity.noContent().build();
    }
}
