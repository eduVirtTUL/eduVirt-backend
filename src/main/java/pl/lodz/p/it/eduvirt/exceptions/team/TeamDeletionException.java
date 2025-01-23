package pl.lodz.p.it.eduvirt.exceptions.team;

import pl.lodz.p.it.eduvirt.exceptions.general.ApplicationBaseException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class TeamDeletionException extends ApplicationBaseException {
    public TeamDeletionException(String message) {
        super(message, I18n.TEAM_DELETION);
    }
}
