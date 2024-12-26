package pl.lodz.p.it.eduvirt.dto.nic;

import lombok.Builder;

import java.util.Objects;

@Builder
public record NicDto(String id, String name, String profileName, String segmentName, String macAddress) {

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (NicDto) obj;
        return Objects.equals(this.id, that.id) &&
                Objects.equals(this.name, that.name) &&
                Objects.equals(this.profileName, that.profileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, profileName);
    }

    @Override
    public String toString() {
        return "NicDto[" +
                "id=" + id + ", " +
                "name=" + name + ", " +
                "profileName=" + profileName + ']';
    }

}
