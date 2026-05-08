package no.fintlabs.applicationResourceLocation;

import lombok.extern.slf4j.Slf4j;
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

@Service
@Slf4j
public class ApplicationResourceLocationExtendedProduserService {
    private final ParameterizedTemplate<ApplicationResourceLocationExtended> parameterizedTemplate;
    private final EntityTopicNameParameters entityTopicNameParameters;
    //private final FintCache<Long, ApplicationResourceLocationExtended> applicationResourceLocationExtendedCache;
    private final FintCache<Long, ApplicationResourceLocationExtended> publishedExtendedApplicationResourceLocation;

    public ApplicationResourceLocationExtendedProduserService(
            ParameterizedTemplateFactory parameterizedTemplateFactory,
            EntityTopicService entityTopicService,
            //FintCache<Long, ApplicationResourceLocationExtended> publishedApplicationResourceLocationExtendedCache,
            FintCache<Long, ApplicationResourceLocationExtended> publishedExtendedApplicationResourceLocation
    ) {
        this.parameterizedTemplate = parameterizedTemplateFactory.createTemplate(ApplicationResourceLocationExtended.class);
        //this.applicationResourceLocationExtendedCache = publishedApplicationResourceLocationExtendedCache;
        this.publishedExtendedApplicationResourceLocation = publishedExtendedApplicationResourceLocation;
        entityTopicNameParameters = EntityTopicNameParameters
                .builder()
                .topicNamePrefixParameters(TopicNamePrefixParameters
                        .stepBuilder()
                        .orgIdApplicationDefault()
                        .domainContextApplicationDefault()
                        .build())
                .resourceName("applicationresourcelocation-extended")
                .build();
        entityTopicService.createOrModifyTopic(entityTopicNameParameters,EntityTopicConfiguration.stepBuilder()
                .partitions(1)
                .lastValueRetainedForever()
                .nullValueRetentionTime(Duration.ofDays(7))
                .cleanupFrequency(EntityCleanupFrequency.NORMAL)
                .build()
        );

    }

    public void publish(ApplicationResourceLocationExtended applicationResourceLocationExtended) {
        String key = applicationResourceLocationExtended.id().toString();
        log.debug("Publishing extended applicationResourceLocation entity with id: {}", key);
        parameterizedTemplate.send(
                ParameterizedProducerRecord.<ApplicationResourceLocationExtended>builder()
                        .topicNameParameters(entityTopicNameParameters)
                        .key(key)
                        .value(applicationResourceLocationExtended)
                        .build()
        );
    }

    public void onRemove(ApplicationResourceLocation applicationResourceLocation) {
        String key = applicationResourceLocation.getId().toString();
        log.info("Publishing removal of extended applicationResourceLocation entity with id: {}", key);
        parameterizedTemplate.send(
                ParameterizedProducerRecord.<ApplicationResourceLocationExtended>builder()
                        .topicNameParameters(entityTopicNameParameters)
                        .key(key)
                        .value(null)
                        .build()
        );
    }

    public List<ApplicationResourceLocationExtended> publish(Long applicationResourceId, List<ApplicationResourceLocation> applicationResourceLocations) {
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
                applicationResourceLocation.getResourceLimit(),
                applicationResourceLocation.isTopOrgunit());

    }
}
