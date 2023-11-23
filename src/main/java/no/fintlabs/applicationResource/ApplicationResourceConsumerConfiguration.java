package no.fintlabs.applicationResource;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.cache.FintCache;
import no.fintlabs.kafka.common.ListenerContainerFactory;
import no.fintlabs.kafka.entity.EntityConsumerFactoryService;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import no.fintlabs.kafka.entity.topic.EntityTopicNamePatternParameters;
import no.fintlabs.resourceGroup.AzureGroup;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Slf4j
@Configuration
public class ApplicationResourceConsumerConfiguration {

    private final EntityConsumerFactoryService entityConsumerFactoryService;

    public ApplicationResourceConsumerConfiguration(EntityConsumerFactoryService entityConsumerFactoryService) {
        this.entityConsumerFactoryService = entityConsumerFactoryService;
    }

    @Bean
    public ConcurrentMessageListenerContainer<String,ApplicationResource> applicationResourceConsumer(
            ApplicationResourceService applicationResourceService,
            EntityConsumerFactoryService entityConsumerFactoryService
    ){
        EntityTopicNameParameters entityTopicNameParameters = EntityTopicNameParameters
                .builder()
                .resource("applicationresource")
                .build();

        return entityConsumerFactoryService.createFactory(
                ApplicationResource.class,
                (ConsumerRecord<String,ApplicationResource> consumerRecord)
                -> applicationResourceService.save(consumerRecord.value()))
                .createContainer(entityTopicNameParameters);

    }
    @Bean
    public ConcurrentMessageListenerContainer<String, AzureGroup> azureGroupConsumer(
            FintCache<Long, AzureGroup> azureGroupCache

    ){
        return entityConsumerFactoryService.createFactory(
                AzureGroup.class,
                consumerRecord -> {
                    AzureGroup azureGroup = consumerRecord.value();
                    log.debug("Saving: " + azureGroup.getId() + " to cache");

                    azureGroupCache.put(azureGroup.getResourceGroupID(),azureGroup);

                }
        ).createContainer(EntityTopicNameParameters.builder().resource("azuread-resource-group").build());
        }
}

