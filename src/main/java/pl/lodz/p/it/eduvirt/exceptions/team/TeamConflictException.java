package pl.lodz.p.it.eduvirt.exceptions.team;

import pl.lodz.p.it.eduvirt.exceptions.general.ConflictException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class TeamConflictException extends ConflictException {
    public TeamConflictException() {
        super("Team optimistic lock conflict", I18n.TEAM_CONFLICT);
    }
}