package no.fintlabs.applicationResource;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NoApplicationResourcesFoundException extends RuntimeException{
    public NoApplicationResourcesFoundException() {
        super("Ingen tilgjengelige ressurser funnet");
    }
}
