package pl.lodz.p.it.eduvirt.dto.access_key;

import pl.lodz.p.it.eduvirt.entity.key.AccessKeyType;

import java.util.UUID;

public record AccessKeyDto(UUID id, String keyValue, AccessKeyType accessKeyType, UUID courseId, UUID teamId) {
}
