package pl.lodz.p.it.eduvirt.executor.entity.tasks.subtasks;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.lodz.p.it.eduvirt.executor.entity.tasks.ExecutorSubtask;
import pl.lodz.p.it.eduvirt.executor.entity.tasks.ExecutorTask;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "executor_subtask_preconditions_check")
@PrimaryKeyJoinColumn(foreignKey = @ForeignKey(name = "executor_subtask_preconditions_check_fk"))
@DiscriminatorValue("PRECONDITIONS_CHECK")
@Getter
@NoArgsConstructor
public class PreconditionsCheckTask extends ExecutorSubtask {

    /* Constructors */

    public PreconditionsCheckTask(ExecutorTask executorTask,
                                  SubtaskType type) {
        super(executorTask, UUID.fromString("00000000-0000-0000-0000-000000000000"), type);

        if (!List.of(SubtaskType.CHECK_VMS_STATUSES, SubtaskType.CHECK_RG_IN_USE).contains(type)) {
            throw new IllegalArgumentException("Invalid 'preconditions check' subtask type");
        }
    }
}
