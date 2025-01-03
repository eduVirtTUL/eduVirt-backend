package pl.lodz.p.it.eduvirt.executor.entity.subtasks;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.lodz.p.it.eduvirt.executor.entity.ExecutorSubtask;
import pl.lodz.p.it.eduvirt.executor.entity.ExecutorTask;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "executor_subtask_vnic_profile")
@PrimaryKeyJoinColumn(foreignKey = @ForeignKey(name = "executor_subtask_vnic_profile_fk"))
@DiscriminatorValue("VNIC_PROFILE")
@Getter
@NoArgsConstructor
public class VnicProfileTask extends ExecutorSubtask {

    @Column(name = "vnic_profile_id", updatable = true, nullable = true)
    @Setter
    private UUID vnicProfileId;

    // Constructors

    public VnicProfileTask(ExecutorTask executorTask,
                           UUID vmId,
                           SubtaskType type) {
        super(executorTask, vmId, type);

        if (!List.of(SubtaskType.ASSIGN_VNIC_PROFILE, SubtaskType.REMOVE_VNIC_PROFILE).contains(type)) {
            throw new IllegalArgumentException("Invalid VNIC PROFILE subtask type");
        }
    }
}
