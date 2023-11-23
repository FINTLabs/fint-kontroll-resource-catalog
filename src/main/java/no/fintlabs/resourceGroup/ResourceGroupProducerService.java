package no.fintlabs.resourceGroup;

import no.fintlabs.applicationResource.ApplicationResource;
import no.fintlabs.kafka.entity.EntityProducer;
import no.fintlabs.kafka.entity.EntityProducerFactory;
import no.fintlabs.kafka.entity.EntityProducerRecord;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import no.fintlabs.kafka.entity.topic.EntityTopicService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResourceGroupProducerService {
    private final EntityProducer<ApplicationResource> entityProducer;
    private final EntityTopicNameParameters entityTopicNameParameters;

    public ResourceGroupProducerService(
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
    public List<ApplicationResource> publishResourceGroups (List<ApplicationResource> applicationResources) {
        return applicationResources
                .stream()
                .peek(this::publish)
                .toList();
    }


}
