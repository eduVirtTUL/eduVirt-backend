package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.NotFoundException;
import pl.lodz.p.it.eduvirt.util.I18n;

import java.util.UUID;

public class TeamNotFoundException extends NotFoundException {

    public TeamNotFoundException(String message) {
        super(message, I18n.TEAM_NOT_FOUND);
    }

    public TeamNotFoundException(UUID teamId) {
        super("Team with id %s could not be found".formatted(teamId), I18n.TEAM_NOT_FOUND);
    }
}
