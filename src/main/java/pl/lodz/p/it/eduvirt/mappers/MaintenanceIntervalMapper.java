package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import pl.lodz.p.it.eduvirt.dto.maintenance_interval.MaintenanceIntervalDetailsDto;
import pl.lodz.p.it.eduvirt.dto.maintenance_interval.MaintenanceIntervalDto;
import pl.lodz.p.it.eduvirt.entity.eduvirt.reservation.MaintenanceInterval;

@Mapper(componentModel = "spring")
public interface MaintenanceIntervalMapper {

    MaintenanceIntervalDto maintenanceIntervalToDto(MaintenanceInterval maintenanceInterval);
    MaintenanceIntervalDetailsDto maintenanceIntervalToDetailsDto(MaintenanceInterval maintenanceInterval);
}
