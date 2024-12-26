package pl.lodz.p.it.eduvirt.entity.reservation.keys;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.lodz.p.it.eduvirt.entity.general.Metric;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ClusterMetricKey {

    private UUID clusterId;
    private Metric metric;
}
