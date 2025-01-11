package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.eduvirt.entity.User;
import pl.lodz.p.it.eduvirt.repository.UserRepository;
import pl.lodz.p.it.eduvirt.service.AuthService;
import pl.lodz.p.it.eduvirt.util.jwt.AccessToken;
import pl.lodz.p.it.eduvirt.util.jwt.JwtHelper;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

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
            User newUser = new User(userId, actualToken.getEmail());
            userRepository.save(newUser);
        } else {
            User actualUser = user.get();
            if (!actualUser.getEmail().equals(actualToken.getEmail())) {
                actualUser.setEmail(actualToken.getEmail());
                userRepository.save(actualUser);
            }
        }
    }
}
