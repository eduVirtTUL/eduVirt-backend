package pl.lodz.p.it.eduvirt.entity.eduvirt.general;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.lodz.p.it.eduvirt.entity.eduvirt.AbstractEntity;

@Entity
@Table(name = "i72_metric")
@Getter @Setter
@NoArgsConstructor
public class Metric extends AbstractEntity {

    @Column(name = "name", nullable = false, unique = true, length = 64)
    private String name;
}
