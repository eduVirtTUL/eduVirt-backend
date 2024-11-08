package pl.lodz.p.it.eduvirt.entity.eduvirt.reservation.keys;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.lodz.p.it.eduvirt.entity.eduvirt.general.Metric;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ClusterMetricKey {

    private UUID clusterId;
    private Metric metric;
}
