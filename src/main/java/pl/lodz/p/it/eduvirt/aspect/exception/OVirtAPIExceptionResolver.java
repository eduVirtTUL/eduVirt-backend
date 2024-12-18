package pl.lodz.p.it.eduvirt.aspect.exception;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.lodz.p.it.eduvirt.exceptions.ClusterNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.handle.ExceptionResponse;

@ControllerAdvice
public class OVirtAPIExceptionResolver {

    @ExceptionHandler({ClusterNotFoundException.class})
    ResponseEntity<ExceptionResponse> handleOperationNotImplementedException(
            ClusterNotFoundException exception) {
        return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ExceptionResponse(exception.getMessage()));
    }
}
