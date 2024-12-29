package pl.lodz.p.it.eduvirt.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor
public class User {
    @Id
    private UUID id;

//    @ManyToMany
//    private List<Team> teams = new ArrayList<>();
}
