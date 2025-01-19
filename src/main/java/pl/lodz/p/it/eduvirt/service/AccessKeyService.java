package pl.lodz.p.it.eduvirt.service;

import pl.lodz.p.it.eduvirt.entity.Course;
import pl.lodz.p.it.eduvirt.entity.Team;
import pl.lodz.p.it.eduvirt.entity.key.CourseAccessKey;
import pl.lodz.p.it.eduvirt.entity.key.TeamAccessKey;

import java.util.UUID;

public interface AccessKeyService {
    CourseAccessKey createCourseKey(Course course, String courseKey);

    void createTeamKey(UUID teamId, String teamKey);

    CourseAccessKey getKeyForCourse(Course course);

    TeamAccessKey getKeyForTeam(Team team, Course course);
}