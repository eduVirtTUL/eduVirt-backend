package pl.lodz.p.it.eduvirt.entity.eduvirt;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "course")
@Entity
public class Course extends AbstractEntity {
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    @Column(name = "description", nullable = false, length = 1000)
    private String description;
    @OneToMany(mappedBy = "course")
    private List<ResourceGroupPool> resourceGroupPools;
    @OneToMany(mappedBy = "course")
    private List<Team> teams = new ArrayList<>();
    @Column(name = "team_based", nullable = false)
    private boolean teamBased;
    @Column(name = "course_key", unique = true, length = 17, nullable = true)
    @Size(min = 5, max = 17)
    @Pattern(regexp = "^s[a-zA-Z0-9]{4,16}$")
    private String courseKey;
    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy hibernateProxy ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy hibernateProxy ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Course course = (Course) o;
        return getId() != null && Objects.equals(getId(), course.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy hibernateProxy ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
