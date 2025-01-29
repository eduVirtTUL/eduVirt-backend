package pl.lodz.p.it.eduvirt.exceptions.team;

import pl.lodz.p.it.eduvirt.exceptions.general.ConflictException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class TeamSizeException extends ConflictException {

    public TeamSizeException() {
        super("Team max size cannot be exceeded", I18n.TEAM_SIZE_EXCEPTION);
    }
}
