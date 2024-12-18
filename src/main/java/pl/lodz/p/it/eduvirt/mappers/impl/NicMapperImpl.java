package pl.lodz.p.it.eduvirt.mappers.impl;

import org.ovirt.engine.sdk4.types.Nic;
import org.springframework.stereotype.Component;
import pl.lodz.p.it.eduvirt.dto.nic.NicDto;
import pl.lodz.p.it.eduvirt.mappers.NicMapper;

import java.util.List;
import java.util.stream.Stream;

@Component
public class NicMapperImpl implements NicMapper {
    @Override
    public NicDto nicToDto(Nic nic) {
        return NicDto.builder().id(nic.id()).name(nic.name()).macAddress(nic.mac().address()).profileName(nic.vnicProfilePresent() ? nic.vnicProfile().name() : null).build();
    }

    @Override
    public List<NicDto> nicsToDtos(Stream<Nic> nics) {
        return nics.map(this::nicToDto).toList();
    }
}
