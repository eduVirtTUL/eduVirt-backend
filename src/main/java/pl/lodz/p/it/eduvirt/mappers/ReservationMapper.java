package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.lodz.p.it.eduvirt.dto.reservation.ReservationDetailsDto;
import pl.lodz.p.it.eduvirt.dto.reservation.ReservationDto;
import pl.lodz.p.it.eduvirt.entity.Reservation;

@Mapper(
        componentModel = "spring",
        uses = {
                TeamMapper.class,
                ResourceGroupMapper.class
        }
)
public interface ReservationMapper {

    @Mapping(target = "start", expression = "java(reservation.getStartTime())")
    @Mapping(target = "end", expression = "java(reservation.getEndTime())")
    ReservationDto reservationToDto(Reservation reservation);

    @Mapping(target = "id", expression = "java(reservation.getId())")
    @Mapping(target = "start", expression = "java(reservation.getStartTime())")
    @Mapping(target = "end", expression = "java(reservation.getEndTime())")
    ReservationDetailsDto reservationToDetailsDto(Reservation reservation);
}
