package pl.lodz.p.it.eduvirt.dto.access_key;

import java.util.UUID;

public record CreateAccessKeyDto(UUID targetId, boolean isTeamKey) {
}
