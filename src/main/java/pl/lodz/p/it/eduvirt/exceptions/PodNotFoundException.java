package pl.lodz.p.it.eduvirt.exceptions;

import java.util.UUID;

public class PodNotFoundException extends RuntimeException {
    public PodNotFoundException(UUID podId) {
        super("Pod not found with id: " + podId);
    }
}
