package pl.lodz.p.it.eduvirt.entity.eduvirt.reservation;

import lombok.Getter;
import lombok.Setter;
import pl.lodz.p.it.eduvirt.entity.eduvirt.ResourceGroup;
import pl.lodz.p.it.eduvirt.entity.eduvirt.Team;

@Getter
@Setter
@Deprecated
public class _FakeMockTeamAndRG {

    private ResourceGroup resourceGroup;
    private Team team;

    public _FakeMockTeamAndRG(ResourceGroup resourceGroup,
                              Team team) {
        this.resourceGroup = resourceGroup;
        this.team = team;
    }
}
