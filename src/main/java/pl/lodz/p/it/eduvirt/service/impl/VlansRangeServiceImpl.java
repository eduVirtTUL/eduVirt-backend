package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.eduvirt.entity.network.VlansRange;
import pl.lodz.p.it.eduvirt.exceptions.VlansRangeInvalidDefinitionException;
import pl.lodz.p.it.eduvirt.exceptions.VlansRangeConflictingRangeException;
import pl.lodz.p.it.eduvirt.exceptions.VlansRangeNotFoundException;
import pl.lodz.p.it.eduvirt.repository.VlansRangeRepository;
import pl.lodz.p.it.eduvirt.service.VlansRangeService;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VlansRangeServiceImpl implements VlansRangeService {

    private final VlansRangeRepository vlansRangeRepository;

    @Override
    public List<VlansRange> getVlansRanges(boolean sorted) {
        return sorted ? vlansRangeRepository.findAll(Sort.by("from").ascending()) : vlansRangeRepository.findAll();
    }

    @Override
    public VlansRange getVlansRange(UUID id) {
        return vlansRangeRepository.findById(id)
                .orElseThrow(() -> new VlansRangeNotFoundException(id));
    }

    @Override
    public VlansRange addVlansRange(VlansRange vlansRange) {
        validateVlansRange(vlansRange);
        compareVlansRangeToOthers(vlansRange);
        return vlansRangeRepository.saveAndFlush(vlansRange);
    }

    @Override
    public void removeVlansRange(UUID id) {
        vlansRangeRepository.findById(id)
                .orElseThrow(() -> new VlansRangeNotFoundException(id));

        vlansRangeRepository.deleteById(id);
    }

    private void validateVlansRange(final VlansRange vlansRange) {
        if (Objects.isNull(vlansRange.getFrom()) || vlansRange.getFrom() < 0
                || Objects.isNull(vlansRange.getTo()) || vlansRange.getTo() < 0
                || vlansRange.getFrom() > vlansRange.getTo()) {
            throw new VlansRangeInvalidDefinitionException("This VLANs range exceeds the limitations (0-4096)");
        }
    }

    private void compareVlansRangeToOthers(final VlansRange vlansRange) {
        List<VlansRange> vlansRangeList = getVlansRanges(false);

        for (VlansRange vlansRangeFromList : vlansRangeList) {
            if (
                    (vlansRange.getFrom() >= vlansRangeFromList.getFrom() && vlansRange.getFrom() <= vlansRangeFromList.getTo())
                 || (vlansRange.getTo() >= vlansRangeFromList.getFrom() && vlansRange.getTo() <= vlansRangeFromList.getTo())
                 || (vlansRange.getFrom() >= vlansRangeFromList.getFrom() && vlansRange.getTo() <= vlansRangeFromList.getTo())
                 || (vlansRange.getFrom() < vlansRangeFromList.getFrom() && vlansRange.getTo() > vlansRangeFromList.getTo())
            ) {
                String exMessage = String.format("This VLANs range conflicts with the already defined range = (%s-%s)",
                        vlansRangeFromList.getFrom(), vlansRangeFromList.getTo()
                );
                throw new VlansRangeConflictingRangeException(exMessage);
            }
        }
    }
}
