package pl.lodz.p.it.eduvirt.service.ovirt.impl;

import lombok.RequiredArgsConstructor;
import org.ovirt.engine.sdk4.Connection;
import org.ovirt.engine.sdk4.builders.VnicProfileBuilder;
import org.ovirt.engine.sdk4.types.Network;
import org.ovirt.engine.sdk4.types.VnicProfile;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.entity.network.VlansRange;
import pl.lodz.p.it.eduvirt.exceptions.VnicProfileOvirtNotFoundException;
import pl.lodz.p.it.eduvirt.service.ovirt.OVirtVnicProfileService;
import pl.lodz.p.it.eduvirt.util.connection.ConnectionFactory;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

@Service
@LoggerInterceptor
@RequiredArgsConstructor
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class OVirtVnicProfileServiceImpl implements OVirtVnicProfileService {
    private final ConnectionFactory connectionFactory;

    @Override
    public VnicProfile getVnicProfileById(String vnicProfileId) {
        try (Connection connection = connectionFactory.getConnection()) {
            return connection.systemService()
                    .vnicProfilesService()
                    .profileService(vnicProfileId)
                    .get()
                    .follow("network")
                    .send()
                    .profile();
        } catch (Throwable e) {
            throw new VnicProfileOvirtNotFoundException(e.getMessage());
        }
    }

    @Override
    public List<VnicProfile> getVnicProfiles() {
        try (Connection connection = connectionFactory.getConnection()) {
            return connection.systemService()
                    .vnicProfilesService()
                    .list()
                    .follow("network")
                    .send()
                    .profiles();
        } catch (Throwable e) {
            throw new VnicProfileOvirtNotFoundException(e.getMessage());
        }
    }

    @Override
    public List<VnicProfile> getVnicProfiles(Pageable pageable, VlansRange... vlansRanges) {
        // Default sort order
        String sortProp = "vlanid";
        String sortDir = Sort.Direction.ASC.name();

        // We select only the first ordering from the list,
        // due to the fact that oVirt's API supports only sorting by single property
        Optional<Sort.Order> sortOrder = pageable.getSort().stream().findFirst();
        if (sortOrder.isPresent()) {
            sortProp = sortOrder.get().getProperty();
            sortDir = sortOrder.get().getDirection().name();
        }

        // Default vlan id range
        final String defaultRange = "vlanid >= 1";

        String searchQuery = String.format(
                "%s vmnetwork = true sortBy %s %s page %d",
                vlansRanges.length == 0 ? defaultRange : mapVlansRangesToQueryString(vlansRanges),
                sortProp, sortDir, (pageable.getPageNumber() + 1)
        );

        try (Connection connection = connectionFactory.getConnection()) {
            return connection.systemService()
                    .networksService()
                    .list()
                    .follow("vnic_profiles")
                    .search(searchQuery)
                    .max(pageable.getPageSize())
                    .send()
                    .networks()
                    .stream()
                    .map(OVirtVnicProfileServiceImpl::getFirstVnicProfile)
                    .filter(Objects::nonNull)
                    .toList();
        } catch (Throwable e) {
            throw new VnicProfileOvirtNotFoundException(e.getMessage());
        }
    }

    private static String mapVlansRangesToQueryString(VlansRange... vlansRanges) {
        StringBuilder sb = new StringBuilder();
        for (VlansRange vlansRange : vlansRanges) {
            sb.append(" or vlanid >= %d and vlanid <= %d".formatted(vlansRange.getFrom(), vlansRange.getTo()));
        }

        // Start from 4 char to skip ' or ' at the beginning
        return sb.substring(4);
    }

    private static VnicProfile getFirstVnicProfile(Network network) {
        try {
            VnicProfile vnicProfile = network.vnicProfiles().getFirst();

            return new VnicProfileBuilder()
                    .comment(vnicProfile.comment())
                    .customProperties(vnicProfile.customProperties())
                    .description(vnicProfile.description())
                    .failover(vnicProfile.failover())
                    .href(vnicProfile.href())
                    .id(vnicProfile.id())
                    .migratable(vnicProfile.migratablePresent() ? vnicProfile.migratable() : null)
                    .name(vnicProfile.name())
                    .network(network)
                    .networkFilter(vnicProfile.networkFilter())
                    .passThrough(vnicProfile.passThrough())
                    .permissions(vnicProfile.permissions())
                    .portMirroring(vnicProfile.portMirroringPresent() ? vnicProfile.portMirroring() : null)
                    .qos(vnicProfile.qos())
                    .build();
        } catch (NoSuchElementException ignored) {
            return null;
        }
    }
}