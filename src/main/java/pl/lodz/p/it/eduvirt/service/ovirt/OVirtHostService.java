package pl.lodz.p.it.eduvirt.service.ovirt;

import org.ovirt.engine.sdk4.types.Host;

import java.util.UUID;

public interface OVirtHostService {

    Host findHostById(UUID hostId);
}
