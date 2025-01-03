package pl.lodz.p.it.eduvirt.aspect.exception;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.lodz.p.it.eduvirt.exceptions.general.*;
import pl.lodz.p.it.eduvirt.exceptions.handle.ExceptionResponse;

@ControllerAdvice
@Order(50)
public class GeneralControllerExceptionResolver {

    @ExceptionHandler({AlreadyExistsException.class})
    ResponseEntity<ExceptionResponse> handleAlreadyExistsException(
            AlreadyExistsException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ExceptionResponse(exception.getMessage(), exception.getKey()));
    }

    @ExceptionHandler({BadRequestException.class})
    ResponseEntity<ExceptionResponse> handleBadRequestException(
            BadRequestException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ExceptionResponse(exception.getMessage(), exception.getKey()));
    }

    @ExceptionHandler({ConflictException.class})
    ResponseEntity<ExceptionResponse> handleConflictException(
            ConflictException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ExceptionResponse(exception.getMessage(), exception.getKey()));
    }

    @ExceptionHandler({NotFoundException.class})
    ResponseEntity<ExceptionResponse> handleNotFoundException(
            NotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ExceptionResponse(exception.getMessage(), exception.getKey()));
    }

    @ExceptionHandler({OperationNotImplementedException.class, OpeningConnectionException.class})
    ResponseEntity<ExceptionResponse> handleServerErrors(ApplicationBaseException exception) {
        return ResponseEntity.internalServerError()
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ExceptionResponse(exception.getMessage(), exception.getKey()));
    }

}
