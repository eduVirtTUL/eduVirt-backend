package pl.lodz.p.it.eduvirt;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.sdk4.Connection;
import org.ovirt.engine.sdk4.builders.VmBuilder;
import org.ovirt.engine.sdk4.internal.containers.NicContainer;
import org.ovirt.engine.sdk4.internal.containers.VmContainer;
import org.ovirt.engine.sdk4.services.VmService;
import org.ovirt.engine.sdk4.services.VmsService;
import org.ovirt.engine.sdk4.types.Nic;
import org.ovirt.engine.sdk4.types.Vm;
import org.ovirt.engine.sdk4.types.VnicProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pl.lodz.p.it.eduvirt.util.connection.ConnectionFactory;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.stream.Stream;


import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = EduVirtApplication.class)
@SuppressWarnings("NewClassNamingConvention")
public class oVirtApiStartupModuleT3st {

    @Autowired
    private ConnectionFactory connectionFactory;

    @Test
    void testContext() {
        assertNotNull(connectionFactory);
    }

    // Pobranie listy dostępnych Vnic profili
    @Test
    void testFetchingVnicProfiles() {
        try (Connection connection = connectionFactory.getConnection()) {
            connection.systemService()
                    .vnicProfilesService()
                    .list()
                    .follow("network")
                    .send()
                    .profiles()
                    .forEach(
                            p -> System.out.printf("Vnic profile: %36s - %20s (Network: %36s - %20s ~ VLAN: %4s)%n",
                                    p.id(), p.name(), p.network().id(), p.network().name(), p.network().vlan().id())
                    );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Pobranie listy konkretnych maszyn wirtualnych po frazie (np uniklany prefix dla każdego pod'a)
    @Test
    void testFetchingVMsByPhrase() {
        try (Connection connection = connectionFactory.getConnection()) {
            connection.systemService()
                    .vmsService()
                    .list()
                    .search("NET")
                    .caseSensitive(false)
                    .send()
                    .vms()
                    .forEach(
                            vm -> System.out.printf("VM: %36s - %20s%n", vm.id(), vm.name())
                    );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Pobranie listy konkretnych maszyn wirtualnych po UUID {"4181e5f8-7cc6-4021-a653-b7c59f5ef16e", "ef44305e-adc6-4329-97ba-78ebaa30eb98"}
    @Test
    void testFetchingVMsByIds() {
        try (Connection connection = connectionFactory.getConnection()) {
            VmsService vmsService = connection.systemService().vmsService();
            List<String> ids = List.of("4181e5f8-7cc6-4021-a653-b7c59f5ef16e", "ef44305e-adc6-4329-97ba-78ebaa30eb98");

            ids.stream()
                    .map(
                            id -> vmsService
                                    .vmService(id)
                                    .get()
                                    .send()
                                    .vm()
                    )
                    .forEach(
                            vm -> System.out.printf("VM: %36s - %20s%n", vm.id(), vm.name())
                    );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Pobranie informacji o niezajętych zasobach CPU i RAM
//    @Test
//    void testFetchingFreeCPUsAndRAM() {
//        //TODO: ???
//    }

    // Przypisanie Vnic profilu do maszyny wirtualnej
    @Test
    void testChangingVMVnicProfile() {
        // Vnic Profiles for tests
        //Vnic profile: 8321cca4-5877-486f-826d-ac8519443b75 - vnicTest1 (Network: 9603af0a-f8d5-498f-9ce3-102e187cf7e2 - suma_pub ~ VLAN: 4032)
        //Vnic profile: 80510a79-3683-4e25-a1b9-918201538a47 - vnicTest2 (Network: 9603af0a-f8d5-498f-9ce3-102e187cf7e2 - suma_pub ~ VLAN: 4032)

        try (Connection connection = connectionFactory.getConnection()) {
            VmsService vmsService = connection.systemService().vmsService();
            VmService.GetRequest vmGetReq = vmsService
                    .vmService("4181e5f8-7cc6-4021-a653-b7c59f5ef16e")
                    .get()
                    .follow("nics");

            // Print info before update
            {
                Vm vmBefore = vmGetReq
                        .send()
                        .vm();
                System.out.printf("VM: %36s - %20s ~ %s%n",
                        vmBefore.id(), vmBefore.name(),
                        String.join(", ", vmBefore.nics().stream().map(nic -> nic.id() + " " + nic.vnicProfile().id()).toList()));

                Nic wantedNic = vmBefore.nics().stream().filter(nic -> nic.name().equals("nic2")).findFirst().get();

                VnicProfile newVnicProfile = connection.systemService()
                        .vnicProfilesService()
                        .profileService("8321cca4-5877-486f-826d-ac8519443b75")
                        .get()
                        .send()
                        .profile();

                ((NicContainer) wantedNic).vnicProfile(newVnicProfile);

                var vmUpdateReq = vmsService
                        .vmService("4181e5f8-7cc6-4021-a653-b7c59f5ef16e")
                        .nicsService()
                        .nicService(wantedNic.id())
                        .update()
                        .nic(wantedNic)
                        .send();
            }

            // Print info after update
            {
                Vm vmAfter = vmGetReq
                        .send()
                        .vm();
                System.out.printf("VM: %36s - %20s ~ %s%n",
                        vmAfter.id(), vmAfter.name(),
                        String.join(", ", vmAfter.nics().stream().map(nic -> nic.id() + " " + nic.vnicProfile().id()).toList()));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
