package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.ovirt.engine.sdk4.Connection;
import org.ovirt.engine.sdk4.types.*;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.service.OVirtVmService;
import pl.lodz.p.it.eduvirt.util.StatisticsUtil;
import pl.lodz.p.it.eduvirt.util.connection.ConnectionFactory;

import java.util.*;

@Service
@LoggerInterceptor
@RequiredArgsConstructor
public class OVirtVmServiceImpl implements OVirtVmService {

    private final ConnectionFactory connectionFactory;

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
}
