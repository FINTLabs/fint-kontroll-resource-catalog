package no.fintlabs.applicationResourceLocation;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.kafka.entity.EntityProducer;
import no.fintlabs.kafka.entity.EntityProducerFactory;
import no.fintlabs.kafka.entity.EntityProducerRecord;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import no.fintlabs.kafka.entity.topic.EntityTopicService;
import org.springframework.stereotype.Service;

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
        entityProducer = entityProducerFactory.createProducer(ApplicationResourceLocationExtended.class);
        entityTopicNameParameters = EntityTopicNameParameters
                .builder()
                .resource("applicationresourcelocation-extended")
                .build();
        entityTopicService.ensureTopic(entityTopicNameParameters, 0);
    }

    public void publish(ApplicationResourceLocationExtended applicationResourceLocationExtended) {
        String key = applicationResourceLocationExtended.id().toString();
        log.info("Publishing applicationResourceLocationDTO entity with id: {}", key);
        entityProducer.send(
                EntityProducerRecord.<ApplicationResourceLocationExtended>builder()
                        .topicNameParameters(entityTopicNameParameters)
                        .key(key)
                        .value(applicationResourceLocationExtended)
                        .build()
        );
    }

    public List<ApplicationResourceLocationExtended> publish(Long applicationResourceId, List<ApplicationResourceLocation> applicationResourceLocations) {
        List<ApplicationResourceLocationExtended> publishedApplicationRessourceLocationsExtended = applicationResourceLocations
                .stream()
                .map(applicationResourceLocation -> createExtendedApplicationResourceLocation(applicationResourceId,applicationResourceLocation))
                .peek(this::publish)
                .toList();
        log.info("Published applicationResourceLocations: {}", publishedApplicationRessourceLocationsExtended.size());
        return publishedApplicationRessourceLocationsExtended;
    }


    public ApplicationResourceLocationExtended createExtendedApplicationResourceLocation(
            Long applicationResourceId,ApplicationResourceLocation applicationResourceLocation) {
        return new ApplicationResourceLocationExtended(
                applicationResourceLocation.getId(),
                applicationResourceId,
                applicationResourceLocation.getResourceId(),
                applicationResourceLocation.getOrgUnitId(),
                applicationResourceLocation.getOrgUnitName(),
                applicationResourceLocation.getResourceLimit());

    }
}
