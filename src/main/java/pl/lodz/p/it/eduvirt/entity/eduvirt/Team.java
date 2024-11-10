package pl.lodz.p.it.eduvirt.entity.eduvirt;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "i72_team")
@Entity
public class Team extends AbstractEntity {

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "key", nullable = false, unique = true)
    private String key;

    @Column(name = "active", nullable = false)
    private boolean active;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "i72_user_team",
            joinColumns = @JoinColumn(name = "team_id")
    )
    @Column(name = "user_id", nullable = false)
    private List<UUID> users = new ArrayList<>();

}
