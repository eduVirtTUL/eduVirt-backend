package pl.lodz.p.it.eduvirt.service;

import org.ovirt.engine.sdk4.types.Statistic;
import org.ovirt.engine.sdk4.types.Vm;

import java.util.List;

public interface IVmService {

    List<Statistic> findStatisticsByVm(Vm vm);
}
