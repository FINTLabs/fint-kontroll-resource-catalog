package no.fintlabs.applicationResource;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.cache.FintCache;
import no.fintlabs.kafka.entity.EntityConsumerFactoryService;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import no.fintlabs.resourceGroup.AzureGroup;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import java.util.Optional;

@Slf4j
@Configuration
public class ApplicationResourceConsumerConfiguration {

    private final EntityConsumerFactoryService entityConsumerFactoryService;

    public ApplicationResourceConsumerConfiguration(EntityConsumerFactoryService entityConsumerFactoryService) {
        this.entityConsumerFactoryService = entityConsumerFactoryService;
    }

    @Bean
    @ConditionalOnProperty(name = "fint.kontroll.resource-catalog.source", havingValue = "fint")
    public ConcurrentMessageListenerContainer<String,ApplicationResource> applicationResourceConsumer(
            ApplicationResourceService applicationResourceService,
            EntityConsumerFactoryService entityConsumerFactoryService
    ){
        EntityTopicNameParameters entityTopicNameParameters = EntityTopicNameParameters
                .builder()
                .resource("applicationresource")
                .build();

        log.info("Creating application resource consumer for {}", entityTopicNameParameters);

        return entityConsumerFactoryService.createFactory(
                ApplicationResource.class,
                (ConsumerRecord<String,ApplicationResource> consumerRecord)
                -> applicationResourceService.save(consumerRecord.value()))
                .createContainer(entityTopicNameParameters);

    }
    @Bean
    public ConcurrentMessageListenerContainer<String, AzureGroup> azureGroupConsumer(
            FintCache<Long, AzureGroup> azureGroupCache,
            ApplicationResourceService applicationResourceService
    ){
        return entityConsumerFactoryService.createFactory(
                AzureGroup.class,
                consumerRecord -> {
                    AzureGroup azureGroup = consumerRecord.value();
                    log.debug("Saving: " + azureGroup.getId() + " to cache");
                    Optional<ApplicationResource> applicationResourceOptional = applicationResourceService.getApplicationResourceFromId(azureGroup.getResourceGroupID());

                    if (applicationResourceOptional.isPresent()) {
                        ApplicationResource applicationResource = applicationResourceOptional.get();
                        applicationResource.setIdentityProviderGroupObjectId(azureGroup.getId());
                        applicationResource.setIdentityProviderGroupName(azureGroup.getDisplayName());
                        log.debug("Saving " + applicationResource.getId() + " with Azure groupObjectId " + azureGroup.getId());
                        applicationResourceService.save(applicationResource);
                        azureGroupCache.put(azureGroup.getResourceGroupID(),azureGroup);

                    }
                }
        ).createContainer(EntityTopicNameParameters.builder().resource("azuread-resource-group").build());
        }
}

