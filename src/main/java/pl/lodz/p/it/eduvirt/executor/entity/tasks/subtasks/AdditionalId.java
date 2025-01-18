package pl.lodz.p.it.eduvirt.executor.entity.tasks.subtasks;

import lombok.Getter;

import java.util.UUID;

@Getter
public enum AdditionalId {

    VNIC_PROFILE,
    NIC;

    private UUID id;

    public AdditionalId withId(UUID id) {
        this.id = id;
        return this;
    }
}
