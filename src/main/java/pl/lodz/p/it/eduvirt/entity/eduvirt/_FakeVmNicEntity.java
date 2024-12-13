package pl.lodz.p.it.eduvirt.entity.eduvirt;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "vm_nic")
@IdClass(_FakeVmNicKey.class)
public class _FakeVmNicEntity {
    @Id
    private UUID vmId;

    @Id
    private UUID nicId;
}
