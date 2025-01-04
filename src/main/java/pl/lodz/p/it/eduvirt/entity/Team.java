package pl.lodz.p.it.eduvirt.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
@ToString
@Table(
        name = "team",
        indexes = @Index(name = "team_course_id_idx", columnList = "course_id"),
        uniqueConstraints = @UniqueConstraint(name = "team_name_course_id_unique", columnNames = {"name", "course_id"})
)
@Entity
public class Team extends AbstractEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "key", nullable = false, unique = true, length = 16)
    @Size(min = 4, max = 16)
    @Pattern(regexp = "^[a-zA-Z0-9]*$")
    private String key;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "max_size", nullable = false)
    private int maxSize;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "user_team",
            joinColumns = @JoinColumn(name = "team_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"team_id", "user_id"})
    )
    @Column(name = "user_id", nullable = false)
    private List<UUID> users = new ArrayList<>();

    @OneToMany
    @ToString.Exclude
    private List<Reservation> reservations = new ArrayList<>();

    @ManyToOne
    @JoinColumn(
            name = "course_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "team_course_id_fk")
    )
    private Course course;

    /* Constructor */

    public Team(String name,
                String key,
                boolean active,
                int maxSize,
                Course course) {
        this.name = name;
        this.key = key;
        this.active = active;
        this.maxSize = maxSize;
        this.course = course;
    }
}
