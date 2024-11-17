package pl.lodz.p.it.eduvirt.dto.pagination;

public record PageInfoDto(
        int page,
        int elements,
        int totalPages,
        long totalElements
) {}
