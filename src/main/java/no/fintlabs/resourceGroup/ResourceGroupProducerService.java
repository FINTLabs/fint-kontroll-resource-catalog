package no.fintlabs.resourceGroup;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.applicationResource.ApplicationResource;
import no.fintlabs.cache.FintCache;
import no.novari.kafka.producing.ParameterizedProducerRecord;
import no.novari.kafka.producing.ParameterizedTemplate;
import no.novari.kafka.producing.ParameterizedTemplateFactory;
import no.novari.kafka.topic.EntityTopicService;
import no.novari.kafka.topic.EventTopicService;
import no.novari.kafka.topic.configuration.EntityCleanupFrequency;
import no.novari.kafka.topic.configuration.EntityTopicConfiguration;
import no.novari.kafka.topic.configuration.EventCleanupFrequency;
import no.novari.kafka.topic.configuration.EventTopicConfiguration;
import no.novari.kafka.topic.name.EntityTopicNameParameters;
import no.novari.kafka.topic.name.EventTopicNameParameters;
import no.novari.kafka.topic.name.TopicNamePrefixParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class ResourceGroupProducerService {
    private static final String RESOURCE_GROUP_TOPIC = "resource-group";
    private static final String RESOURCE_GROUP_MS_GRAPH_TOPIC = "resource-group";

    private final ParameterizedTemplate<ApplicationResource> resourceGroupTemplate;
    private final ParameterizedTemplate<ResourceGroup> resourceGroupMsGraphTemplate;
    private final EntityTopicNameParameters resourceGroupTopicNameParameters;
    private final EventTopicNameParameters resourceGroupMsGraphTopicNameParameters;
    private final FintCache<Long, Integer> publishedApplicationResourceCache;

    public ResourceGroupProducerService(
            EntityTopicService entityTopicService,
            EventTopicService eventTopicService,
            FintCache<Long, Integer> publishedApplicationResourceCache,
            ParameterizedTemplateFactory parameterizedTemplateFactory
    ) {
        this.resourceGroupTemplate = parameterizedTemplateFactory.createTemplate(ApplicationResource.class);
        this.resourceGroupMsGraphTemplate = parameterizedTemplateFactory.createTemplate(ResourceGroup.class);
        this.publishedApplicationResourceCache = publishedApplicationResourceCache;

        TopicNamePrefixParameters topicNamePrefixParameters = TopicNamePrefixParameters
                .stepBuilder()
                .orgIdApplicationDefault()
                .domainContextApplicationDefault()
                .build();

        resourceGroupTopicNameParameters = EntityTopicNameParameters
                .builder()
                .topicNamePrefixParameters(topicNamePrefixParameters)
                .resourceName(RESOURCE_GROUP_TOPIC)
                .build();

        resourceGroupMsGraphTopicNameParameters = EventTopicNameParameters
                .builder()
                .topicNamePrefixParameters(topicNamePrefixParameters)
                .eventName(RESOURCE_GROUP_MS_GRAPH_TOPIC)
                .build();

        entityTopicService.createOrModifyTopic(resourceGroupTopicNameParameters, EntityTopicConfiguration.stepBuilder()
                .partitions(1)
                .lastValueRetainedForever()
                .nullValueRetentionTime(Duration.ofDays(7))
                .cleanupFrequency(EntityCleanupFrequency.NORMAL)
                .build()
        );

        eventTopicService.createOrModifyTopic(resourceGroupMsGraphTopicNameParameters, EventTopicConfiguration.stepBuilder()
                .partitions(1)
                .retentionTime(Duration.ofDays(7))
                .cleanupFrequency(EventCleanupFrequency.NORMAL)
                .build()
        );
    }

    public void publish(ApplicationResource applicationResource) {
        publishResourceGroup(applicationResource);
        publishResourceGroupMsGraph(applicationResource);
    }

    public void publish(ApplicationResource applicationResource, boolean publishMsGraph) {
        publishResourceGroup(applicationResource);
        if (publishMsGraph) {
            publishResourceGroupMsGraph(applicationResource);
        }
    }

    public void publishResourceGroup(ApplicationResource applicationResource) {
        String key = applicationResource.getId().toString();
        log.debug("Publishing resource-group entity with id: {}", key);
        resourceGroupTemplate.send(
                ParameterizedProducerRecord.<ApplicationResource>builder()
                        .topicNameParameters(resourceGroupTopicNameParameters)
                        .key(key)
                        .value(applicationResource)
                        .build()
        );
        publishedApplicationResourceCache.put(applicationResource.getId(), applicationResource.hashCode());
    }

    public void publishResourceGroupMsGraph(ApplicationResource applicationResource) {
        if (shouldSkipMsGraphPublish(applicationResource)) {
            log.debug(
                    "Skipping event.resource-group command for deleted resource group with id: {} because idpGroupObjectId is empty",
                    applicationResource.getId()
            );
            return;
        }

        String key = UUID.randomUUID().toString();
        ResourceGroup resourceGroup = toResourceGroup(applicationResource);
        log.debug(
                "Publishing event.resource-group command with traceId: {}, resourceGroupId: {}, operation: {}",
                key,
                applicationResource.getId(),
                resourceGroup.getOperation()
        );
        resourceGroupMsGraphTemplate.send(
                ParameterizedProducerRecord.<ResourceGroup>builder()
                        .topicNameParameters(resourceGroupMsGraphTopicNameParameters)
                        .key(key)
                        .value(resourceGroup)
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
                .peek(this::publishResourceGroup)
                .toList();

        log.debug("Published application resources: {}", toPublish.size());
        return toPublish;
    }

    public List<ApplicationResource> publishResourceGroupsMsGraph(List<ApplicationResource> applicationResources) {
        List<ApplicationResource> publishedApplicationResources = applicationResources.stream()
                .filter(applicationResource -> !shouldSkipMsGraphPublish(applicationResource))
                .peek(this::publishResourceGroupMsGraph)
                .toList();

        log.info("Published {} resource groups to event.resource-group", publishedApplicationResources.size());
        return publishedApplicationResources;
    }

    private ResourceGroup toResourceGroup(ApplicationResource applicationResource) {
        ResourceGroupOperation operation = resolveOperation(applicationResource);

        return ResourceGroup.builder()
                .operation(operation)
                .resourceId(applicationResource.getId().toString())
                .idpGroupObjectId(applicationResource.getIdentityProviderGroupObjectId() == null
                        ? null
                        : applicationResource.getIdentityProviderGroupObjectId().toString())
                .resourceName(applicationResource.getResourceName())
                .build();
    }

    private ResourceGroupOperation resolveOperation(ApplicationResource applicationResource) {
        if ("DELETED".equalsIgnoreCase(applicationResource.getStatus())) {
            return ResourceGroupOperation.DELETE;
        }

        return applicationResource.getIdentityProviderGroupObjectId() == null
                ? ResourceGroupOperation.CREATE
                : ResourceGroupOperation.UPDATE;
    }

    private boolean shouldSkipMsGraphPublish(ApplicationResource applicationResource) {
        return "DELETED".equalsIgnoreCase(applicationResource.getStatus());
    }

    public List<ApplicationResource> publishAllResourceGroups(List<ApplicationResource> applicationResources) {
        List<ApplicationResource> publishedApplicationResources = applicationResources.stream()
                .peek(this::publishResourceGroup)
                .toList();

        log.debug("Published all application resources: {}", publishedApplicationResources.size());
        return publishedApplicationResources;
    }
}
