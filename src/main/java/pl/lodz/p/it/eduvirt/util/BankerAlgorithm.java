package pl.lodz.p.it.eduvirt.util;

import lombok.RequiredArgsConstructor;
import org.ovirt.engine.sdk4.types.Cluster;
import org.ovirt.engine.sdk4.types.Host;
import org.ovirt.engine.sdk4.types.Vm;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.entity.ResourceGroup;
import pl.lodz.p.it.eduvirt.entity.VirtualMachine;
import pl.lodz.p.it.eduvirt.entity.reservation.Reservation;
import pl.lodz.p.it.eduvirt.service.OVirtClusterService;
import pl.lodz.p.it.eduvirt.service.OVirtVmService;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class BankerAlgorithm {

    /* Services */

    private final OVirtVmService vmService;
    private final OVirtClusterService clusterService;

    public boolean process(Supplier<Map<String, Object>> metricValuesSupplier,
                            List<Reservation> reservationList, Cluster cluster) {
        Map<String, Object> metricValues = metricValuesSupplier.get();

        int cpuCount = (int) Math.floor((double) metricValues.get("cpu_count"));
        long memorySize = (long) Math.floor((double) metricValues.get("memory_size"));
        int networkCount = (int) Math.floor((double) metricValues.get("network_count"));

        int requiredCpus = 0;
        long requiredMemory = 0;
        int requiredNetworks = 0;

        for (Reservation reservation : reservationList) {
            ResourceGroup resourceGroup = reservation.getResourceGroup();
            List<VirtualMachine> vms = resourceGroup.getVms();

            for (VirtualMachine vm : vms) {
                Vm oVirtVM = vmService.findVmById(vm.getId().toString());
                List<Host> oVirtHosts = clusterService.findAllHostsInCluster(cluster);
                Map<String, Object> resources = vmService.findVmResources(oVirtVM, oVirtHosts.getFirst(), cluster);

                requiredCpus += (int) resources.get("cpu");
                requiredMemory += (long) resources.get("memory");
            }

            requiredNetworks += resourceGroup.getNetworks().size();
        }

        return (cpuCount == 0 || cpuCount >= requiredCpus)
                && (memorySize == 0 || memorySize >= requiredMemory)
                && (networkCount == 0 || networkCount >= requiredNetworks);
    }
}
