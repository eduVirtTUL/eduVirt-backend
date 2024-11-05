package pl.lodz.p.it.eduvirt.controller;

import lombok.RequiredArgsConstructor;
import org.ovirt.engine.sdk4.Connection;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.dto.VmDto;
import pl.lodz.p.it.eduvirt.mappers.VmMapper;
import pl.lodz.p.it.eduvirt.util.connection.ConnectionFactory;

import java.util.stream.Stream;

@RestController
@LoggerInterceptor
@RequestMapping("/resources/vms")
@RequiredArgsConstructor
public class VmController {

    private final ConnectionFactory connectionFactory;
    private final VmMapper vmMapper;

    @GetMapping
    public ResponseEntity<?> getVms() {
        try (Connection connection = connectionFactory.getConnection()) {
            Stream<VmDto> vms = connection
                    .systemService()
                    .vmsService()
                    .list()
                    .send()
                    .vms()
                    .stream()
                    .map(vmMapper::ovirtVmToDto);
            return ResponseEntity.ok(vms);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
