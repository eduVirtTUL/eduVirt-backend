package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.eduvirt.entity.eduvirt.User;
import pl.lodz.p.it.eduvirt.repository.eduvirt.UserRepository;
import pl.lodz.p.it.eduvirt.service.AuthService;
import pl.lodz.p.it.eduvirt.service.JwtService;
import pl.lodz.p.it.eduvirt.util.jwt.AccessToken;
import pl.lodz.p.it.eduvirt.util.jwt.JwtHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    public String loginWithExternalToken(String externalToken) {
        Optional<AccessToken> accessToken = JwtHelper.parseToken(externalToken);
        if (accessToken.isEmpty()) {
            return null;
        }

        UUID userId = UUID.fromString(accessToken.get().getSub());
        User user = userRepository.findById(userId).orElseGet(() -> {
            User newUser = new User(userId);
            return userRepository.save(newUser);
        });

        List<String> roles = getUserRoles(accessToken.get());

        return jwtService.generateToken(user, roles);
    }

    private List<String> getUserRoles(AccessToken token) {
        List<String> roles = new ArrayList<>();
        if (token.getGroups().contains("/ovirt-administrator")) {
            roles.add("administrator");
        }

        if (token.getGroups().contains("/teachers")) {
            roles.add("teacher");
        }

        return roles;
    }
}
