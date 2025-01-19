package pl.lodz.p.it.eduvirt.exceptions.team;

import pl.lodz.p.it.eduvirt.exceptions.general.BadRequestException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class TeamUserNotMemberException extends BadRequestException {
    public TeamUserNotMemberException() {
        super("User is not a member of this team", I18n.TEAM_USER_NOT_MEMBER);
    }

    public TeamUserNotMemberException(String message) {
        super(message, I18n.TEAM_USER_NOT_MEMBER);
    }
}
