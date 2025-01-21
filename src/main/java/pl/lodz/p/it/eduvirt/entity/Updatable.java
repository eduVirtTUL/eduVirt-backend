package pl.lodz.p.it.eduvirt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

@MappedSuperclass
@Getter
@NoArgsConstructor
public abstract class Updatable extends AbstractEntity {

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    /* Constructors */

    protected Updatable(Long version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Updatable updatable = (Updatable) o;
        return getId() != null && Objects.equals(getId(), updatable.getId());
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
