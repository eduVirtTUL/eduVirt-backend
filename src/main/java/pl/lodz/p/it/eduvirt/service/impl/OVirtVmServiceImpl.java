package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ovirt.engine.sdk4.Connection;
import org.ovirt.engine.sdk4.internal.containers.NicContainer;
import org.ovirt.engine.sdk4.internal.containers.VnicProfileContainer;
import org.ovirt.engine.sdk4.services.SystemService;
import org.ovirt.engine.sdk4.services.VmService;
import org.ovirt.engine.sdk4.services.VmsService;
import org.ovirt.engine.sdk4.types.Nic;
import org.ovirt.engine.sdk4.types.Statistic;
import org.ovirt.engine.sdk4.types.Vm;
import org.ovirt.engine.sdk4.types.VnicProfile;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.entity.VirtualMachine;
import pl.lodz.p.it.eduvirt.repository.VirtualMachineRepository;
import pl.lodz.p.it.eduvirt.service.OVirtVmService;
import pl.lodz.p.it.eduvirt.util.StatisticsUtil;
import pl.lodz.p.it.eduvirt.util.connection.ConnectionFactory;

import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.TreeMap;
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
    public Map<String, Object> findVmResources(Vm vm, Host host, Cluster cluster) {
        Connection connection = connectionFactory.getConnection();
        CpuProfile vmCpuProfile = connection.followLink(vm.cpuProfile());

        int cpuCount;
        if (vmCpuProfile.qos() != null) {
            Qos vmCpuProfileQos = connection.followLink(vmCpuProfile.qos());
            int hostCpuCount = StatisticsUtil.getNumberOfCpus(host, cluster).intValue();
            double cpuLimit = vmCpuProfileQos.cpuLimit().intValue() / 100.0;
            cpuCount = (int) Math.ceil(cpuLimit * hostCpuCount);
        } else {
            CpuTopology topology = vm.cpu().topology();
            cpuCount = topology.sockets().multiply(topology.cores()).multiply(topology.threads()).intValue();
        }

        Map<String, Object> resources = new TreeMap<>();
        resources.put("cpu", cpuCount);
        resources.put("memory", vm.memory().longValue());
        return resources;
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

    @Override
    public boolean runVm(String id) {
        try (Connection connection = connectionFactory.getConnection()) {
            connection
                    .systemService()
                    .vmsService()
                    .vmService(id)
                    .start()
                    .send();
            return true;
        } catch (Throwable e) {
            log.error(e.getMessage());
            //TODO michal: if VM is started lets restart it!
            return false;
        }
    }

    @Override
    public boolean shutdownVm(String id) {
        try (Connection connection = connectionFactory.getConnection()) {
            connection
                    .systemService()
                    .vmsService()
                    .vmService(id)
                    .shutdown()
                    .send();
            return true;
        } catch (Throwable e) {
            log.error(e.getMessage());
            return false;
        }
    }

    @Override
    public boolean powerOffVm(String id) {
        try (Connection connection = connectionFactory.getConnection()) {
            connection
                    .systemService()
                    .vmsService()
                    .vmService(id)
                    .stop()
                    .send();
            return true;
        } catch (Throwable e) {
            log.error(e.getMessage());
            return false;
        }
    }

    @Override
    public boolean assignVnicProfileToVm(String vmId, String vmNicId, String vnicProfileId) {
        try (Connection connection = connectionFactory.getConnection()) {
            SystemService systemService = connection
                    .systemService();

            VmService vmService = systemService
                    .vmsService()
                    .vmService(vmId);

            Vm fetchedVm = vmService
                    .get()
                    .follow("nics")
                    .send()
                    .vm();

            Nic wantedNic = fetchedVm.nics()
                    .stream()
                    .filter(nic -> nic.id().equals(vmNicId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("NIC not found in the VM fetched object"));

            VnicProfile wantedVnicProfile = systemService
                    .vnicProfilesService()
                    .profileService(vnicProfileId)
                    .get()
                    .send()
                    .profile();

            ((NicContainer) wantedNic).vnicProfile(wantedVnicProfile);

            vmService
                    .nicsService()
                    .nicService(wantedNic.id())
                    .update()
                    .nic(wantedNic)
                    .send();

            return true;
        } catch (Throwable e) {
            log.error(e.getMessage());
            return false;
        }
    }

    @Override
    public String removeVnicProfileFromVm(String vmId, String vmNicId) {
        try (Connection connection = connectionFactory.getConnection()) {
            SystemService systemService = connection
                    .systemService();

            VmService vmService = systemService
                    .vmsService()
                    .vmService(vmId);

            Vm fetchedVm = vmService
                    .get()
                    .follow("nics")
                    .send()
                    .vm();

            Nic wantedNic = fetchedVm.nics()
                    .stream()
                    .filter(nic -> nic.id().equals(vmNicId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("NIC not found in the VM fetched object"));

            String vnicProfileToRemoveId = wantedNic.vnicProfile().id();

            ((NicContainer) wantedNic).vnicProfile(new VnicProfileContainer());

            vmService
                    .nicsService()
                    .nicService(wantedNic.id())
                    .update()
                    .nic(wantedNic)
                    .send();

            return vnicProfileToRemoveId;
        } catch (Throwable e) {
            log.error(e.getMessage());
            return null;
        }
    }
}
