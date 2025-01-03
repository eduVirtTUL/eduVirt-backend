package pl.lodz.p.it.eduvirt.executor.entity;

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
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.lodz.p.it.eduvirt.entity.AbstractEntity;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "executor_subtask")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "kind")
@Getter
@NoArgsConstructor
public abstract class ExecutorSubtask extends AbstractEntity {

    @ManyToOne
    @JoinColumn(
            name = "task_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "task_id_fk"),
            nullable = false, updatable = false
    )
    private ExecutorTask executorTask;

    public enum SubtaskType {
        ASSIGN_VNIC_PROFILE, REMOVE_VNIC_PROFILE,
        START_VM, SHUTDOWN_VM, POWER_OFF, REBOOT_VM,
        ASSIGN_PERMISSION, REVOKE_PERMISSION
    }

    /// todo michal maybe change it to VirtualMachine entity -> Foreign Key
    @Column(name = "vm_id", updatable = false, nullable = false)
    private UUID vmId;

    @Column(name = "type", updatable = false, nullable = false)
    @Enumerated(EnumType.STRING)
    private SubtaskType type;

    @Column(name = "_created_at", updatable = false, nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;

    @Column(name = "successful", updatable = true, nullable = true)
    private Boolean successful;

    @Column(name = "description", updatable = true, nullable = true, length = 200)
    private String description;

    // Constructors

    public ExecutorSubtask(ExecutorTask executorTask,
                           UUID vmId,
                           SubtaskType type) {
        this.executorTask = executorTask;
        //TODO michal maybe validate it with VMs in RG, but maybeeeee
        this.vmId = vmId;
        this.type = type;
    }

    // Other methods

    @PrePersist
    private void setCreatedTime() {
        this.createdAt = LocalDateTime.now();
    }

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
}