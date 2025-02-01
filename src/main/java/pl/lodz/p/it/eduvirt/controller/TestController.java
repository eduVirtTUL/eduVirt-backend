package pl.lodz.p.it.eduvirt.controller;

import lombok.RequiredArgsConstructor;
import org.ovirt.engine.sdk4.Connection;
import org.ovirt.engine.sdk4.builders.DataCenterBuilder;
import org.ovirt.engine.sdk4.builders.NetworkBuilder;
import org.ovirt.engine.sdk4.builders.VlanBuilder;
import org.ovirt.engine.sdk4.services.SystemService;
import org.ovirt.engine.sdk4.types.NetworkUsage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.dto.vnic_profile.VnicProfileDto;
import pl.lodz.p.it.eduvirt.entity.VirtualMachine;
import pl.lodz.p.it.eduvirt.executor.entity.tasks.ExecutorTask;
import pl.lodz.p.it.eduvirt.executor.service.ExecutorTaskService;
import pl.lodz.p.it.eduvirt.mappers.VnicProfileMapper;
import pl.lodz.p.it.eduvirt.repository.VnicProfileRepository;
import pl.lodz.p.it.eduvirt.service.ovirt.OVirtVnicProfileService;
import pl.lodz.p.it.eduvirt.util.MailHelper;
import pl.lodz.p.it.eduvirt.util.MailProvider;
import pl.lodz.p.it.eduvirt.util.connection.ConnectionFactory;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final ConnectionFactory connectionFactory;

    private final MailHelper mailHelper;
    private final MailProvider mailProvider;

    private final OVirtVnicProfileService oVirtVnicProfileService;
    private final VnicProfileMapper vnicProfileMapper;

    private final ExecutorTaskService executorTaskService;

    @GetMapping
    public ResponseEntity<?> test(JwtAuthenticationToken auth) {
        var test = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok("Test successful!");
    }

    @PostMapping(path = "/send-simple-mail/{mail-to}")
    public ResponseEntity<Void> sendSimpleMail(@PathVariable("mail-to") String mailTo) {
        mailHelper.sendEmail(
                "Test mailing - eduVirt",
                "Greetings from eduVirt Team :*!",
                mailTo
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/send-html-mail/{mail-to}")
    public ResponseEntity<Void> sendHtmlMail(@PathVariable("mail-to") String mailTo) {
        String firstName = "FirstName";
        String lastName = "LastName";

        mailProvider.sendHtmlTestMessage(
                firstName, lastName, mailTo, null,null
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/create-networks")
    public ResponseEntity<Void> createNetworks() {
        try (Connection connection = connectionFactory.getConnection()) {
            SystemService systemService = connection.systemService();

            for (int i = 2000; i < 3000; i++) {
                systemService
                        .networksService()
                        .add()
                        .network(
                                new NetworkBuilder()
                                        .name("testNetworkNo" + i)
                                        .vlan(new VlanBuilder().id(i))
                                        .dataCenter(new DataCenterBuilder().name("Default"))
                                        .usages(NetworkUsage.VM)
                        ).send();
            }

        } catch (Throwable e) {
            e.printStackTrace(System.err);
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping(path = "/vnic-profiles-test")
    public ResponseEntity<?> testVnicProfilesPagination(@PageableDefault Pageable pageable) {
        List<VnicProfileDto> vnicProfileDtoList = oVirtVnicProfileService.getVnicProfiles(pageable).stream()
                .map(vnicProfileMapper::ovirtVnicProfileToDto)
                .toList();

        if (vnicProfileDtoList.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(vnicProfileDtoList);
    }

    private final VnicProfileRepository vnicProfileRepository;

    @GetMapping(path = "/test-fetch/{id}")
    public ResponseEntity<?> testFetch(@PathVariable UUID id) {
        List<ExecutorTask> list = executorTaskService.getReservationsToEndTasks();

        if (list.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(list.stream().filter(executorTask -> executorTask.getReservation().getId().equals(id)).findFirst().get().getReservation().getResourceGroup().getVms().stream().map(VirtualMachine::getId).toList());
    }
}
