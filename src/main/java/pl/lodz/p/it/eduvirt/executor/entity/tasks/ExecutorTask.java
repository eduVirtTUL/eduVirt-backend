package pl.lodz.p.it.eduvirt.executor.entity.tasks;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.lodz.p.it.eduvirt.entity.Reservation;
import pl.lodz.p.it.eduvirt.entity.Updatable;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;

@Entity
@Table(
        name = "executor_task",
        indexes = @Index(name = "executor_task_reservation_id_idx", columnList = "reservation_id")
)
@Getter
@NoArgsConstructor
public class ExecutorTask extends Updatable {

    public enum TaskStatus {SUCCESSFUL, FAILED, IN_PROGRESS}

    @ManyToOne(optional = false)
    @JoinColumn(
            name = "reservation_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "reservation_id_fk"),
            unique = false, updatable = false, nullable = false
    )
    private Reservation reservation;

    public enum TaskType {POD_INIT, POD_DESTRUCT, END_RESERVATION}

    @Column(name = "type", updatable = false, nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskType type;

    @Column(name = "status", updatable = true, nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.IN_PROGRESS;

    @Column(name = "description", updatable = true, nullable = true, length = 200)
    private String description;

    @OneToMany(mappedBy = "executorTask", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ExecutorSubtask> subtasks;

    @Column(name = "_created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;

    @Column(name = "_updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updatedAt;

    /* Constructors */

    public ExecutorTask(Reservation reservation,
                        TaskType type) {
        this.reservation = reservation;
        this.type = type;
    }


    /* Custom setters */

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

    /* Other methods */

    @PrePersist
    public void changeCreateData() {
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
    }

    @PreUpdate
    public void changeUpdateData() {
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
    }
}
