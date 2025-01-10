package pl.lodz.p.it.eduvirt.entity;

import jakarta.persistence.*;
import lombok.*;
import pl.lodz.p.it.eduvirt.exceptions.StatefulPodAssignmentException;
import pl.lodz.p.it.eduvirt.exceptions.StatelessPodAssignmentException;

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
public class Team extends Updatable {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "max_size", nullable = false)
    private int maxSize;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_team",
            joinColumns = @JoinColumn(name = "team_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"team_id", "user_id"})
    )
    @Column(name = "user_id", nullable = false)
    private List<UUID> users = new ArrayList<>();

    @OneToMany(mappedBy = "team")
    @ToString.Exclude
    private List<Reservation> reservations = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @OneToMany(mappedBy = "team")
    @ToString.Exclude
    private List<PodStateful> statefulPods = new ArrayList<>();

    @OneToMany(mappedBy = "team")
    @ToString.Exclude
    private List<PodStateless> statelessPods = new ArrayList<>();

    /* Constructor */

    public Team(String name,
                boolean active,
                int maxSize,
                Course course) {
        this.name = name;
        this.active = active;
        this.maxSize = maxSize;
        this.course = course;
    }

    /* Other methods */

    /* Check POD assignment */

    public boolean hasStatefulPod(UUID podId) {
        return this.getStatefulPods().stream()
                .anyMatch(podStateful -> podStateful.getId().equals(podId));
    }

    public boolean hasStatelessPod(UUID podId) {
        return this.getStatelessPods().stream()
                .anyMatch(id -> id.equals(podId));
    }

    /* Retrieve POD */

    public UUID getStatelessPod(UUID podId) {
        return this.getStatelessPods().stream()
                .filter(id -> id.equals(podId))
                .findAny().orElseThrow(() -> new StatelessPodAssignmentException(
                        "Stateless POD %s is not assigned to team %s".formatted(podId, getId())));
    }

    public PodStateful getStatefulPod(UUID podId) {
        return this.getStatefulPods().stream()
                .filter(pod -> pod.getId().equals(podId))
                .findAny().orElseThrow(() -> new StatefulPodAssignmentException(
                        "Stateful POD %s is not assigned to team %s".formatted(podId, getId())));
    }
}
