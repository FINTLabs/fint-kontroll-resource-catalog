package no.fintlabs.applicationResource;

public class ApplicationResourceNotFoundExeption extends Exception{
    public ApplicationResourceNotFoundExeption(Long id) {
        super(String.format("ApplicationResourceNotFoundExeption - applicationResource with id : %s does not exists: ",id));
    }
}
