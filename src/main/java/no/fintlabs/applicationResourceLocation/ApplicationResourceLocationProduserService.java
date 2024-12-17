package no.fintlabs.applicationResourceLocation;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.applicationResource.ApplicationResource;
import no.fintlabs.kafka.entity.EntityProducer;
import no.fintlabs.kafka.entity.EntityProducerFactory;
import no.fintlabs.kafka.entity.EntityProducerRecord;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import no.fintlabs.kafka.entity.topic.EntityTopicService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ApplicationResourceLocationProduserService {
    private final EntityProducer entityProducer;
    private final EntityTopicNameParameters entityTopicNameParameters;

    public ApplicationResourceLocationProduserService(
            EntityProducerFactory entityProducerFactory,
            EntityTopicService entityTopicService
    ) {
        entityProducer = entityProducerFactory.createProducer(ApplicationResourceLocation.class);
        entityTopicNameParameters = EntityTopicNameParameters
                .builder()
                .resource("applicationresourcelocation")
                .build();
        entityTopicService.ensureTopic(entityTopicNameParameters, 0);
    }

    public void publish(ApplicationResourceLocation applicationResourceLocation) {
        String key = applicationResourceLocation.getId().toString();
        log.info("Publishing applicationResourceLocation entity with id: {}", key);
        entityProducer.send(
                EntityProducerRecord.<ApplicationResourceLocation>builder()
                        .topicNameParameters(entityTopicNameParameters)
                        .key(key)
                        .value(applicationResourceLocation)
                        .build()
        );
    }

    public List<ApplicationResourceLocation> publish(List<ApplicationResourceLocation> applicationResourceLocations) {
        List<ApplicationResourceLocation> publishedApplicationRessourceLocations = applicationResourceLocations
                .stream()
                .peek(this::publish)
                .toList();
        return publishedApplicationRessourceLocations;
    }
}
