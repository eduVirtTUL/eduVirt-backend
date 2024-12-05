package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.lodz.p.it.eduvirt.dto.reservation.ReservationDetailsDto;
import pl.lodz.p.it.eduvirt.dto.reservation.ReservationDto;
import pl.lodz.p.it.eduvirt.entity.eduvirt.reservation.Reservation;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    @Mapping(target = "teamId", expression = "java(vm.getTeam().getId())")
    ReservationDto reservationToDto(Reservation reservation);

    @Mapping(target = "teamId", expression = "java(vm.getTeam().getId())")
    @Mapping(target = "resourceGroupId", expression = "java(vm.getResourceGroup().getId())")
    ReservationDetailsDto reservationToDetailsDto(Reservation reservation);
}
