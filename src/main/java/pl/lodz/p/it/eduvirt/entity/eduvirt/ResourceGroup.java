package pl.lodz.p.it.eduvirt.entity.eduvirt;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.proxy.HibernateProxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "resource_group")
public class ResourceGroup extends HistoricalData {
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    @Column(name = "stateless", nullable = false)
    private boolean stateless;

    @Column(name = "max_rent_time", nullable = false)
    private int maxRentTime;

    @ToString.Exclude
    @OneToMany(mappedBy = "resourceGroup", fetch = FetchType.LAZY)
    private List<VirtualMachine> vms = new ArrayList<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "resourceGroup", fetch = FetchType.LAZY)
    private List<ResourceGroupNetwork> networks = new ArrayList<>();

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Class<?> oEffectiveClass = o instanceof HibernateProxy hibernateProxy ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy hibernateProxy ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        ResourceGroup that = (ResourceGroup) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy hibernateProxy ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
