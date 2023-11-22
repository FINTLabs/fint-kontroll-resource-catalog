package no.fintlabs.resourceGroup;

import no.fintlabs.applicationResource.ApplicationResource;
import no.fintlabs.applicationResource.ApplicationResourceService;
import no.fintlabs.cache.FintCache;
import no.fintlabs.kafka.entity.EntityConsumerFactoryService;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

public class AzureADResourceGroupConfiguration {
    @Bean
    public ConcurrentMessageListenerContainer<String, AzureGroup> applicationResourceConsumer(
            FintCache <String, AzureGroup> azureGroupCache,
            EntityConsumerFactoryService entityConsumerFactoryService
    ){
        EntityTopicNameParameters entityTopicNameParameters = EntityTopicNameParameters
                .builder()
                .resource("azuread-resource-group")
                .build();

        return entityConsumerFactoryService.createFactory(
                        AzureGroup.class,
                        (ConsumerRecord<String,AzureGroup> consumerRecord)
                                -> azureGroupCache.put(consumerRecord.value().getStringresourceGroupID(),
                                consumerRecord.value()))
                .createContainer(entityTopicNameParameters);

    }
}
