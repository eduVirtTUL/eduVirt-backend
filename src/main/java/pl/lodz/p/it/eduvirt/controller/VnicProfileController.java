package pl.lodz.p.it.eduvirt.controller;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.lodz.p.it.eduvirt.dto.vnic_profile.VnicProfileDto;
import pl.lodz.p.it.eduvirt.dto.vnic_profile.VnicProfilePoolMemberDto;
import pl.lodz.p.it.eduvirt.entity.eduvirt.network.VnicProfilePoolMember;
import pl.lodz.p.it.eduvirt.exceptions.handle.ExceptionResponse;
import pl.lodz.p.it.eduvirt.mappers.VnicProfileMapper;
import pl.lodz.p.it.eduvirt.service.OVirtVnicProfileService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/resources/vnic-profiles")
@RequiredArgsConstructor
public class VnicProfileController {

    private final OVirtVnicProfileService vnicProfileService;
    private final VnicProfileMapper vnicProfileMapper;

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = VnicProfileDto.class)))}),
            @ApiResponse(responseCode = "204", content = {@Content(schema = @Schema(implementation = Void.class))}),
            @ApiResponse(responseCode = "500", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))})}
    )
    public ResponseEntity<List<VnicProfileDto>> getSynchronizedVnicProfiles() {
        List<VnicProfileDto> vnicProfileDtoList = new ArrayList<>();
        vnicProfileService.getSynchronizedVnicProfiles()
                .forEach((key, value) -> value.forEach(
                        vnicProfile -> vnicProfileDtoList.add(vnicProfileMapper.ovirtVnicProfileToDto(vnicProfile, key))
                ));


        if (vnicProfileDtoList.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(vnicProfileDtoList);
    }

    @GetMapping(path = "/ovirt", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = VnicProfileDto.class)))}),
            @ApiResponse(responseCode = "204", content = {@Content(schema = @Schema(implementation = Void.class))}),
            @ApiResponse(responseCode = "500", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))})}
    )
    public ResponseEntity<List<VnicProfileDto>> getOvirtVnicProfiles() {
        List<VnicProfileDto> vnicProfileDtoList = vnicProfileService.fetchOVirtVnicProfiles().stream()
                .map(vnicProfile -> vnicProfileMapper.ovirtVnicProfileToDto(vnicProfile, null))
                .toList();

        if (vnicProfileDtoList.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(vnicProfileDtoList);
    }

    @GetMapping(path = "/eduvirt", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = VnicProfilePoolMemberDto.class)))}),
            @ApiResponse(responseCode = "204", content = {@Content(schema = @Schema(implementation = Void.class))}),
            @ApiResponse(responseCode = "500", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))})}
    )
    public ResponseEntity<List<VnicProfilePoolMemberDto>> getVnicProfilesPool() {
        List<VnicProfilePoolMemberDto> vnicProfileDtoList = vnicProfileService.getVnicProfilesPool()
                .stream()
                .map(vnicProfileMapper::vnicProfileToDto)
                .toList();

        if (vnicProfileDtoList.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(vnicProfileDtoList);
    }

    @PostMapping(path = "/eduvirt/add-to-pool/{vnicProfileId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = VnicProfilePoolMemberDto.class))}),
            @ApiResponse(responseCode = "409", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))}),
            @ApiResponse(responseCode = "500", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))})}
    )
    public ResponseEntity<VnicProfilePoolMemberDto> extendVnicProfilesPool(@PathVariable("vnicProfileId") UUID vnicProfileId) {
        VnicProfilePoolMember vnicProfile = vnicProfileService.addVnicProfileToPool(vnicProfileId);

        return ResponseEntity.ok(vnicProfileMapper.vnicProfileToDto(vnicProfile));
    }

    @DeleteMapping(path = "/eduvirt/remove-from-pool/{vnicProfileId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", content = {@Content(schema = @Schema(implementation = Void.class))}),
            @ApiResponse(responseCode = "404", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))}),
            @ApiResponse(responseCode = "500", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))})}
    )
    public ResponseEntity<Void> reduceVnicProfilesPool(@PathVariable("vnicProfileId") UUID vnicProfileId) {
        vnicProfileService.removeVnicProfileFromPool(vnicProfileId);

        return ResponseEntity.noContent().build();
    }
}
