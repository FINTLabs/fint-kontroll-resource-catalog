package no.fintlabs.resourceGroup;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.applicationResource.ApplicationResource;
import no.fintlabs.cache.FintCache;
import no.novari.kafka.producing.ParameterizedProducerRecord;
import no.novari.kafka.producing.ParameterizedTemplate;
import no.novari.kafka.producing.ParameterizedTemplateFactory;
import no.novari.kafka.topic.EntityTopicService;
import no.novari.kafka.topic.configuration.EntityCleanupFrequency;
import no.novari.kafka.topic.configuration.EntityTopicConfiguration;
import no.novari.kafka.topic.name.EntityTopicNameParameters;
import no.novari.kafka.topic.name.TopicNamePrefixParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class ResourceGroupProducerService {
    private final ParameterizedTemplate<ApplicationResource> parameterizedTemplate;
    private final EntityTopicNameParameters entityTopicNameParameters;
    private final FintCache<Long, Integer> publishedApplicationResourceCache;

    public ResourceGroupProducerService(
            EntityTopicService entityTopicService,
            FintCache<Long, Integer> publishedApplicationResourceCache,
            ParameterizedTemplateFactory parameterizedTemplateFactory
    ) {
        this.parameterizedTemplate = parameterizedTemplateFactory.createTemplate(ApplicationResource.class);
        this.publishedApplicationResourceCache = publishedApplicationResourceCache;
        entityTopicNameParameters = EntityTopicNameParameters
                .builder()
                .topicNamePrefixParameters(TopicNamePrefixParameters
                        .stepBuilder()
                        .orgIdApplicationDefault()
                        .domainContextApplicationDefault()
                        .build())
                .resourceName("resource-group")
                .build();
        entityTopicService.createOrModifyTopic(entityTopicNameParameters,EntityTopicConfiguration.stepBuilder()
                .partitions(1)
                .lastValueRetainedForever()
                .nullValueRetentionTime(Duration.ofDays(7))
                .cleanupFrequency(EntityCleanupFrequency.NORMAL)
                .build()
        );
    }

    public void publish(ApplicationResource applicationResource) {
        String key = applicationResource.getId().toString();
        log.debug("Publishing resourceGroup with id: {}", key);
        parameterizedTemplate.send(
                ParameterizedProducerRecord.<ApplicationResource>builder()
                        .topicNameParameters(entityTopicNameParameters)
                        .key(key)
                        .value(applicationResource)
                        .build()
        );
    }

    public List<ApplicationResource> publishResourceGroups(List<ApplicationResource> applicationResources) {
       log.debug("Number of entities in cache: {}", publishedApplicationResourceCache.getNumberOfEntries());

        List<ApplicationResource> toPublish = applicationResources.stream()
                .filter(ar -> {
                    Long id = ar.getId();
                    int currentHash = ar.hashCode();
                    return publishedApplicationResourceCache
                            .getOptional(id)
                            .map(cachedHash -> !Objects.equals(cachedHash, currentHash))
                            .orElse(true);
                })
                .peek(this::publish)
                .toList();

        log.debug("Published application resources: {}", toPublish.size());
        return toPublish;
    }
}
