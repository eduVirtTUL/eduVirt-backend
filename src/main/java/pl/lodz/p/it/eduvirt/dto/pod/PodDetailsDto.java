package pl.lodz.p.it.eduvirt.dto.pod;

import pl.lodz.p.it.eduvirt.dto.course.CourseBasicDto;
import pl.lodz.p.it.eduvirt.dto.resource_group.ResourceGroupDto;
import pl.lodz.p.it.eduvirt.dto.team.TeamDto;

import java.util.UUID;

public record PodDetailsDto(
        UUID id,
        ResourceGroupDto resourceGroup,
        CourseBasicDto course,
        TeamDto team
) {
}
