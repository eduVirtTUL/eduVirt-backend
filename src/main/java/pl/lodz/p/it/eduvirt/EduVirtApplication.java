package pl.lodz.p.it.eduvirt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import pl.lodz.p.it.eduvirt.configuration.KeycloackConfig;

@SpringBootApplication(exclude = {
        ThymeleafAutoConfiguration.class
})
@EnableConfigurationProperties(KeycloackConfig.class)
@EnableCaching
public class EduVirtApplication {

    public static void main(String[] args) {
        SpringApplication.run(EduVirtApplication.class, args);
    }
}
