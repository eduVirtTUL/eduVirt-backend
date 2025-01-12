package pl.lodz.p.it.eduvirt.service;

import pl.lodz.p.it.eduvirt.entity.key.CourseAccessKey;
import pl.lodz.p.it.eduvirt.entity.key.TeamAccessKey;

import java.util.UUID;

public interface AccessKeyService {
    CourseAccessKey createCourseKey(UUID courseId, String courseKey);
    TeamAccessKey createTeamKey(UUID teamId, String teamKey);
    CourseAccessKey getKeyForCourse(UUID courseId);
    TeamAccessKey getKeyForTeam(UUID teamId);
}