package pl.lodz.p.it.eduvirt.entity.eduvirt.network;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vnic_profile_pool")
@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
public class VnicProfilePoolMember {

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

    @Column(name = "vlan_id", unique = true, nullable = false, updatable = false)
    private Integer vlanId;

    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    @Column(name = "_created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;

    @Transient
    @Setter
    private String name;

    public VnicProfilePoolMember(UUID id,
                                 Integer vlanId) {
        this.id = id;
        this.vlanId = vlanId;
    }

    @PrePersist
    public void changeCreateData() {
        //TODO: Change it later, when authentication is implemented (to put user's id in the context as well)
//        this.createdBy = UUID.fromString((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        this.createdAt = LocalDateTime.now();
    }
}
