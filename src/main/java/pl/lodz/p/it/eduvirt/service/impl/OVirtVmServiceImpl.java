package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ovirt.engine.sdk4.Connection;
import org.ovirt.engine.sdk4.internal.containers.NicContainer;
import org.ovirt.engine.sdk4.internal.containers.VnicProfileContainer;
import org.ovirt.engine.sdk4.services.SystemService;
import org.ovirt.engine.sdk4.services.VmService;
import org.ovirt.engine.sdk4.types.CpuTopology;
import org.ovirt.engine.sdk4.types.Event;
import org.ovirt.engine.sdk4.types.Nic;
import org.ovirt.engine.sdk4.types.Qos;
import org.ovirt.engine.sdk4.types.Statistic;
import org.ovirt.engine.sdk4.types.Vm;
import org.ovirt.engine.sdk4.types.VnicProfile;
import org.ovirt.engine.sdk4.types.Host;
import org.ovirt.engine.sdk4.types.Cluster;
import org.ovirt.engine.sdk4.services.EventsService;
import org.ovirt.engine.sdk4.services.VmsService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.entity.VirtualMachine;
import pl.lodz.p.it.eduvirt.exceptions.EventNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.VmNotFoundException;
import pl.lodz.p.it.eduvirt.repository.VirtualMachineRepository;
import pl.lodz.p.it.eduvirt.service.OVirtVmService;
import pl.lodz.p.it.eduvirt.util.StatisticsUtil;
import pl.lodz.p.it.eduvirt.util.connection.ConnectionFactory;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@LoggerInterceptor
@RequiredArgsConstructor
public class OVirtVmServiceImpl implements OVirtVmService {

    private final ConnectionFactory connectionFactory;
    private final VirtualMachineRepository virtualMachineRepository;

    @PreAuthorize("isAuthenticated()")
    @Override
    public List<Statistic> findStatisticsByVm(Vm vm) {
        Connection connection = connectionFactory.getConnection();
        return connection.followLink(vm.statistics());
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public Map<String, Object> findVmResources(Vm vm, Qos qos, Host host, Cluster cluster) {
        int cpuCount;
        if (qos != null && host != null && cluster != null) {
            int hostCpuCount = StatisticsUtil.getNumberOfCpus(host, cluster).intValue();
            double cpuLimit = qos.cpuLimit().intValue() / 100.0;
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

    @PreAuthorize("isAuthenticated()")
    @Cacheable(value = "vms", key = "#id")
    @Override
    public Vm findVmWithCpuProfileById(String id) {
        try (Connection connection = connectionFactory.getConnection()) {
            return connection
                    .systemService()
                    .vmsService()
                    .vmService(id)
                    .get()
                    .follow("cpu_profile")
                    .send()
                    .vm();
        } catch (Exception e) {
            throw new VmNotFoundException(
                    "VM %s could not be found".formatted(id));
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Cacheable(value = "qos", key = "#vm.id()")
    @Override
    public Qos findQosForVmCpu(Vm vm) {
        try (Connection connection = connectionFactory.getConnection()) {
            return connection.followLink(vm.cpuProfile().qos());
        } catch (Exception e) {
            throw new VmNotFoundException("No VM could be found!");
        }
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
    public List<Vm> findVmsForCluster(Cluster cluster) {
        try (Connection connection = connectionFactory.getConnection()) {
            SystemService systemService = connection.systemService();
            VmsService vmsService = systemService.vmsService();

            String searchQuery = "cluster=%s".formatted(cluster.name());

            return vmsService.list().search(searchQuery).follow("cpu_profile").send().vms();
        } catch (Exception e) {
            throw new VmNotFoundException("No VM could be found!");
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
    public List<Event> findEventsByVmId(Vm vm, Pageable pageable) {
        try (Connection connection = connectionFactory.getConnection()) {
            SystemService systemService = connection.systemService();
            EventsService eventsService = systemService.eventsService();

            String sortBy = "";
            for (Sort.Order sortOrder : pageable.getSort()) {
                sortBy = " sortby %s %s".formatted(sortOrder.getProperty(), sortOrder.getDirection());
                break;
            }

            String searchQuery = "vm=%s%s page %s".formatted(vm.name(), sortBy, pageable.getPageNumber() + 1);
            return eventsService.list().search(searchQuery).max(pageable.getPageSize()).send().events();
        } catch (Exception exception) {
            throw new EventNotFoundException("No event could be found for vm %s".formatted(vm.id()));
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
    public List<Vm> findVmsWithNicsByVmIds(Set<String> vmIds) {
        try (Connection connection = connectionFactory.getConnection()) {
            String searchQuery = vmIds.stream()
                    .collect(Collectors.joining(" or id=", "id=", ""));

            return connection
                    .systemService()
                    .vmsService()
                    .list()
                    .follow("nics")
                    .search(searchQuery)
                    .send()
                    .vms();
        } catch (Throwable e) {
            log.error("Error while fetching VMs", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void runVm(String id) {
        try (Connection connection = connectionFactory.getConnection()) {
            connection
                    .systemService()
                    .vmsService()
                    .vmService(id)
                    .start()
                    .send();
        } catch (Throwable e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void shutdownVm(String id) {
        try (Connection connection = connectionFactory.getConnection()) {
            connection
                    .systemService()
                    .vmsService()
                    .vmService(id)
                    .shutdown()
                    .send();
        } catch (Throwable e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void powerOffVm(String id) {
        try (Connection connection = connectionFactory.getConnection()) {
            connection
                    .systemService()
                    .vmsService()
                    .vmService(id)
                    .stop()
                    .send();
        } catch (Throwable e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void assignVnicProfileToVm(String vmId, String vmNicId, String vnicProfileId) {
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

            // Verify that the NIC has <EMPTY> status before setting the selected vnic profile
            Optional.ofNullable(wantedNic.vnicProfile()).ifPresent(profile -> {
                throw new RuntimeException("VNIC PROFILE already assigned");
            });

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

        } catch (Throwable e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
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

            String vnicProfileToRemoveId = Optional.ofNullable(wantedNic.vnicProfile())
                    .map(VnicProfile::id)
                    .orElse(null);

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
            throw new RuntimeException(e);
        }
    }
}
