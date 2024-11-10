package pl.lodz.p.it.eduvirt.entity.ovirt.readonly;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Immutable;

import java.util.UUID;

@Entity
@Immutable
@Table(name = "vnic_profiles", schema = "public")
@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
public class VnicProfile {

    @EqualsAndHashCode.Include
    @Id
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    private UUID id;

}
