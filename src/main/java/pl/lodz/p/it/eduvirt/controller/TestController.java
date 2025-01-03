package pl.lodz.p.it.eduvirt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
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
    public ResponseEntity<?> test(JwtAuthenticationToken auth) {
        return ResponseEntity.ok("Test successful!");
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
