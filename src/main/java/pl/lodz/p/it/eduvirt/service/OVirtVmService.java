package pl.lodz.p.it.eduvirt.service;

import org.ovirt.engine.sdk4.types.Nic;
import org.ovirt.engine.sdk4.types.Statistic;
import org.ovirt.engine.sdk4.types.Vm;

import java.util.List;

public interface OVirtVmService {

    List<Statistic> findStatisticsByVm(Vm vm);

    Vm findVmById(String id);

    List<Nic> findNicsByVmId(String id);
}
