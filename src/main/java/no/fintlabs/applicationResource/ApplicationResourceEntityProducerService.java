package no.fintlabs.applicationResource;

import no.fintlabs.kafka.entity.EntityProducer;
import no.fintlabs.kafka.entity.EntityProducerFactory;
import no.fintlabs.kafka.entity.EntityProducerRecord;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import no.fintlabs.kafka.entity.topic.EntityTopicService;
import org.springframework.stereotype.Service;

@Service
public class ApplicationResourceEntityProducerService {
    private final EntityProducer<ApplicationResource> entityProducer;
    private final EntityTopicNameParameters entityTopicNameParameters;

    public ApplicationResourceEntityProducerService(
            EntityProducerFactory entityProducerFactory,
            EntityTopicService entityTopicService
    ) {
        entityProducer = entityProducerFactory.createProducer(ApplicationResource.class);
        entityTopicNameParameters = EntityTopicNameParameters
                .builder()
                .resource("resource-group")
                .build();
        entityTopicService.ensureTopic(entityTopicNameParameters, 0);
    }
    public void publish(ApplicationResource applicationResource) {
        String key = applicationResource.getId().toString();
        entityProducer.send(
                EntityProducerRecord.<ApplicationResource>builder()
                        .topicNameParameters(entityTopicNameParameters)
                        .key(key)
                        .value(applicationResource)
                        .build()
        );
    }
}
