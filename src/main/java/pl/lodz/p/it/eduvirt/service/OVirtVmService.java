package pl.lodz.p.it.eduvirt.service;

import org.ovirt.engine.sdk4.types.Nic;
import org.ovirt.engine.sdk4.types.Statistic;
import org.ovirt.engine.sdk4.types.Vm;
import org.ovirt.engine.sdk4.types.Host;
import org.ovirt.engine.sdk4.types.Cluster;

import java.util.List;
import java.util.Map;

public interface OVirtVmService {

    List<Statistic> findStatisticsByVm(Vm vm);

    Map<String, Object> findVmResources(Vm vm, Host host, Cluster cluster);

    Vm findVmById(String id);

    List<Nic> findNicsByVmId(String id);

    List<Vm> findVms();

    void runVm(String id);

    void shutdownVm(String id);

    void powerOffVm(String id);

    void assignVnicProfileToVm(String vmId, String vmNicId, String vnicProfileId);

    String removeVnicProfileFromVm(String vmId, String vmNicId);
}
