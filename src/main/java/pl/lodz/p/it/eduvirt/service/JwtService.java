package pl.lodz.p.it.eduvirt.service;

import pl.lodz.p.it.eduvirt.entity.User;

import java.util.List;

public interface JwtService {
    String generateToken(User user, List<String> roles);
}
