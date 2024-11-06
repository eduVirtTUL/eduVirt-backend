package pl.lodz.p.it.eduvirt.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Base64;

public class JwtHelper {
    public static String extractTokenPayload(String token) {
        String[] chunks = token.split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();

        return new String(decoder.decode(chunks[1]));
    }

    public static String extractPayloadField(String token, String key) {
        String payload = extractTokenPayload(token);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node;
        try {
            node = mapper.readTree(payload);
            return node.get(key).asText();
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
