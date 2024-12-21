package pl.lodz.p.it.eduvirt.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ovirt.engine.sdk4.types.Vm;
import org.ovirt.engine.sdk4.types.VnicProfile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.dto.nic.NicDto;
import pl.lodz.p.it.eduvirt.dto.vm.VmDto;
import pl.lodz.p.it.eduvirt.mappers.VmMapper;
import pl.lodz.p.it.eduvirt.service.OVirtVmService;
import pl.lodz.p.it.eduvirt.service.OVirtVnicProfileService;

import java.util.List;

@Slf4j
@RestController
@LoggerInterceptor
@RequestMapping("/resource/vm")
@RequiredArgsConstructor
public class VmController {
    private final VmMapper vmMapper;
    private final OVirtVmService oVirtVmService;
    private final OVirtVnicProfileService oVirtVnicProfileService;

    @GetMapping
    public ResponseEntity<List<VmDto>> getVms() {
        return ResponseEntity.ok(vmMapper.ovirtVmsToDtos(oVirtVmService.findVms().stream()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VmDto> getVm(@PathVariable String id) {
        Vm vm = oVirtVmService.findVmById(id);
        VmDto vmDto = vmMapper.ovirtVmToDto(vm);
        vmDto.setNics(
                vm.nics().parallelStream().map(nic -> {
                    NicDto.NicDtoBuilder nicDtoBuilder = NicDto.builder()
                            .id(nic.id())
                            .name(nic.name())
                            .macAddress(nic.mac().address());
                    if (nic.vnicProfilePresent()) {
                        VnicProfile profile = oVirtVnicProfileService.getVnicProfileById(nic.vnicProfile().id());
                        return nicDtoBuilder
                                .profileName(profile.name())
                                .build();
                    }

                    return nicDtoBuilder
                            .build();

                }).toList());
        return ResponseEntity.ok(vmDto);
    }
}
