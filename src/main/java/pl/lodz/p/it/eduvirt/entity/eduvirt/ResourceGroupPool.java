package pl.lodz.p.it.eduvirt.entity.eduvirt;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "resource_group_pool")
public class ResourceGroupPool extends HistoricalData {
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    @Column(name = "max_rent", nullable = false)
    private int maxRent;
    @Column(name = "grace_period", nullable = false)
    private int gracePeriod;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ResourceGroup> resourceGroups = new ArrayList<>();

    @ManyToOne
    private Course course;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy hibernateProxy ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy hibernateProxy ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        ResourceGroupPool that = (ResourceGroupPool) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy hibernateProxy ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
