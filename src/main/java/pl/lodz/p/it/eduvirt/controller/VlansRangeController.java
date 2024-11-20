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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.lodz.p.it.eduvirt.dto.vlans_range.CreateVlansRangeDto;
import pl.lodz.p.it.eduvirt.dto.vlans_range.ResizeVlansRangeDto;
import pl.lodz.p.it.eduvirt.dto.vlans_range.VlansRangeDto;
import pl.lodz.p.it.eduvirt.entity.eduvirt.network.VlansRange;
import pl.lodz.p.it.eduvirt.exceptions.handle.ExceptionResponse;
import pl.lodz.p.it.eduvirt.mappers.VlansRangeMapper;
import pl.lodz.p.it.eduvirt.service.VlansRangeService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/resources/vnic-profiles/vlans-range")
@RequiredArgsConstructor
public class VlansRangeController {

    private final VlansRangeService vlansRangeService;
    private final VlansRangeMapper vlansRangeMapper;

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = VlansRangeDto.class)))}),
            @ApiResponse(responseCode = "204", content = {@Content(schema = @Schema(implementation = Void.class))}),
            @ApiResponse(responseCode = "500", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))})}
    )
    public ResponseEntity<List<VlansRangeDto>> getVlansRanges() {
        List<VlansRangeDto> vlansRangeDtoList = vlansRangeService.getVlansRanges(true).stream()
                .map(vlansRangeMapper::vlansRangeToDto)
                .toList();

        if (vlansRangeDtoList.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(vlansRangeDtoList);
    }

    @GetMapping(path = "/{id}",  produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = VlansRangeDto.class))}),
            @ApiResponse(responseCode = "404", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))}),
            @ApiResponse(responseCode = "500", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))})}
    )
    public ResponseEntity<VlansRangeDto> getVlansRange(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(vlansRangeMapper.vlansRangeToDto(vlansRangeService.getVlansRange(id)));
    }

    @PostMapping(path = "/add", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = VlansRangeDto.class))}),
            @ApiResponse(responseCode = "409", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))}),
            @ApiResponse(responseCode = "500", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))})}
    )
    public ResponseEntity<VlansRangeDto> addVlansRange(@RequestBody CreateVlansRangeDto createDto) {
        VlansRange vlansRange = vlansRangeService.addVlansRange(vlansRangeMapper.createVlansRangeDtoToVlansRange(createDto));

        return ResponseEntity.ok(vlansRangeMapper.vlansRangeToDto(vlansRange));
    }

//    @PutMapping(path = "/{id}/resize", produces = MediaType.APPLICATION_JSON_VALUE)
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = VlansRangeDto.class))}),
//            @ApiResponse(responseCode = "409", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))}),
//            @ApiResponse(responseCode = "500", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))})}
//    )
//    public ResponseEntity<VlansRangeDto> resizeVlansRange(@PathVariable("id") UUID id, @RequestBody ResizeVlansRangeDto resizeDto) {
//        VlansRange vlansRange = vlansRangeService.resizeVlansRange(vlansRangeMapper.resizeVlansRangeDtoToVlansRange(id, resizeDto));
//
//        return ResponseEntity.ok(vlansRangeMapper.vlansRangeToDto(vlansRange));
//    }

    @DeleteMapping(path = "/{id}/remove")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", content = {@Content(schema = @Schema(implementation = Void.class))}),
            @ApiResponse(responseCode = "404", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))}),
            @ApiResponse(responseCode = "500", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))})}
    )
    public ResponseEntity<Void> removeVlansRange(@PathVariable("id") UUID id) {
        vlansRangeService.removeVlansRange(id);

        return ResponseEntity.noContent().build();
    }
}
