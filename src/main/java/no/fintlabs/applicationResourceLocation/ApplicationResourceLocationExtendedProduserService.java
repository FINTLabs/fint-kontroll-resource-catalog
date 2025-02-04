package no.fintlabs.applicationResourceLocation;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.cache.FintCache;
import no.fintlabs.kafka.entity.EntityProducer;
import no.fintlabs.kafka.entity.EntityProducerFactory;
import no.fintlabs.kafka.entity.EntityProducerRecord;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import no.fintlabs.kafka.entity.topic.EntityTopicService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ApplicationResourceLocationExtendedProduserService {
    private final EntityProducer entityProducer;
    private final EntityTopicNameParameters entityTopicNameParameters;
    private final FintCache<Long, ApplicationResourceLocationExtended> applicationResourceLocationExtendedCache;
    private final FintCache<Long, ApplicationResourceLocationExtended> publishedExtendedApplicationResourceLocation;

    public ApplicationResourceLocationExtendedProduserService(
            EntityProducerFactory entityProducerFactory,
            EntityTopicService entityTopicService,
            FintCache<Long, ApplicationResourceLocationExtended> publishedApplicationResourceLocationExtendedCache,
            FintCache<Long, ApplicationResourceLocationExtended> publishedExtendedApplicationResourceLocation) {
        entityProducer = entityProducerFactory.createProducer(ApplicationResourceLocationExtended.class);
        this.applicationResourceLocationExtendedCache = publishedApplicationResourceLocationExtendedCache;
        entityTopicNameParameters = EntityTopicNameParameters
                .builder()
                .resource("applicationresourcelocation-extended")
                .build();
        entityTopicService.ensureTopic(entityTopicNameParameters, 0);
        this.publishedExtendedApplicationResourceLocation = publishedExtendedApplicationResourceLocation;
    }

    public void publish(ApplicationResourceLocationExtended applicationResourceLocationExtended) {
        String key = applicationResourceLocationExtended.id().toString();
        log.info("Publishing extended applicationResourceLocation entity with id: {}", key);
        entityProducer.send(
                EntityProducerRecord.<ApplicationResourceLocationExtended>builder()
                        .topicNameParameters(entityTopicNameParameters)
                        .key(key)
                        .value(applicationResourceLocationExtended)
                        .build()
        );
    }

    public List<ApplicationResourceLocationExtended> publish(
            Long applicationResourceId,
            List<ApplicationResourceLocation> applicationResourceLocations
    ) {
        List<ApplicationResourceLocationExtended> publishedApplicationRessourceLocationsExtended = applicationResourceLocations
                .stream()
                .map(applicationResourceLocation -> createExtendedApplicationResourceLocation(applicationResourceId, applicationResourceLocation))
                .filter(applicationResourceLocationExtended -> publishedExtendedApplicationResourceLocation
                        .getOptional(applicationResourceLocationExtended.id())
                        .map(publishedInCache -> !publishedInCache.equals(applicationResourceLocationExtended))
                        .orElse(true)
                )
                .peek(this::publish)
                .toList();
        log.info("Published extended applicationResourceLocations: {} for applicationResource with id: {}", publishedApplicationRessourceLocationsExtended.size(), applicationResourceId);
        return publishedApplicationRessourceLocationsExtended;
    }


    public ApplicationResourceLocationExtended createExtendedApplicationResourceLocation(
            Long applicationResourceId, ApplicationResourceLocation applicationResourceLocation) {
        return new ApplicationResourceLocationExtended(
                applicationResourceLocation.getId(),
                applicationResourceId,
                applicationResourceLocation.getResourceId(),
                applicationResourceLocation.getOrgUnitId(),
                applicationResourceLocation.getOrgUnitName(),
                applicationResourceLocation.getResourceLimit());

    }
}
