package pl.lodz.p.it.eduvirt.controller;

import lombok.RequiredArgsConstructor;
import org.ovirt.engine.sdk4.Connection;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.util.MailHelper;
import pl.lodz.p.it.eduvirt.util.connection.ConnectionFactory;

@RestController
@LoggerInterceptor
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final ConnectionFactory connectionFactory;
    private final MailHelper mailHelper;

    @GetMapping
    public ResponseEntity<?> test() {
        try (Connection connection = connectionFactory.getConnection()) {
            var users = connection
                    .systemService()
                    .usersService()
                    .list()
                    .send()
                    .users();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping(path = "/send-simple-mail/{mail-to}")
    public ResponseEntity<?> sendSimpleMail(@PathVariable("mail-to") String mailTo) {
        mailHelper.sendSimpleMail(
                mailTo,
                "Test mailing - eduVirt",
                "Greetings from eduVirt Team :*!"
        );
        return ResponseEntity.ok("Mail sent successfully!");
    }
}
