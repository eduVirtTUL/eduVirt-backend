package pl.lodz.p.it.eduvirt.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "auth.keycloack")
public class KeycloackConfig {
    private String url;
    private String clientId;
    private String redirectUri;
    private String tokenUri;
    private String clientSecret;
}
