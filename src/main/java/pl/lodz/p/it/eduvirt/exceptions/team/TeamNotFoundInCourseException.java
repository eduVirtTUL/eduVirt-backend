package pl.lodz.p.it.eduvirt.exceptions.team;

import pl.lodz.p.it.eduvirt.exceptions.general.NotFoundException;
import pl.lodz.p.it.eduvirt.util.I18n;

import java.util.UUID;

public class TeamNotFoundInCourseException extends NotFoundException {
    public TeamNotFoundInCourseException() {
        super("Team has not been found in specified course", I18n.TEAM_NOT_FOUND_IN_COURSE);
    }

    public TeamNotFoundInCourseException(UUID courseId) {
        super("Team with id %s could not be found.".formatted(courseId), I18n.TEAM_NOT_FOUND_IN_COURSE);
    }
}
