package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.eduvirt.entity.User;
import pl.lodz.p.it.eduvirt.repository.UserRepository;
import pl.lodz.p.it.eduvirt.service.AuthService;
import pl.lodz.p.it.eduvirt.service.OVirtUserService;
import pl.lodz.p.it.eduvirt.util.jwt.AccessToken;
import pl.lodz.p.it.eduvirt.util.jwt.JwtHelper;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final OVirtUserService oVirtUserService;

    @Override
    public void loginWithExternalToken(String externalToken) {
        Optional<AccessToken> accessToken = JwtHelper.parseToken(externalToken);
        if (accessToken.isEmpty()) {
            throw new IllegalArgumentException("Invalid token");
        }

        AccessToken actualToken = accessToken.get();
        UUID userId = UUID.fromString(accessToken.get().getSub());
        Optional<User> user = userRepository.findById(userId);

        if (user.isEmpty()) {
            UUID oVirtUserId = UUID.fromString(oVirtUserService.getUserByPrincipal(actualToken.getPreferredUsername()).id());

            User newUser = new User(userId,
                    oVirtUserId,
                    actualToken.getEmail(),
                    actualToken.getPreferredUsername(),
                    actualToken.getGivenName(),
                    actualToken.getFamilyName(),
                    actualToken.getGroups(),
                    null);

            userRepository.saveAndFlush(newUser);

        } else {
            User actualUser = user.get();
            if (!actualUser.getEmail().equals(actualToken.getEmail())) {
                actualUser.setEmail(actualToken.getEmail());
                userRepository.saveAndFlush(actualUser);
            }
            if (!actualUser.getFirstName().equals(actualToken.getGivenName())) {
                actualUser.setFirstName(actualToken.getGivenName());
                userRepository.saveAndFlush(actualUser);
            }
            if (!actualUser.getLastName().equals(actualToken.getFamilyName())) {
                actualUser.setLastName(actualToken.getFamilyName());
                userRepository.saveAndFlush(actualUser);
            }
            if (!actualUser.getRoles().equals(actualToken.getGroups())) {
                actualUser.setRoles(actualToken.getGroups());
                userRepository.saveAndFlush(actualUser);
            }
        }
    }
}
