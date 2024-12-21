package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ovirt.engine.sdk4.Connection;
import org.ovirt.engine.sdk4.types.Nic;
import org.ovirt.engine.sdk4.types.Statistic;
import org.ovirt.engine.sdk4.types.Vm;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.entity.eduvirt.VirtualMachine;
import pl.lodz.p.it.eduvirt.repository.eduvirt.VirtualMachineRepository;
import pl.lodz.p.it.eduvirt.service.OVirtVmService;
import pl.lodz.p.it.eduvirt.util.connection.ConnectionFactory;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@LoggerInterceptor
@RequiredArgsConstructor
public class OVirtVmServiceImpl implements OVirtVmService {

    private final ConnectionFactory connectionFactory;
    private final VirtualMachineRepository virtualMachineRepository;

    @Override
    public List<Statistic> findStatisticsByVm(Vm vm) {
        Connection connection = connectionFactory.getConnection();
        return connection.followLink(vm.statistics());
    }

    @Override
    public Vm findVmById(String id) {
        try (Connection connection = connectionFactory.getConnection()) {
            return connection
                    .systemService()
                    .vmsService()
                    .vmService(id)
                    .get()
                    .follow("nics")
                    .send()
                    .vm();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Nic> findNicsByVmId(String id) {
        try (Connection connection = connectionFactory.getConnection()) {
            return connection
                    .systemService()
                    .vmsService()
                    .vmService(id)
                    .get()
                    .follow("nics")
                    .send()
                    .vm()
                    .nics();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Vm> findVms() {
        List<VirtualMachine> virtualMachines = virtualMachineRepository.findAll();

        try (Connection connection = connectionFactory.getConnection()) {
            return connection
                    .systemService()
                    .vmsService()
                    .list()
                    .send()
                    .vms()
                    .stream()
                    .filter(vm -> virtualMachines.stream().noneMatch(virtualMachine -> UUID.fromString(vm.id()).equals(virtualMachine.getId()))
                    )
                    .toList();
        } catch (Exception e) {
            log.error("Error while fetching VMs", e);
            throw new RuntimeException(e);
        }
    }
}
