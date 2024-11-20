package pl.lodz.p.it.eduvirt.service;

import pl.lodz.p.it.eduvirt.entity.eduvirt.network.VlansRange;

import java.util.List;
import java.util.UUID;

public interface VlansRangeService {
    //todo add methods to handle CRUD for vlans range

    List<VlansRange> getVlansRanges(boolean sorted);

    VlansRange getVlansRange(UUID id);

    VlansRange addVlansRange(VlansRange vlansRange);

    void removeVlansRange(UUID id);

//    VlansRange resizeVlansRange(VlansRange vlansRange);
}
