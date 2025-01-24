package pl.lodz.p.it.eduvirt.util;

import lombok.RequiredArgsConstructor;
import org.ovirt.engine.sdk4.types.Cluster;
import org.ovirt.engine.sdk4.types.Host;
import org.ovirt.engine.sdk4.types.Qos;
import org.ovirt.engine.sdk4.types.Vm;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.entity.ResourceGroup;
import pl.lodz.p.it.eduvirt.entity.VirtualMachine;
import pl.lodz.p.it.eduvirt.entity.Reservation;
import pl.lodz.p.it.eduvirt.service.ovirt.OVirtVmService;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class BankerAlgorithm {

    /* Services */

    private final OVirtVmService vmService;

    public boolean process(Supplier<Map<String, Object>> metricValuesSupplier,
                           List<Reservation> reservationList, ResourceGroup resourceGroup,
                           Cluster cluster, List<Host> hosts) {
        Map<String, Object> metricValues = metricValuesSupplier.get();

        int cpuCount = (int) Math.floor((double) metricValues.get("cpu_count"));
        long memorySize = (long) Math.floor((double) metricValues.get("memory_size"));
        int networkCount = (int) Math.floor((double) metricValues.get("network_count"));

        int requiredCpus = 0;
        long requiredMemory = 0;
        int requiredNetworks = 0;

        List<ResourceGroup> resourceGroups = new LinkedList<>();
        resourceGroups.add(resourceGroup);
        reservationList.forEach(reservation -> resourceGroups.add(reservation.getResourceGroup()));

        if (hosts.isEmpty()) return false;

        for (ResourceGroup reservationRg : resourceGroups) {
            List<VirtualMachine> vms = reservationRg.getVms();

            for (VirtualMachine vm : vms) {
                Vm oVirtVM = vmService.findVmWithCpuProfileById(vm.getId().toString());
                Qos vmsQos = null;
                if (oVirtVM.cpuProfile().qos() != null) {
                    vmsQos = vmService.findQosForVmCpu(oVirtVM);
                }

                Map<String, Object> resources = vmService.findVmResources(
                        oVirtVM, vmsQos, hosts.getFirst(), cluster
                );

                requiredCpus += (int) resources.get("cpu");
                requiredMemory += (long) resources.get("memory");
            }

            requiredNetworks += reservationRg.getNetworks().size();
        }

        return (cpuCount == 0 || cpuCount >= requiredCpus)
                && (memorySize == 0 || memorySize >= requiredMemory)
                && (networkCount == 0 || networkCount >= requiredNetworks);
    }
}
