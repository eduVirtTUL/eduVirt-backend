package pl.lodz.p.it.eduvirt.executor.exception;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

public class ResourceGroupCurrentlyInUseException extends ExecutorBaseException {

    public ResourceGroupCurrentlyInUseException(UUID rgId, UUID... reservationIds) {
        super("Resource group {%s} is currently in use, as part of the ongoing reservation(s) {%s}"
                .formatted(rgId, Arrays.stream(reservationIds).map(UUID::toString).collect(Collectors.joining(" ")))
        );
    }

    public ResourceGroupCurrentlyInUseException(String message) {
        super(message);
    }
}
