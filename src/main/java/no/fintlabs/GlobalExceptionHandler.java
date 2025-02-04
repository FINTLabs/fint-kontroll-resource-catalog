package no.fintlabs;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.applicationResource.ApplicationResourceNotFoundExeption;
import no.fintlabs.authorization.ForbiddenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApplicationResourceNotFoundExeption.class)
    public ResponseEntity<ErrorResponseBody> handleResourceNotFoundException(ApplicationResourceNotFoundExeption ex) {
        ErrorResponseBody errorResponse = new ErrorResponseBody(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponseBody> handleForbiddenException(ForbiddenException ex) {
        ErrorResponseBody errorResponse = new ErrorResponseBody(HttpStatus.FORBIDDEN.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseBody> handleException(Exception ex) {
        log.error("An unhandled exception occurred with message: {}", ex.getMessage(), ex);
        ErrorResponseBody errorResponse = new ErrorResponseBody(HttpStatus.INTERNAL_SERVER_ERROR.value(), "En uventet serverfeil oppstod") ;
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}