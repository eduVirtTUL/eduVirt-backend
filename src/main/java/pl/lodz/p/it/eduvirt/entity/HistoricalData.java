package pl.lodz.p.it.eduvirt.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Optional;
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
        String performerId = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Principal::getName).orElse("00000000-0000-0000-0000-000000000000");
        this.createdBy = UUID.fromString(performerId);
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
    }

    @PreUpdate
    public void changeUpdateData() {
        String performerId = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Principal::getName).orElse("00000000-0000-0000-0000-000000000000");
        this.updatedBy = UUID.fromString(performerId);
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
    }
}
