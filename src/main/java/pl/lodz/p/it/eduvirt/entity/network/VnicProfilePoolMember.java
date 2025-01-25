package pl.lodz.p.it.eduvirt.entity.network;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "vnic_profile_pool")
@Getter
@ToString
@NoArgsConstructor
public class VnicProfilePoolMember {

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
        String performerId = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Principal::getName).orElse("00000000-0000-0000-0000-000000000000");
        this.createdBy = UUID.fromString(performerId);
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        VnicProfilePoolMember that = (VnicProfilePoolMember) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
