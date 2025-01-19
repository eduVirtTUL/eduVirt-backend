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
    private String clientId;
    private String tokenUrl;
    private String clientSecret;
    private String loginUrl;
    private String logoutUrl;
    private String loginRedirectUrl;
    private String logoutRedirectUrl;
}
