package pl.lodz.p.it.eduvirt.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.query.Param;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import pl.lodz.p.it.eduvirt.configuration.KeycloackConfig;
import pl.lodz.p.it.eduvirt.entity.User;
import pl.lodz.p.it.eduvirt.exceptions.user.UserNotFoundException;
import pl.lodz.p.it.eduvirt.model.OAuthResult;
import pl.lodz.p.it.eduvirt.repository.UserRepository;
import pl.lodz.p.it.eduvirt.service.AuthService;

import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class LoginController {

    @Value("${frontend.callback}")
    private String frontendCallback;

    @Value("${frontend.login}")
    private String frontendLogin;

    @Value("${frontend.base-path}")
    private String frontentBasePath;

    private final RestClient restClient;
    private final KeycloackConfig keycloackConfig;
    private final AuthService authService;
    private final UserRepository userRepository;

    @GetMapping("/login")
    public void login(HttpServletResponse httpServletResponse) {
        String uri = UriComponentsBuilder.fromUriString(keycloackConfig.getLoginUrl())
                .queryParam("response_type", "code")
                .queryParam("scope", "openid")
                .queryParam("client_id", keycloackConfig.getClientId())
                .queryParam("redirect_uri", keycloackConfig.getLoginRedirectUrl())
                .build().toString();

        httpServletResponse.setHeader("Location", uri);
        httpServletResponse.setStatus(302);
    }

    @GetMapping("/login/callback")
    public void loginCallback(@Param("code") String code, HttpServletResponse httpServletResponse) {
        MultiValueMap<String, String> values = new LinkedMultiValueMap<>();
        values.add("grant_type", "authorization_code");
        values.add("client_id", keycloackConfig.getClientId());
        values.add("client_secret", keycloackConfig.getClientSecret());
        values.add("code", code);
        values.add("redirect_uri", keycloackConfig.getLoginRedirectUrl());

        ResponseEntity<OAuthResult> result = restClient
                .post()
                .uri(keycloackConfig.getTokenUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(values)
                .retrieve()
                .toEntity(OAuthResult.class);

        log.error("{}, {}", keycloackConfig.getTokenUrl(), values);

        if (result.getBody() == null) {
            log.error("Error while logging in");
            httpServletResponse.setStatus(500);
            return;
        }


        try {
            authService.loginWithExternalToken(result.getBody().getAccessToken());
        } catch (UserNotFoundException e) {
            httpServletResponse.setHeader("Location", frontentBasePath + "/loginNotFound");
            httpServletResponse.setStatus(302);
            return;
        }

        httpServletResponse.setHeader("Location", frontendCallback);
        Cookie cookie = new Cookie("access_token", result.getBody().getAccessToken());
        cookie.setPath("/");
        httpServletResponse.addCookie(cookie);
        httpServletResponse.setStatus(302);
    }

    @GetMapping("/logout")
    public void logout(HttpServletResponse httpServletResponse) {
        String uri = UriComponentsBuilder.fromUriString(keycloackConfig.getLogoutUrl())
                .queryParam("response_type", "code")
                .queryParam("scope", "openid")
                .queryParam("client_id", keycloackConfig.getClientId())
                .queryParam("redirect_uri", keycloackConfig.getLogoutRedirectUrl())
                .build().toString();

        httpServletResponse.setHeader("Location", uri);
        httpServletResponse.setStatus(302);
    }

    @GetMapping("/logout/callback")
    public void logoutCallback(HttpServletResponse httpServletResponse) {
        httpServletResponse.setHeader("Location", frontendLogin);
        httpServletResponse.setStatus(302);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping(path = "/update-timezone-and-language")
    public ResponseEntity<Void> setLanguageAndTimeZone(
            @RequestParam(name = "timezone", defaultValue = "UTC") String timeZone,
            @RequestParam(name = "language", defaultValue = "en") String language) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        user.setTimeZone(timeZone);
        user.setLanguage(language);
        userRepository.save(user);

        return ResponseEntity.noContent().build();
    }
}
