package pl.lodz.p.it.eduvirt.entity.reservation.keys;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ClusterNetworkKey {

    private UUID clusterId;
    private UUID networkId;
}
