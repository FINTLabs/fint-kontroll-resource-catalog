package no.fintlabs.resourceGroup;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.applicationResource.ApplicationResource;
import no.fintlabs.applicationResourceLocation.ApplicationResourceLocation;
import no.fintlabs.cache.FintCache;
import no.fintlabs.kodeverk.applikasjonskategori.Applikasjonskategori;
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
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
        publishedApplicationResourceCache.put(applicationResource.getId(), publicationFingerprint(applicationResource));
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
                    int currentHash = publicationFingerprint(ar);
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

    static int publicationFingerprint(ApplicationResource applicationResource) {
        return Objects.hash(
                applicationResource.getId(),
                applicationResource.getResourceId(),
                applicationResource.getResourceName(),
                applicationResource.getResourceType(),
                applicationResource.getIdentityProviderGroupObjectId(),
                applicationResource.getIdentityProviderGroupName(),
                applicationResource.getApplicationAccessType(),
                applicationResource.getApplicationAccessRole(),
                sortedStream(applicationResource.getPlatform()).toList(),
                applicationResource.getAccessType(),
                applicationResource.getResourceLimit(),
                applicationResource.getResourceOwnerOrgUnitId(),
                applicationResource.getResourceOwnerOrgUnitName(),
                applicationResource.getLicenseEnforcement(),
                applicationResource.isHasCost(),
                applicationResource.getUnitCost(),
                applicationResource.getStatus(),
                applicationResource.getStatusChanged(),
                applicationResource.isNeedApproval(),
                sortedStream(applicationResource.getValidForRoles()).toList(),
                sortedApplicationCategories(applicationResource),
                sortedApplicationResourceLocations(applicationResource)
        );
    }

    private static Stream<String> sortedStream(Iterable<String> values) {
        if (values == null) {
            return Stream.empty();
        }

        return StreamSupport.stream(values.spliterator(), false)
                .sorted();
    }

    private static List<String> sortedApplicationCategories(ApplicationResource applicationResource) {
        if (applicationResource.getApplicationCategory() == null) {
            return List.of();
        }

        return applicationResource.getApplicationCategory().stream()
                .map(Applikasjonskategori::getName)
                .sorted()
                .toList();
    }

    private static List<List<Object>> sortedApplicationResourceLocations(ApplicationResource applicationResource) {
        if (applicationResource.getValidForOrgUnits() == null) {
            return List.of();
        }

        return applicationResource.getValidForOrgUnits().stream()
                .map(ResourceGroupProducerService::applicationResourceLocationFingerprint)
                .sorted(ResourceGroupProducerService::compareLocationFingerprint)
                .toList();
    }

    private static List<Object> applicationResourceLocationFingerprint(ApplicationResourceLocation location) {
        return List.of(
                nullable(location.getResourceId()),
                nullable(location.getResourceName()),
                nullable(location.getOrgUnitId()),
                nullable(location.getOrgUnitName()),
                nullable(location.getResourceLimit()),
                location.isTopOrgunit()
        );
    }

    private static int compareLocationFingerprint(List<Object> first, List<Object> second) {
        return first.toString().compareTo(second.toString());
    }

    private static Object nullable(Object value) {
        return value == null ? "" : value;
    }

    public List<ApplicationResource> publishAllResourceGroups(List<ApplicationResource> applicationResources) {
        List<ApplicationResource> publishedApplicationResources = applicationResources.stream()
                .peek(this::publishResourceGroup)
                .toList();

        log.debug("Published all application resources: {}", publishedApplicationResources.size());
        return publishedApplicationResources;
    }
}
