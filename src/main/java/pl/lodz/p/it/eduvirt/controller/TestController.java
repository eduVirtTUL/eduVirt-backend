package pl.lodz.p.it.eduvirt.controller;

import lombok.RequiredArgsConstructor;
import org.ovirt.engine.sdk4.Connection;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.util.connection.ConnectionFactory;

@RestController
@LoggerInterceptor
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final ConnectionFactory connectionFactory;

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
}
