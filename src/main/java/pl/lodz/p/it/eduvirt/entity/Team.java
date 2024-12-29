package pl.lodz.p.it.eduvirt.entity;

import jakarta.persistence.*;
import lombok.*;
import pl.lodz.p.it.eduvirt.entity.reservation.Reservation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "team")
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

//    @ManyToMany(fetch = FetchType.LAZY)
//    private List<User> users = new ArrayList<>();

    @OneToMany(mappedBy = "team")
    @ToString.Exclude
    private List<Reservation> reservations = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    // @ManyToOne
    // private AccessKey accessKey;

    @OneToMany(mappedBy = "team")
    private List<PodStateful> statefulPods = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "pod_stateless",
            joinColumns = @JoinColumn(name = "team_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"team_id", "rgp_id"})
    )
    @Column(name = "rgp_id", nullable = false)
    private List<UUID> statelessPods = new ArrayList<>();

}
