package pl.lodz.p.it.eduvirt.service;

import pl.lodz.p.it.eduvirt.entity.key.AccessKey;
import java.util.UUID;

public interface AccessKeyService {
    AccessKey createCourseKey(UUID courseId, String courseKey);
    AccessKey createTeamKey(UUID teamId, String teamKey);
    AccessKey getKey(String keyValue);
}