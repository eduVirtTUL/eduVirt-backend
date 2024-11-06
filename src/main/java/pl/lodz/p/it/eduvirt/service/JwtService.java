package pl.lodz.p.it.eduvirt.service;

import java.util.UUID;

public interface JwtService {
    String generateToken(UUID id);
}
