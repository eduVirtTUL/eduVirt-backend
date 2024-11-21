package pl.lodz.p.it.eduvirt.entity.eduvirt;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.UUID;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
public class HistoricalData extends Updatable {

    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "_created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;

    @Column(name = "_updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updatedAt;

    // Constructors

    public HistoricalData(Long version) {
        super(version);
    }

    // Other methods

    @PrePersist
    public void changeCreateData() {
        //TODO: Change it later, when authentication is implemented (to put user's id in the context as well)
//        this.createdBy = UUID.fromString((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void changeUpdateData() {
        //TODO: Change it later, when authentication is implemented (to put user's id in the context as well)
//        this.updatedBy = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        this.updatedAt = LocalDateTime.now();
    }
}
