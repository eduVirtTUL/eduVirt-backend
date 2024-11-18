package pl.lodz.p.it.eduvirt.util.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Base64;
import java.util.Optional;

@Slf4j
public class JwtHelper {
    private JwtHelper() {
    }

    public static String extractTokenPayload(String token) {
        String[] chunks = token.split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();

        return new String(decoder.decode(chunks[1]));
    }

    public static Optional<AccessToken> parseToken(String token) {
        String payload = extractTokenPayload(token);
        ObjectMapper mapper = new ObjectMapper();
        try {
            return Optional.of(mapper.readValue(payload, AccessToken.class));
        } catch (JsonProcessingException e) {
            log.error("Error while parsing token", e);
            return Optional.empty();
        }
    }
}
