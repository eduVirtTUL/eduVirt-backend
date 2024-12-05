package pl.lodz.p.it.eduvirt.entity.eduvirt;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import pl.lodz.p.it.eduvirt.entity.eduvirt.reservation.Reservation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
@Table(name = "team")
@Entity
public class Team extends AbstractEntity {

    @Column(name = "name", nullable = false, unique = true)
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
    private Course course;
}
