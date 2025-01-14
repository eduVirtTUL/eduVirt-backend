package pl.lodz.p.it.eduvirt.util.etag;

import pl.lodz.p.it.eduvirt.entity.Updatable;

import java.util.UUID;

public interface ETagHelper {
    String generateEtag(Updatable entity);

    String generateEtag(UUID id, long version);

    boolean validateEtag(String etag, Updatable entity);

    boolean validateEtag(String etag, UUID id, long version);
}
