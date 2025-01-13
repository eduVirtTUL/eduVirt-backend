package pl.lodz.p.it.eduvirt.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "users")
@NoArgsConstructor
public class User {

    @Id
    @Column(name = "user_id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "ovirt_id", unique = true, nullable = false)
    private UUID oVirtId;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @ElementCollection
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private List<String> roles = new ArrayList<>();

    @ManyToMany(mappedBy = "users")
    @ToString.Exclude
    private List<Team> teams = new ArrayList<>();
}
