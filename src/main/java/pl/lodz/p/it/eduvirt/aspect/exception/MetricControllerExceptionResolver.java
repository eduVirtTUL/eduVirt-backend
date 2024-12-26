package pl.lodz.p.it.eduvirt.aspect.exception;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.lodz.p.it.eduvirt.exceptions.handle.ExceptionResponse;
import pl.lodz.p.it.eduvirt.exceptions.metric.MetricNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.metric.MetricValueAlreadyDefined;
import pl.lodz.p.it.eduvirt.exceptions.metric.MetricValueNotDefinedException;

@ControllerAdvice
@Order(10)
public class MetricControllerExceptionResolver {

    @ExceptionHandler({MetricValueAlreadyDefined.class})
    ResponseEntity<ExceptionResponse> handleMetricValueAlreadyDefinedValueException(
            MetricValueAlreadyDefined exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ExceptionResponse(exception.getMessage()));
    }

    @ExceptionHandler({MetricValueNotDefinedException.class})
    ResponseEntity<ExceptionResponse> handleMetricValueNotDefinedException(
            MetricValueNotDefinedException exception) {
        return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ExceptionResponse(exception.getMessage()));
    }

    @ExceptionHandler({MetricNotFoundException.class})
    ResponseEntity<ExceptionResponse> handleMetricNotFoundException(
            Exception exception) {
        return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ExceptionResponse(exception.getMessage()));
    }
}
