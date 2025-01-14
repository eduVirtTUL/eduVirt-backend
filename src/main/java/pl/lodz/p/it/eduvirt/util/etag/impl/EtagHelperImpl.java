package pl.lodz.p.it.eduvirt.util.etag.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.lodz.p.it.eduvirt.exceptions.general.InternalServerException;
import pl.lodz.p.it.eduvirt.exceptions.general.PreconditionFailed;
import pl.lodz.p.it.eduvirt.util.etag.ETagHelper;
import pl.lodz.p.it.eduvirt.util.etag.EtagPayload;

import java.text.ParseException;
import java.util.Map;

@Component
public class EtagHelperImpl implements ETagHelper {

    @Value("${jws.secret}")
    private String secret;

    @Override
    public String generateEtag(EtagPayload payload) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = objectMapper.convertValue(payload, new TypeReference<>() {
        });

        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.HS256), new Payload(map));

        try {
            jwsObject.sign(new MACSigner(secret));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new InternalServerException();
        }
    }

    @Override
    public boolean validateEtag(String etag, EtagPayload payload) {
        try {
            JWSObject jwsObject = JWSObject.parse(etag);
            JWSVerifier verifier = new MACVerifier(secret);

            boolean isValid = jwsObject.verify(verifier);
            if (!isValid) {
                return false;
            }

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> jwsPayload = jwsObject.getPayload().toJSONObject();
            EtagPayload etagPayload = objectMapper.convertValue(jwsPayload, new TypeReference<>() {
            });
            return payload.equals(etagPayload);

        } catch (ParseException e) {
            throw new PreconditionFailed();
        } catch (JOSEException e) {
            throw new InternalServerException();
        }
    }
}
