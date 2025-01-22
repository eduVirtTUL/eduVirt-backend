package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.eduvirt.entity.Team;
import pl.lodz.p.it.eduvirt.entity.User;
import pl.lodz.p.it.eduvirt.exceptions.user.UserNotFoundException;
import pl.lodz.p.it.eduvirt.repository.UserRepository;
import pl.lodz.p.it.eduvirt.service.AuthService;
import pl.lodz.p.it.eduvirt.service.ovirt.OVirtUserService;
import pl.lodz.p.it.eduvirt.util.jwt.AccessToken;
import pl.lodz.p.it.eduvirt.util.jwt.JwtHelper;

import java.util.ArrayList;
import java.util.List;
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
        UUID userId = UUID.fromString(actualToken.getSub());
        Optional<User> user = userRepository.findById(userId);

        if (user.isEmpty()) {
            UUID oVirtUserId = UUID.fromString(oVirtUserService.getUserByPrincipal(actualToken.getPreferredUsername()).id());
            List<Team> emptyTeams = new ArrayList<>();

            User newUser = new User(userId,
                    oVirtUserId,
                    actualToken.getEmail(),
                    actualToken.getPreferredUsername(),
                    actualToken.getGivenName(),
                    actualToken.getFamilyName(),
                    parseGroups(actualToken.getGroups()),
                    emptyTeams);

            userRepository.saveAndFlush(newUser);
        } else {
            User actualUser = userRepository.findByIdWithRoles(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            boolean needsUpdate = false;

            if (actualToken.getEmail() != null && !actualToken.getEmail().equals(actualUser.getEmail())) {
                actualUser.setEmail(actualToken.getEmail());
                needsUpdate = true;
            }
            if (actualToken.getPreferredUsername() != null && !actualToken.getPreferredUsername().equals(actualUser.getUserName())) {
                actualUser.setUserName(actualToken.getPreferredUsername());
                needsUpdate = true;
            }
            if (actualToken.getGivenName() != null && !actualToken.getGivenName().equals(actualUser.getFirstName())) {
                actualUser.setFirstName(actualToken.getGivenName());
                needsUpdate = true;
            }
            if (actualToken.getFamilyName() != null && !actualToken.getFamilyName().equals(actualUser.getLastName())) {
                actualUser.setLastName(actualToken.getFamilyName());
                needsUpdate = true;
            }
            if (actualToken.getGroups() != null) {
                actualUser.setRoles(parseGroups(actualToken.getGroups()));
                needsUpdate = true;
            }

            if (needsUpdate) {
                userRepository.saveAndFlush(actualUser);
            }
        }
    }

    private List<String> parseGroups(List<String> groups) {
        var roles = groups.stream().map(group -> switch (group) {
                    case "/teacher" -> "teacher";
                    case "/student" -> "student";
                    case "/ovirt-administrator" -> "administrator";
                    default -> "user";
                }).filter(role -> !role.equals("user"))
                .toList();

        if (roles.isEmpty()) {
            return List.of("student");
        }

        return roles;
    }
}
