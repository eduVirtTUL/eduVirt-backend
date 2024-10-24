package pl.lodz.p.it.eduvirt;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.sdk4.types.Vm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import pl.lodz.p.it.eduvirt.util.ConnectionWrapper;
import pl.lodz.p.it.eduvirt.util.ConnectionWrapperImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class EduVirtApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
//        assertNotNull(applicationContext);
//        ConnectionWrapper connectionWrapper = applicationContext.getBean(ConnectionWrapperImpl.class);
//        assertNotNull(connectionWrapper);
//
//        List<Vm> vms = connectionWrapper
//                .systemService()
//                .vmsService()
//                .list()
//                .send()
//                .vms();
//
//        for (Vm vm : vms) {
//            System.out.printf("VM - %s: %s - (nic1 - %s)%n", vm.id(), vm.name(),
//                    !vm.nics().isEmpty() ? String.format("%s - %s", vm.nics().getFirst().vnicProfile().id(),
//                            vm.nics().getFirst().vnicProfile().name()) : null
//            );
//        }
    }

}
