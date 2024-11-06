package pl.lodz.p.it.eduvirt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import pl.lodz.p.it.eduvirt.configuration.KeycloackConfig;

@SpringBootApplication
@EnableConfigurationProperties(KeycloackConfig.class)
public class EduVirtApplication {

    public static void main(String[] args) {
        SpringApplication.run(EduVirtApplication.class, args);
    }
}
