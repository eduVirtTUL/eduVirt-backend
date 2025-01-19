package pl.lodz.p.it.eduvirt.exceptions.team;

import pl.lodz.p.it.eduvirt.exceptions.general.ConflictException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class TeamSizeException extends ConflictException {

    public TeamSizeException() {
        super("Team has not been found", I18n.TEAM_NOT_FOUND);
    }
}
