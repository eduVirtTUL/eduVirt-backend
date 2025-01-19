package pl.lodz.p.it.eduvirt.service;

import pl.lodz.p.it.eduvirt.entity.Course;

public interface KeyGeneratorService {
    String generateUniqueTeamKey(Course course);
}