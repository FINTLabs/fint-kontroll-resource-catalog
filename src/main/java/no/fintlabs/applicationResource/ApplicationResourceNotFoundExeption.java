package no.fintlabs.applicationResource;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ApplicationResourceNotFoundExeption extends RuntimeException{
    public ApplicationResourceNotFoundExeption(Long id) {
        super("Ressurs med id: " +id+ " ble ikke funnet");
    }
}
