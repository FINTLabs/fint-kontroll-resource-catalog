package no.fintlabs;

import com.fasterxml.jackson.databind.DatabindException;
import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.applicationResource.ApplicationResourceNotFoundExeption;
import no.fintlabs.authorization.ForbiddenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    private final Tracer tracer;

    public GlobalExceptionHandler(Tracer tracer) {
        this.tracer = tracer;
    }


    @ExceptionHandler(ApplicationResourceNotFoundExeption.class)
    public ResponseEntity<ErrorResponseBody> handleResourceNotFoundException(ApplicationResourceNotFoundExeption ex) {
        String correlationId = getCorrelationId();
        ErrorResponseBody errorResponse = new ErrorResponseBody(HttpStatus.NOT_FOUND.value(), ex.getMessage(), correlationId,  new Date());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponseBody> handleForbiddenException(ForbiddenException ex) {
        String correlationId = getCorrelationId();
        ErrorResponseBody errorResponse = new ErrorResponseBody(HttpStatus.FORBIDDEN.value(), ex.getMessage(), correlationId, new Date());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseBody> handleException(Exception ex) {
        log.error("An unhandled exception occurred with message: {}", ex.getMessage(), ex);
        String correlationId = getCorrelationId();
        ErrorResponseBody errorResponse = new ErrorResponseBody(HttpStatus.INTERNAL_SERVER_ERROR.value(), "En uventet serverfeil oppstod", correlationId,  new Date() );
        log.info("Returning error response: {}", errorResponse);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    private String getCorrelationId() {
        Optional<String> traceId = Optional.of(Objects.requireNonNull(tracer.currentSpan()).context().traceId());

        return traceId.orElse(null);
    }
}