package pl.lodz.p.it.eduvirt.aspect.exception;

import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.lodz.p.it.eduvirt.exceptions.handle.ExceptionResponse;
import pl.lodz.p.it.eduvirt.exceptions.maintenance_interval.MaintenanceIntervalInvalidTimeWindowException;
import pl.lodz.p.it.eduvirt.exceptions.maintenance_interval.MaintenanceIntervalNotFound;

@ControllerAdvice
@Order(10)
public class MaintenanceIntervalExceptionResolver {

    @ExceptionHandler({
            MaintenanceIntervalNotFound.class,
            MaintenanceIntervalInvalidTimeWindowException.class
    })
    ResponseEntity<ExceptionResponse> handleGeneralMaintenanceIntervalExceptions(
            Exception exception) {
        return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ExceptionResponse(exception.getMessage()));
    }
}
