package no.fintlabs.resourceGroup;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.applicationResource.ApplicationResource;
import no.fintlabs.cache.FintCache;
import no.fintlabs.kafka.entity.EntityProducer;
import no.fintlabs.kafka.entity.EntityProducerFactory;
import no.fintlabs.kafka.entity.EntityProducerRecord;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import no.fintlabs.kafka.entity.topic.EntityTopicService;
import org.springframework.stereotype.Service;

import java.util.List;
@Slf4j
@Service
public class ResourceGroupProducerService {
    private final EntityProducer<ApplicationResource> entityProducer;
    private final EntityTopicNameParameters entityTopicNameParameters;
    private final FintCache<Long,ApplicationResource> publishedApplicationResourceCache;

    public ResourceGroupProducerService(
            EntityProducerFactory entityProducerFactory,
            EntityTopicService entityTopicService, FintCache<Long, ApplicationResource> publishedApplicationResourceCache
    ) {
        entityProducer = entityProducerFactory.createProducer(ApplicationResource.class);
        this.publishedApplicationResourceCache = publishedApplicationResourceCache;
        entityTopicNameParameters = EntityTopicNameParameters
                .builder()
                .resource("resource-group")
                .build();
        entityTopicService.ensureTopic(entityTopicNameParameters, 0);
    }
    public void publish(ApplicationResource applicationResource) {
        String key = applicationResource.getId().toString();
        log.debug("Publishing resourceGroup with id: {}", key);
        entityProducer.send(
                EntityProducerRecord.<ApplicationResource>builder()
                        .topicNameParameters(entityTopicNameParameters)
                        .key(key)
                        .value(applicationResource)
                        .build()
        );
    }
    public List<ApplicationResource> publishResourceGroups (List<ApplicationResource> applicationResources) {
        log.info("Number of entities in cache: {}", publishedApplicationResourceCache.getNumberOfEntries());
        List<ApplicationResource> publishedApplicationResources =  applicationResources
                .stream()
                .filter(applicationResource -> publishedApplicationResourceCache
                        .getOptional(applicationResource.getId())
                        .map(publishedApplicationResourceInCache -> !publishedApplicationResourceInCache.equals(applicationResource))
                        .orElse(true)
                )
                .peek(this::publish)
                .toList();
        log.info("Published application resources: {}", publishedApplicationResources.size());
        return publishedApplicationResources;
    }


}
