package pl.lodz.p.it.eduvirt.entity.ovirt;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pl.lodz.p.it.eduvirt.entity.ovirt.readonly.VnicProfile;

import java.util.UUID;

@Entity
@Table(name = "i72_vnic_profile_pool", schema = "eduvirt")
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

    @OneToOne(optional = false, fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
    @PrimaryKeyJoinColumn(
            name = "id",
            referencedColumnName = "id"
//            foreignKey = @ForeignKey(name = "vnic_profile_pool_id_fk")
    )
    private VnicProfile vnicProfile;

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
