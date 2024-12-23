package pl.lodz.p.it.eduvirt.entity.eduvirt;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "virtual_machine")
public class VirtualMachine {
    @Id
    private UUID id;

    private boolean hidden;

    @ManyToOne()
    private ResourceGroup resourceGroup;
}
