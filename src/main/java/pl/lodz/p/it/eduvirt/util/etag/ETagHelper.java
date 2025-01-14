package pl.lodz.p.it.eduvirt.util.etag;

import pl.lodz.p.it.eduvirt.entity.Updatable;

public interface ETagHelper {
    String generateEtag(Updatable entity);

    boolean validateEtag(String etag, Updatable entity);
}
