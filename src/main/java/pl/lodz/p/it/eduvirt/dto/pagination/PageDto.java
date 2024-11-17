package pl.lodz.p.it.eduvirt.dto.pagination;

import java.util.List;

public record PageDto<T>(
        List<T> items,
        PageInfoDto page
) {}
