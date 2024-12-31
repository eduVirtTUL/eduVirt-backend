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
@Table(name = "executor_subtask_permission")
@PrimaryKeyJoinColumn(foreignKey = @ForeignKey(name = "executor_subtask_permission_fk"))
@DiscriminatorValue("PERMISSION")
@Getter
@NoArgsConstructor
public class PermissionTask extends ExecutorSubtask {

    // Constructors

    public PermissionTask(ExecutorTask executorTask,
                          UUID vmId,
                          SubtaskType type) {
        super(executorTask, vmId, type);

        if (!List.of(SubtaskType.ASSIGN_PERMISSION, SubtaskType.REVOKE_PERMISSION).contains(type)) {
            throw new IllegalArgumentException("Invalid PERMISSION subtask type");
        }
    }
}
