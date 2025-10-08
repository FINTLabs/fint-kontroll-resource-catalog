package no.fintlabs.applicationResource;

import no.fintlabs.exception.KontrollException;

public class ApplicationResourceNotFoundException extends KontrollException {

    public ApplicationResourceNotFoundException(Long id) {
        super(String.format("ApplicationResourceNotFoundExeption - applicationResource with id : %s does not exists: ", id));
    }

    @Override
    public String getTypeIdentifier() {
        return "application-resource-not-found";
    }
}
