package pl.lodz.p.it.eduvirt.util.etag;

import lombok.*;
import pl.lodz.p.it.eduvirt.entity.Updatable;

import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public final class EtagPayload {
    private UUID id;
    private long version;

    public EtagPayload(Updatable updatable) {
        this.id = updatable.getId();
        this.version = updatable.getVersion();
    }
}
