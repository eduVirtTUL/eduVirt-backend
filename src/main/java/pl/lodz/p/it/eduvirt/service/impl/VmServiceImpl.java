package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.ovirt.engine.sdk4.Connection;
import org.ovirt.engine.sdk4.types.Statistic;
import org.ovirt.engine.sdk4.types.Vm;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.service.IVmService;
import pl.lodz.p.it.eduvirt.util.connection.ConnectionFactory;

import java.util.List;

@Service
@LoggerInterceptor
@RequiredArgsConstructor
public class VmServiceImpl implements IVmService {

    private final ConnectionFactory connectionFactory;

    @Override
    public List<Statistic> findStatisticsByVm(Vm vm) {
        Connection connection = connectionFactory.getConnection();
        return connection.followLink(vm.statistics());
    }
}
