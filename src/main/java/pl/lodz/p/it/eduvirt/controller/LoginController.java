package pl.lodz.p.it.eduvirt.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.query.Param;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import pl.lodz.p.it.eduvirt.configuration.KeycloackConfig;
import pl.lodz.p.it.eduvirt.entity.eduvirt.User;
import pl.lodz.p.it.eduvirt.model.OAuthResult;
import pl.lodz.p.it.eduvirt.repository.eduvirt.UserRepository;
import pl.lodz.p.it.eduvirt.service.JwtService;
import pl.lodz.p.it.eduvirt.util.JwtHelper;

import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class LoginController {
    private final RestClient restClient;
    private final KeycloackConfig keycloackConfig;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @GetMapping("/login")
    public void login(HttpServletResponse httpServletResponse) {
        String uri = UriComponentsBuilder.fromHttpUrl(keycloackConfig.getUrl())
                .queryParam("response_type", "code")
                .queryParam("scope", "openid")
                .queryParam("client_id", keycloackConfig.getClientId())
                .queryParam("redirect_uri", keycloackConfig.getRedirectUri())
                .build().toString();

        httpServletResponse.setHeader("Location", uri);
        httpServletResponse.setStatus(302);
    }

    @GetMapping("/callback")
    public void loginCallback(@Param("code") String code, HttpServletResponse httpServletResponse) {
        MultiValueMap<String, String> values = new LinkedMultiValueMap<>();
        values.add("grant_type", "authorization_code");
        values.add("client_id", keycloackConfig.getClientId());
        values.add("client_secret", keycloackConfig.getClientSecret());
        values.add("code", code);
        values.add("redirect_uri", keycloackConfig.getRedirectUri());

        ResponseEntity<OAuthResult> result = restClient
                .post()
                .uri(keycloackConfig.getTokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(values)
                .retrieve()
                .toEntity(OAuthResult.class);

        if (result.getBody() == null) {
            log.error("Error while logging in");
            httpServletResponse.setStatus(500);
            return;
        }

        String userId = JwtHelper.extractPayloadField(result.getBody().getAccessToken(), "sub");

        if (userId == null) {
            log.error("Error while logging in");
            httpServletResponse.setStatus(500);
            return;
        }

        UUID id = UUID.fromString(userId);


        User user = userRepository.findById(id).orElseGet(() -> {
            User newUser = new User(id);
            return userRepository.save(newUser);
        });

        String token = jwtService.generateToken(user.getId());


        httpServletResponse.setHeader("Location", "http://localhost:5173/");
        Cookie cookie = new Cookie("access_token", token);
        cookie.setPath("/");
        httpServletResponse.addCookie(cookie);
        httpServletResponse.setStatus(302);
    }
}
