package pl.lodz.p.it.eduvirt.exceptions.user;

import pl.lodz.p.it.eduvirt.exceptions.general.NotFoundException;
import pl.lodz.p.it.eduvirt.util.I18n;

import java.util.UUID;

public class UserDoesntBelongToCourseException extends NotFoundException {
    public UserDoesntBelongToCourseException() {
        super("User doesn't belong to a team in the course", I18n.TEAM_NOT_FOUND);
    }

    public UserDoesntBelongToCourseException(UUID userId, UUID courseId) {
        super("User with id %s doesn't belong to course with id %s.".formatted(userId, courseId), I18n.TEAM_NOT_FOUND);
    }
}