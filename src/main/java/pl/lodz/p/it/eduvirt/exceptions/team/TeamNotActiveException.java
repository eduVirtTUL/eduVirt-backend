package pl.lodz.p.it.eduvirt.exceptions.team;

import pl.lodz.p.it.eduvirt.exceptions.general.BadRequestException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class TeamNotActiveException extends BadRequestException {
    public TeamNotActiveException() {
        super("Team is not active", I18n.TEAM_NOT_ACTIVE);
    }
}
