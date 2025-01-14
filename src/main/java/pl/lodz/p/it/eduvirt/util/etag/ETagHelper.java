package pl.lodz.p.it.eduvirt.util.etag;

public interface ETagHelper {
    String generateEtag(EtagPayload payload);

    boolean validateEtag(String etag, EtagPayload payload);
}
