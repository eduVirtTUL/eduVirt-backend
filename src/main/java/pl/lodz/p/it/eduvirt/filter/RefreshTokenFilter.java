package pl.lodz.p.it.eduvirt.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import pl.lodz.p.it.eduvirt.configuration.KeycloackConfig;
import pl.lodz.p.it.eduvirt.model.OAuthResult;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenFilter implements Filter {

    private final RestClient restClient;
    private final KeycloackConfig keycloackConfig;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse res = (HttpServletResponse) servletResponse;
        String refreshToken = req.getHeader("X-Refresh-Token");
        if (refreshToken != null) {
            MultiValueMap<String, String> values = new LinkedMultiValueMap<>();
            values.add("grant_type", "refresh_token");
            values.add("refresh_token", refreshToken);
            values.add("client_id", keycloackConfig.getClientId());
            values.add("client_secret", keycloackConfig.getClientSecret());

            ResponseEntity<OAuthResult> response = restClient
                    .post()
                    .uri(keycloackConfig.getTokenUrl())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(values)
                    .retrieve()
                    .toEntity(OAuthResult.class);

            if (response.getBody() == null) {
                res.setStatus(500);
                return;
            }

            res.setHeader("X-Access-Token", response.getBody().getAccessToken());
            res.setHeader("X-Refresh-Token", response.getBody().getRefreshToken());
        }
        
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
