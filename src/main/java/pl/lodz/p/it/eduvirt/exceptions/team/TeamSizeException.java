package pl.lodz.p.it.eduvirt.exceptions.team;

import pl.lodz.p.it.eduvirt.util.I18n;

public class TeamSizeException extends TeamValidationException {

    public TeamNotFoundException() {
        super("Team has not been found", I18n.TEAM_NOT_FOUND);
    }
}
