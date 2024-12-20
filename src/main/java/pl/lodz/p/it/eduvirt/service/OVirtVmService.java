package pl.lodz.p.it.eduvirt.service;

import org.ovirt.engine.sdk4.types.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface OVirtVmService {

    List<Statistic> findStatisticsByVm(Vm vm);
    Map<String, Object> findVmResources(Vm vm, Host host, Cluster cluster);

    Vm findVmById(String id);

    List<Nic> findNicsByVmId(String id);
}
