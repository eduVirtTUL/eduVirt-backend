
package pl.lodz.p.it.eduvirt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.lodz.p.it.eduvirt.dto.VnicProfileDto;
import pl.lodz.p.it.eduvirt.service.OVirtVnicProfileService;

import java.util.List;

@RestController
@RequestMapping("/resources/vnic-profiles")
@RequiredArgsConstructor
public class VnicProfileController {

    private final OVirtVnicProfileService vnicProfileService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getVnicProfiles() {
        List<VnicProfileDto> vnicProfileDtoList = vnicProfileService.fetchVnicProfiles();

        if (vnicProfileDtoList.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(vnicProfileDtoList);
    }
}
