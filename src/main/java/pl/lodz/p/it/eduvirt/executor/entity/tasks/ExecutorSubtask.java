package pl.lodz.p.it.eduvirt.executor.entity.tasks;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.lodz.p.it.eduvirt.entity.Updatable;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "executor_subtask")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "kind")
@Getter
@NoArgsConstructor
public abstract class ExecutorSubtask extends Updatable {

    public enum SubtaskType {
        CHECK_VMS_STATUSES, CHECK_RG_IN_USE,
        ASSIGN_VNIC_PROFILE, REMOVE_VNIC_PROFILE,
        START_VM, SHUTDOWN_VM, POWER_OFF, REBOOT_VM,
        ASSIGN_PERMISSION, REVOKE_PERMISSION
    }

    @ManyToOne
    @JoinColumn(
            name = "task_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "task_id_fk"),
            nullable = false, updatable = false
    )
    private ExecutorTask executorTask;

    @Column(name = "vm_id", updatable = false, nullable = true)
    private UUID vmId;

    @Column(name = "type", updatable = false, nullable = true)
    @Enumerated(EnumType.STRING)
    private SubtaskType type;

    @Column(name = "successful", updatable = true, nullable = true)
    private Boolean successful;

    @Column(name = "description", updatable = true, nullable = true, length = 200)
    private String description;

    @Column(name = "_created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;

    @Column(name = "_updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updatedAt;

    /* Constructors */

    public ExecutorSubtask(ExecutorTask executorTask,
                           UUID vmId,
                           SubtaskType type) {
        this.executorTask = executorTask;
        this.vmId = vmId;
        this.type = type;
    }

    /* Custom Getters */

    public Boolean getSuccessful() {
        return Optional.ofNullable(successful).orElse(false);
    }


    /* Custom setters */

    public void setSuccessful(Boolean successful) {
        if (Objects.isNull(this.successful)) {
            this.successful = successful;
        } else {
            throw new IllegalStateException("Cannot override subtask status");
        }
    }

    public void setDescription(String description) {
        if (Objects.isNull(this.description)) {
            this.description = description;
        } else {
            throw new IllegalStateException("Cannot override subtask description");
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
