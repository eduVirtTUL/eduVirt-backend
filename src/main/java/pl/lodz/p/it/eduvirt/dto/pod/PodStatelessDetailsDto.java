package pl.lodz.p.it.eduvirt.dto.pod;

import pl.lodz.p.it.eduvirt.dto.course.CourseBasicDto;
import pl.lodz.p.it.eduvirt.dto.resource_group_pool.ResourceGroupPoolDto;
import pl.lodz.p.it.eduvirt.dto.team.TeamDto;

import java.util.UUID;

public record PodStatelessDetailsDto(
        UUID id,
        ResourceGroupPoolDto resourceGroupPool,
        CourseBasicDto course,
        TeamDto team
) {
}