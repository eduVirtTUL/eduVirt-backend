package pl.lodz.p.it.eduvirt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor
public class User {

    @Id
    private UUID id;

    // Note: Added email for mailing purposes
    @Setter
    @Column(name = "email", unique = true, nullable = false)
    private String email;
}
