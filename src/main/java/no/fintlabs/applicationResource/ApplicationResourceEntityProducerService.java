package no.fintlabs.applicationResource;

import no.fintlabs.kafka.entity.EntityProducer;
import no.fintlabs.kafka.entity.EntityProducerFactory;
import no.fintlabs.kafka.entity.EntityProducerRecord;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import org.springframework.stereotype.Service;

@Service
public class ApplicationResourceEntityProducerService {
    private final EntityProducer<ApplicationResourceDTOSimplified> entityProducer;
    private final EntityTopicNameParameters entityTopicNameParameters;

    public ApplicationResourceEntityProducerService(EntityProducerFactory entityProducerFactory) {
        entityProducer = entityProducerFactory.createProducer(ApplicationResourceDTOSimplified.class);
        entityTopicNameParameters = EntityTopicNameParameters
                .builder()
                .resource("resource-group")
                .build();
    }
    public void publish(ApplicationResource applicationResource) {
        String key = applicationResource.getResourceId();
        entityProducer.send(
                EntityProducerRecord.<ApplicationResourceDTOSimplified>builder()
                        .topicNameParameters(entityTopicNameParameters)
                        .key(key)
                        .value(applicationResource.toApplicationResourceDTOSimplified())
                        .build()
        );
    }
}
