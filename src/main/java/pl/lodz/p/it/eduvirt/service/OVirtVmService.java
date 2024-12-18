package pl.lodz.p.it.eduvirt.service;

import org.ovirt.engine.sdk4.types.Nic;
import org.ovirt.engine.sdk4.types.Statistic;
import org.ovirt.engine.sdk4.types.Vm;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface OVirtVmService {

    List<Statistic> findStatisticsByVm(Vm vm);
    Map<String, Object> findVmResources(Vm vm, Host host, Cluster cluster);

    Vm findVmById(String id);

    List<Nic> findNicsByVmId(String id);

    List<Vm> findVms();

    boolean runVm(String id);

    boolean shutdownVm(String id);

    boolean powerOffVm(String id);

    boolean assignVnicProfileToVm(String vmId, String vmNicId, String vnicProfileId);

    String removeVnicProfileFromVm(String vmId, String vmNicId);
}
