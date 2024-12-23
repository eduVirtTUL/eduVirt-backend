package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import pl.lodz.p.it.eduvirt.dto.vlans_range.CreateVlansRangeDto;
import pl.lodz.p.it.eduvirt.dto.vlans_range.VlansRangeDto;
import pl.lodz.p.it.eduvirt.entity.network.VlansRange;

@Mapper(componentModel = "spring")
public interface VlansRangeMapper {

    VlansRange createVlansRangeDtoToVlansRange(CreateVlansRangeDto createVlansRangeDto);

    VlansRangeDto vlansRangeToDto(VlansRange vlansRange);

//    default VlansRange resizeVlansRangeDtoToVlansRange(UUID id, ResizeVlansRangeDto resizeVlansRangeDto) {
//        return new VlansRange(
//                id,
//                resizeVlansRangeDto.from()
//                resizeVlansRangeDto.to()
//        );
//    }
}