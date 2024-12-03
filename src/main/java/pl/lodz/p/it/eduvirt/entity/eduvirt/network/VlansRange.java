package pl.lodz.p.it.eduvirt.entity.eduvirt.network;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pl.lodz.p.it.eduvirt.entity.eduvirt.AbstractEntity;

@Entity
@Table(name = "private_vlans_range")
@Getter
@ToString
@NoArgsConstructor
public class VlansRange extends AbstractEntity {

    @Column(name = "range_from", nullable = false, updatable = false)
    @Setter
    private Integer from;

    @Column(name = "range_to", nullable = false, updatable = false)
    @Setter
    private Integer to;

    public VlansRange(Integer from,
                      Integer to) {
        this.from = from;
        this.to = to;
    }
}
