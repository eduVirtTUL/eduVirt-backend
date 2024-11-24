package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.entity.eduvirt.network.VlansRange;
import pl.lodz.p.it.eduvirt.exceptions.range.InvalidVlansRangeDefinitionException;
import pl.lodz.p.it.eduvirt.exceptions.range.VlansRangeConflictException;
import pl.lodz.p.it.eduvirt.exceptions.range.VlansRangeNotFoundException;
import pl.lodz.p.it.eduvirt.repository.eduvirt.VlansRangeRepository;
import pl.lodz.p.it.eduvirt.service.VlansRangeService;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@LoggerInterceptor
@RequiredArgsConstructor
public class VlansRangeServiceImpl implements VlansRangeService {

    private final VlansRangeRepository vlansRangeRepository;

    @Override
    public List<VlansRange> getVlansRanges(boolean sorted) {
        return sorted ? vlansRangeRepository.findAll(Sort.by("from").ascending()) : vlansRangeRepository.findAll();
    }

    @Override
    public VlansRange getVlansRange(UUID id) {
        return vlansRangeRepository.findById(id).orElseThrow(VlansRangeNotFoundException::new);
    }

    @Override
    public VlansRange addVlansRange(VlansRange vlansRange) {
        validateVlansRange(vlansRange);
        compareVlansRangeToOthers(vlansRange);
        return vlansRangeRepository.saveAndFlush(vlansRange);
    }

    @Override
    public void removeVlansRange(UUID id) {
        vlansRangeRepository.findById(id).orElseThrow(VlansRangeNotFoundException::new);

        vlansRangeRepository.deleteById(id);
    }

//    @Override
//    public VlansRange resizeVlansRange(VlansRange vlansRange) {
//        vlansRangeRepository.findById(vlansRange.getId()).orElseThrow(VlansRangeNotFoundException::new);
//
//        validateVlansRange(vlansRange);
//        compareVlansRangeToOthers(vlansRange);
//        return vlansRangeRepository.saveAndFlush(vlansRange);
//    }

    private void validateVlansRange(final VlansRange vlansRange) {
        if (Objects.isNull(vlansRange.getFrom()) || vlansRange.getFrom() < 0
                || Objects.isNull(vlansRange.getTo()) || vlansRange.getTo() < 0
                || vlansRange.getFrom() > vlansRange.getTo()) {
            throw new InvalidVlansRangeDefinitionException();
        }
    }

    private void compareVlansRangeToOthers(final VlansRange vlansRange) {
        List<VlansRange> vlansRangeList = getVlansRanges(false);

        //TODO CHECK IT - OPTIMIZE
        for (VlansRange vlansRangeFromList : vlansRangeList) {
            if (
                    (vlansRange.getFrom() >= vlansRangeFromList.getFrom() && vlansRange.getFrom() <= vlansRangeFromList.getTo())
                 || (vlansRange.getTo() >= vlansRangeFromList.getFrom() && vlansRange.getTo() <= vlansRangeFromList.getTo())
                 || (vlansRange.getFrom() >= vlansRangeFromList.getFrom() && vlansRange.getTo() <= vlansRangeFromList.getTo())
                 || (vlansRange.getFrom() < vlansRangeFromList.getFrom() && vlansRange.getTo() > vlansRangeFromList.getTo())
            ) {
                throw new VlansRangeConflictException();
            }
        }
    }
}
