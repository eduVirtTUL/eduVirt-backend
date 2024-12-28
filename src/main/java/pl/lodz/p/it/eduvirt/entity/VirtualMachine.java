package pl.lodz.p.it.eduvirt.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Builder
@Setter
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

    @OneToMany(mappedBy = "virtualMachine", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<NetworkInterface> networkInterfaces = new ArrayList<>();
}
