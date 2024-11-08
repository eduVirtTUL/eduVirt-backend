package pl.lodz.p.it.eduvirt.entity.eduvirt;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.UUID;

@MappedSuperclass
@Getter @ToString
@EqualsAndHashCode
@NoArgsConstructor
public class AbstractEntity {

    @Id
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
}
