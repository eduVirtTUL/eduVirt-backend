import org.junit.jupiter.api.Test;
import org.ovirt.engine.sdk4.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pl.lodz.p.it.eduvirt.EduVirtApplication;
import pl.lodz.p.it.eduvirt.util.connection.ConnectionFactory;

@SpringBootTest(classes = EduVirtApplication.class)
public class _FakeTest {

    @Autowired
    private ConnectionFactory connectionFactory;

    @Test
    void getVmsNics() {
        try (Connection connection = connectionFactory.getConnection()) {
            connection
                    .systemService()
                    .vmsService()
                    .list()
                    .follow("nics")
                    .send()
                    .vms()
                    .forEach(vm -> System.out.printf("VM(%s) - %s: %n\t%s%n",
                                    vm.name(),
                                    vm.id(),
                                    String.join("\n\t", vm.nics().stream().map(nic -> "%s - %s".formatted(nic.name(), nic.id())).toList())
                            )
                    );
        } catch (Throwable e) {
            e.printStackTrace(System.err);
        }
    }

    @Test
    void getUsers() {
        try (Connection connection = connectionFactory.getConnection()) {
            connection
                    .systemService()
                    .usersService()
                    .list()
                    .send()
                    .users()
                    .forEach(user -> System.out.printf("User(%12s) - %s%n", user.principal(), user.id()));
        } catch (Throwable e) {
            e.printStackTrace(System.err);
        }
    }
}