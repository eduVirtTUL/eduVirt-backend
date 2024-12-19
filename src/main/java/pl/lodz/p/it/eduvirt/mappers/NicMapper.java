package pl.lodz.p.it.eduvirt.mappers;

import org.ovirt.engine.sdk4.types.Nic;
import pl.lodz.p.it.eduvirt.dto.nic.NicDto;

import java.util.List;
import java.util.stream.Stream;

public interface NicMapper {
    NicDto nicToDto(Nic nic);

    List<NicDto> nicsToDtos(Stream<Nic> nics);
}
