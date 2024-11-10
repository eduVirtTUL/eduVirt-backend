package pl.lodz.p.it.eduvirt.entity.eduvirt.network;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Entity
@Table(name = "i72_vnic_profile_pool")
@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
public class VnicProfilePool {

    //TODO: Consider adding ONLY _created_at and _created_by

    @EqualsAndHashCode.Include
    @Id
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    private UUID id;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Setter
    @Column(name = "in_use", nullable = false)
    private Boolean inUse = false;

    public VnicProfilePool(UUID id,
                           Boolean inUse) {
        this.id = id;
        this.inUse = inUse;
    }
}
