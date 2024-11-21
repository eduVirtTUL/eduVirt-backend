package pl.lodz.p.it.eduvirt.controller;

import lombok.RequiredArgsConstructor;
import org.ovirt.engine.sdk4.Connection;
import org.ovirt.engine.sdk4.types.Vm;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.dto.VmDto;
import pl.lodz.p.it.eduvirt.mappers.VmMapper;
import pl.lodz.p.it.eduvirt.service.OVirtVmService;
import pl.lodz.p.it.eduvirt.util.connection.ConnectionFactory;

import java.util.List;

@RestController
@LoggerInterceptor
@RequestMapping("/resource/vm")
@RequiredArgsConstructor
public class VmController {

    private final ConnectionFactory connectionFactory;
    private final VmMapper vmMapper;
    private final OVirtVmService oVirtVmService;

    @GetMapping
    public ResponseEntity<List<VmDto>> getVms() {
        try (Connection connection = connectionFactory.getConnection()) {
            List<Vm> vms = connection
                    .systemService()
                    .vmsService()
                    .list()
                    .send()
                    .vms();
            return ResponseEntity.ok(vmMapper.ovirtVmsToDtos(vms.stream()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<VmDto> getVm(@PathVariable String id) {
        Vm vm = oVirtVmService.findVmById(id);
        return ResponseEntity.ok(vmMapper.ovirtVmToDto(vm));
    }
}
