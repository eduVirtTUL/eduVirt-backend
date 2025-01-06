package pl.lodz.p.it.eduvirt.entity.key;

import jakarta.persistence.*;
import lombok.*;
import pl.lodz.p.it.eduvirt.entity.AbstractEntity;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "access_key")
@Getter
@Setter
public abstract class AccessKey extends AbstractEntity {
    @Column(name = "key_value", nullable = false, unique = true)
    private String keyValue;
}