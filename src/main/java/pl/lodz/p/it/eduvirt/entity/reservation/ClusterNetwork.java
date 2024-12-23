package pl.lodz.p.it.eduvirt.entity.reservation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pl.lodz.p.it.eduvirt.entity.reservation.keys.ClusterNetworkKey;

import java.util.UUID;

@Entity
@Table(name = "cluster_network")
@IdClass(ClusterNetworkKey.class)
@Getter
@Setter
public class ClusterNetwork {

    @Id
    @Column(name = "cluster_id", nullable = false, updatable = false)
    private UUID clusterId;

    @Id
    @Column(name = "network_id", nullable = false, updatable = false)
    private UUID networkId;
}
