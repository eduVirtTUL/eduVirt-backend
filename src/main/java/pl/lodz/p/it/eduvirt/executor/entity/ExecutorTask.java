package pl.lodz.p.it.eduvirt.executor.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.lodz.p.it.eduvirt.entity.eduvirt.AbstractEntity;
import pl.lodz.p.it.eduvirt.entity.eduvirt.reservation.Reservation;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "executor_task")
@Getter
@NoArgsConstructor
public class ExecutorTask extends AbstractEntity {

    @OneToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(
            name = "reservation_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "reservation_id_fk"),
            updatable = false, nullable = false
    )
    private Reservation reservation;

    public enum TaskType {POD_INIT, POD_DESTRUCT}

    @Column(name = "type", updatable = false, nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskType type;

    enum TaskStatus {SUCCESSFUL, FAILED, IN_PROGRESS}

    @Column(name = "status", updatable = true, nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.IN_PROGRESS;

    @Column(name = "_created_at", updatable = false, nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;

    @Column(name = "description", updatable = true, nullable = true, length = 200)
    private String description;

    // Constructors

    public ExecutorTask(Reservation reservation,
                        TaskType type) {
        this.reservation = reservation;
        this.type = type;
    }


    // Other methods

    @PrePersist
    private void setCreatedTime() {
        this.createdAt = LocalDateTime.now();
    }

    public void setSuccessful() {
        if (status.equals(TaskStatus.IN_PROGRESS)) {
            this.status = TaskStatus.SUCCESSFUL;
        }
    }

    public void setFailed() {
        if (status.equals(TaskStatus.IN_PROGRESS)) {
            this.status = TaskStatus.FAILED;
        }
    }

    public void setDescription(String description) {
        if (Objects.isNull(this.description)) {
            this.description = description;
        }
    }
}
