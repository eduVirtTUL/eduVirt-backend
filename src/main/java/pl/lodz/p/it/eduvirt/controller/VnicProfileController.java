package pl.lodz.p.it.eduvirt.controller;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.lodz.p.it.eduvirt.dto.vnic_profile.VnicProfileDto;
import pl.lodz.p.it.eduvirt.dto.vnic_profile.VnicProfilePoolMemberDto;
import pl.lodz.p.it.eduvirt.entity.network.VnicProfilePoolMember;
import pl.lodz.p.it.eduvirt.exceptions.BadRequestEduVirtException;
import pl.lodz.p.it.eduvirt.exceptions.VnicProfileAlreadyExistsException;
import pl.lodz.p.it.eduvirt.exceptions.general.ConflictException;
import pl.lodz.p.it.eduvirt.exceptions.handle.ExceptionResponse;
import pl.lodz.p.it.eduvirt.exceptions.VnicProfileEduvirtNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.VnicProfileOvirtNotFoundException;
import pl.lodz.p.it.eduvirt.mappers.VnicProfileMapper;
import pl.lodz.p.it.eduvirt.service.ovirt.OVirtVnicProfileService;
import pl.lodz.p.it.eduvirt.service.VnicProfilePoolService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//TODO michal: pageable?

@RestController
@RequestMapping("/resources/vnic-profiles")
@PreAuthorize("hasAuthority('administrator')")
@RequiredArgsConstructor
@Transactional(propagation = Propagation.NEVER)
public class VnicProfileController {

    private final OVirtVnicProfileService oVirtVnicProfileService;
    private final VnicProfilePoolService vnicProfileService;
    private final VnicProfileMapper vnicProfileMapper;

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('administrator')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = VnicProfileDto.class)))}),
            @ApiResponse(responseCode = "204", content = {@Content(schema = @Schema(implementation = Void.class))}),
            @ApiResponse(responseCode = "500", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))})}
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResponseEntity<List<VnicProfileDto>> getSynchronizedVnicProfiles(@PageableDefault(sort = "vlanid", direction = Sort.Direction.ASC) Pageable pageable) {
        List<VnicProfileDto> vnicProfileDtoList = new ArrayList<>();
        vnicProfileService.getSynchronizedVnicProfiles(pageable)
                .forEach((key, value) -> value.forEach(
                        vnicProfile -> vnicProfileDtoList.add(vnicProfileMapper.ovirtVnicProfileToDto(vnicProfile, key))
                ));

        //TODO przez mapę jest popsute sortowanie -> pilnie do naprawienia
//        vnicProfileDtoList.sort();

        if (vnicProfileDtoList.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(vnicProfileDtoList);
    }

    @GetMapping(path = "/ovirt", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('administrator')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = VnicProfileDto.class)))}),
            @ApiResponse(responseCode = "204", content = {@Content(schema = @Schema(implementation = Void.class))}),
            @ApiResponse(responseCode = "500", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))})}
    )
    public ResponseEntity<List<VnicProfileDto>> getOvirtVnicProfiles(@PageableDefault(sort = "vlanid", direction = Sort.Direction.ASC) Pageable pageable) {
        List<VnicProfileDto> vnicProfileDtoList = oVirtVnicProfileService.getVnicProfiles(pageable).stream()
                .map(vnicProfile -> vnicProfileMapper.ovirtVnicProfileToDto(vnicProfile, null))
                .toList();

        if (vnicProfileDtoList.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(vnicProfileDtoList);
    }

    @GetMapping(path = "/eduvirt", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('administrator')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = VnicProfilePoolMemberDto.class)))}),
            @ApiResponse(responseCode = "204", content = {@Content(schema = @Schema(implementation = Void.class))}),
            @ApiResponse(responseCode = "500", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))})}
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResponseEntity<List<VnicProfilePoolMemberDto>> getVnicProfilesFromPool() {
        //todo pageable
//        List<VnicProfilePoolMemberDto> vnicProfileDtoList = vnicProfileService.getVnicProfilesPool()
//                .stream()
//                .map(vnicProfileMapper::vnicProfileToDto)
//                .toList();
//
//        if (vnicProfileDtoList.isEmpty()) return ResponseEntity.noContent().build();
//        return ResponseEntity.ok(vnicProfileDtoList);

        return ResponseEntity.noContent().build();
    }

    @GetMapping(path = "/eduvirt/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('administrator')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = VnicProfilePoolMemberDto.class))}),
            @ApiResponse(responseCode = "404", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))}),
            @ApiResponse(responseCode = "500", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))})}
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResponseEntity<VnicProfilePoolMemberDto> getVnicProfileFromPool(@PathVariable("id") UUID id) {
        VnicProfilePoolMember vnicProfileFromPool = vnicProfileService.getVnicProfileFromPool(id);

        return ResponseEntity.ok(vnicProfileMapper.vnicProfileToDto(vnicProfileFromPool));
    }

    @PostMapping(path = "/eduvirt/add-to-pool/{vnicProfileId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('administrator')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = VnicProfilePoolMemberDto.class))}),
            @ApiResponse(responseCode = "400", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))}),
            @ApiResponse(responseCode = "409", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))}),
            @ApiResponse(responseCode = "500", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))})}
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResponseEntity<VnicProfilePoolMemberDto> extendVnicProfilesPool(@PathVariable("vnicProfileId") UUID vnicProfileId) {
        VnicProfilePoolMember vnicProfile;
        try {
            vnicProfile = vnicProfileService.addVnicProfileToPool(vnicProfileId);
        } catch (VnicProfileOvirtNotFoundException e) {
            throw new BadRequestEduVirtException(e);
        } catch (VnicProfileAlreadyExistsException e2) {
            throw new ConflictException(e2);
        }

        return ResponseEntity.ok(vnicProfileMapper.vnicProfileToDto(vnicProfile));
    }

    @DeleteMapping(path = "/eduvirt/remove-from-pool/{vnicProfileId}")
    @PreAuthorize("hasAuthority('administrator')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", content = {@Content(schema = @Schema(implementation = Void.class))}),
            @ApiResponse(responseCode = "400", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))}),
            @ApiResponse(responseCode = "500", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))})}
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResponseEntity<Void> reduceVnicProfilesPool(@PathVariable("vnicProfileId") UUID vnicProfileId) {
        try {
            vnicProfileService.removeVnicProfileFromPool(vnicProfileId);
        } catch (VnicProfileEduvirtNotFoundException e) {
            throw new BadRequestEduVirtException(e);
        }

        return ResponseEntity.noContent().build();
    }
}
