package pl.lodz.p.it.eduvirt.dto.vm;

import lombok.Getter;
import lombok.Setter;
import pl.lodz.p.it.eduvirt.dto.nic.NicDto;

import java.util.List;
import java.util.Objects;

@Getter
public final class VmDto {
    private final String id;
    private final String name;
    @Setter
    private List<NicDto> nics;

    public VmDto(String id, String name, List<NicDto> nics) {
        this.id = id;
        this.name = name;
        this.nics = nics;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public List<NicDto> nics() {
        return nics;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (VmDto) obj;
        return Objects.equals(this.id, that.id) &&
                Objects.equals(this.name, that.name) &&
                Objects.equals(this.nics, that.nics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, nics);
    }

    @Override
    public String toString() {
        return "VmDto[" +
                "id=" + id + ", " +
                "name=" + name + ", " +
                "nics=" + nics + ']';
    }

}
