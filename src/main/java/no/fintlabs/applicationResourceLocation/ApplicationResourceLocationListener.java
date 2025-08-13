package no.fintlabs.applicationResourceLocation;

import jakarta.persistence.PreRemove;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ApplicationResourceLocationListener {

    private static ApplicationResourceLocationExtendedProduserService kafkaProducer;

    // Inject the producer statically (Spring doesn’t manage entity listeners directly)
    @Autowired
    public void init(ApplicationResourceLocationExtendedProduserService producer) {
        ApplicationResourceLocationListener.kafkaProducer = producer;
    }

    @PreRemove
    public void onRemove(ApplicationResourceLocation entity) {
        kafkaProducer.onRemove(entity);
    }
}