package pl.lodz.p.it.eduvirt.aspect.exception;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.lodz.p.it.eduvirt.exceptions.AlreadyExistsException;
import pl.lodz.p.it.eduvirt.exceptions.ApplicationOperationNotImplementedException;
import pl.lodz.p.it.eduvirt.exceptions.NotFoundException;
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

    @ExceptionHandler({NotFoundException.class})
    ResponseEntity<ExceptionResponse> handleNotFoundException(
            NotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ExceptionResponse(exception.getMessage()));
    }

    @ExceptionHandler({AlreadyExistsException.class})
    ResponseEntity<ExceptionResponse> handleAlreadyExistsException(
            AlreadyExistsException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ExceptionResponse(exception.getMessage()));
    }
}
