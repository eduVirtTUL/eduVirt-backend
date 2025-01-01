package pl.lodz.p.it.eduvirt.exceptions.handle;

import java.util.Set;

public record ConstraintViolationDto(
        String message,
        String key,
        Set<String> violations
) {}
