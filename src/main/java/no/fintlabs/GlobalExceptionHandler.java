package no.fintlabs;

import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.applicationResource.ApplicationResourceNotFoundExeption;
import no.fintlabs.applicationResource.NoApplicationResourcesFoundException;
import no.fintlabs.authorization.ForbiddenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    private final Tracer tracer;

    public GlobalExceptionHandler(Tracer tracer) {
        this.tracer = tracer;
    }

    @ExceptionHandler({ApplicationResourceNotFoundExeption.class})
    public ProblemDetail handleResourceNotFoundException(ApplicationResourceNotFoundExeption ex) {
        return getProblemDetail(ex, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({NoApplicationResourcesFoundException.class})
    public ProblemDetail handleNoApplicationResourcesFoundException(NoApplicationResourcesFoundException ex) {
        return getProblemDetail(ex, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ProblemDetail handleForbiddenException(ForbiddenException ex) {
        return getProblemDetail(ex, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception ex) {
        log.error("An unhandled exception occurred with message: {}", ex.getMessage(), ex);
        String correlationId = getCorrelationId();
        ProblemDetail problemDetail = getProblemDetail(ex, HttpStatus.INTERNAL_SERVER_ERROR);
        problemDetail.setDetail("En uventet serverfeil oppstod");

        log.warn("Returning error response: {}", problemDetail);
        return problemDetail;
    }

    private String getCorrelationId() {
        Optional<String> traceId = Optional.of(Objects.requireNonNull(tracer.currentSpan()).context().traceId());
        return traceId.orElse(null);
    }

    private ProblemDetail getProblemDetail(Throwable ex, HttpStatus status) {
        String correlationId = getCorrelationId();
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problemDetail.setProperty("correlationId", correlationId);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }
}