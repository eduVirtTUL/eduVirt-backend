package pl.lodz.p.it.eduvirt.executor.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.lodz.p.it.eduvirt.entity.HistoricalData;
import pl.lodz.p.it.eduvirt.entity.Reservation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "executor_task")
@Getter
@NoArgsConstructor
public class ExecutorTask extends HistoricalData {

    @ManyToOne(optional = false)
    @JoinColumn(
            name = "reservation_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "reservation_id_fk"),
            updatable = false, nullable = false
    )
    private Reservation reservation;

    public enum TaskType {POD_INIT, POD_DESTRUCT, END_RESERVATION}

    @Column(name = "type", updatable = false, nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskType type;

    enum TaskStatus {SUCCESSFUL, FAILED, IN_PROGRESS}

    @Column(name = "status", updatable = true, nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.IN_PROGRESS;

    @Column(name = "description", updatable = true, nullable = true, length = 200)
    private String description;

    @OneToMany(mappedBy = "executorTask", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ExecutorSubtask> subtasks = new ArrayList<>();

    // Constructors

    public ExecutorTask(Reservation reservation,
                        TaskType type) {
        this.reservation = reservation;
        this.type = type;
    }


    // Other methods

    public void setSuccessful() {
        if (status.equals(TaskStatus.IN_PROGRESS)) {
            this.status = TaskStatus.SUCCESSFUL;
        } else {
            throw new IllegalStateException("Cannot set SUCCESSFUL status if the task does not have IN_PROGRESS status");
        }
    }

    public void setFailed() {
        if (status.equals(TaskStatus.IN_PROGRESS)) {
            this.status = TaskStatus.FAILED;
        } else {
            throw new IllegalStateException("Cannot set FAILED status if the task does not have IN_PROGRESS status");
        }
    }

    public void setDescription(String description) {
        if (Objects.isNull(this.description)) {
            this.description = description;
        } else {
            throw new IllegalStateException("Cannot override task description");
        }
    }
}
