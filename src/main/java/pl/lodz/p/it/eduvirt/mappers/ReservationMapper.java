package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.lodz.p.it.eduvirt.dto.reservation.ReservationDetailsDto;
import pl.lodz.p.it.eduvirt.dto.reservation.ReservationDto;
import pl.lodz.p.it.eduvirt.entity.Reservation;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    @Mapping(target = "teamId", expression = "java(reservation.getTeam().getId())")
    @Mapping(target = "start", expression = "java(reservation.getStartTime())")
    @Mapping(target = "end", expression = "java(reservation.getEndTime())")
    ReservationDto reservationToDto(Reservation reservation);

    @Mapping(target = "teamId", expression = "java(reservation.getTeam().getId())")
    @Mapping(target = "resourceGroupId", expression = "java(reservation.getResourceGroup().getId())")
    @Mapping(target = "start", expression = "java(reservation.getStartTime())")
    @Mapping(target = "end", expression = "java(reservation.getEndTime())")
    ReservationDetailsDto reservationToDetailsDto(Reservation reservation);
}
