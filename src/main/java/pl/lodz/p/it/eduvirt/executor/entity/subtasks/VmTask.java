package pl.lodz.p.it.eduvirt.executor.entity.subtasks;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.lodz.p.it.eduvirt.executor.entity.ExecutorSubtask;
import pl.lodz.p.it.eduvirt.executor.entity.ExecutorTask;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "executor_subtask_vm")
@PrimaryKeyJoinColumn(foreignKey = @ForeignKey(name = "executor_subtask_vm_fk"))
@DiscriminatorValue("VM")
@Getter
@NoArgsConstructor
public class VmTask extends ExecutorSubtask {

    // Constructors

    public VmTask(ExecutorTask executorTask,
                  UUID vmId,
                  SubtaskType type) {
        super(executorTask, vmId, type);

        if (!List.of(SubtaskType.START_VM, SubtaskType.SHUTDOWN_VM,
                SubtaskType.POWER_OFF, SubtaskType.REBOOT_VM).contains(type)) {
            throw new IllegalArgumentException("Invalid VM subtask type");
        }
    }
}
