package pl.lodz.p.it.eduvirt.dto.pagination;

import lombok.Builder;

import java.util.List;

@Builder
public record PageDto<T>(
        List<T> items,
        PageInfoDto page
) {
}
