package pl.lodz.p.it.eduvirt.exceptions;

import java.util.UUID;

public class TeamNotFoundException extends RuntimeException {
    public TeamNotFoundException(UUID teamId) {
        super("Team not found with id: " + teamId);
    }
}
