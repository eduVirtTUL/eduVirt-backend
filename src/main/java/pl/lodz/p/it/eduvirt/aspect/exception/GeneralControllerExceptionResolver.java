package pl.lodz.p.it.eduvirt.aspect.exception;

import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.lodz.p.it.eduvirt.exceptions.ApplicationOperationNotImplementedException;
import pl.lodz.p.it.eduvirt.exceptions.handle.ExceptionResponse;

@ControllerAdvice
@Order(50)
public class GeneralControllerExceptionResolver {

    @ExceptionHandler({ApplicationOperationNotImplementedException.class})
    ResponseEntity<ExceptionResponse> handleOperationNotImplementedException(
            ApplicationOperationNotImplementedException exception) {
        return ResponseEntity.internalServerError()
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ExceptionResponse(exception.getMessage()));
    }
}
