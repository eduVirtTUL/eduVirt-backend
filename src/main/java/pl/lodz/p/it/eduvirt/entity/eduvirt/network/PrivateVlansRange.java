package pl.lodz.p.it.eduvirt.entity.eduvirt.network;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pl.lodz.p.it.eduvirt.entity.eduvirt.AbstractEntity;

@Entity
@Table(name = "i72_private_vlans_range")
@Getter
@ToString
@NoArgsConstructor
public class PrivateVlansRange extends AbstractEntity {

    //TODO: Consider if it is at all necessary

    @Column(name = "range_from", nullable = false, updatable = false)
    private Integer from;

    @Column(name = "range_to", nullable = false, updatable = false)
    private Integer to;

    public PrivateVlansRange(Integer from,
                             Integer to) {
        this.from = from;
        this.to = to;
    }
}
