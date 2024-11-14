package pl.lodz.p.it.eduvirt.entity.eduvirt;

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
@Table(name = "i72_users")
@NoArgsConstructor
public class User {
    @Id
    private UUID id;
}
