package pl.lodz.p.it.eduvirt.exceptions.team;

import pl.lodz.p.it.eduvirt.exceptions.general.ConflictException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class TeamUserAlreadyMemberException extends ConflictException {
    public TeamUserAlreadyMemberException() {
        super("User is already a member of this team", I18n.TEAM_USER_ALREADY_MEMBER);
    }
}
