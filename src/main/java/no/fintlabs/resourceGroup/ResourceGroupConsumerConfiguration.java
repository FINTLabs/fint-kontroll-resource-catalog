package no.fintlabs.resourceGroup;


import lombok.extern.slf4j.Slf4j;
import no.fintlabs.applicationResource.ApplicationResource;
import no.fintlabs.cache.FintCache;
import no.fintlabs.kafka.entity.EntityConsumerFactoryService;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Configuration
@Slf4j
public class ResourceGroupConsumerConfiguration {
    private final EntityConsumerFactoryService entityConsumerFactoryService;

    public ResourceGroupConsumerConfiguration(EntityConsumerFactoryService entityConsumerFactoryService) {
        this.entityConsumerFactoryService = entityConsumerFactoryService;
    }

    @Bean
    ConcurrentMessageListenerContainer<String, ApplicationResource> resourceGroupConsumer(
            FintCache<Long,ApplicationResource> publishedApplicationResourceCache
    ){
        return entityConsumerFactoryService.createFactory(
                ApplicationResource.class,
                consumerRecord -> publishedApplicationResourceCache.put(
                        consumerRecord.value().getId(),
                        consumerRecord.value()
                )
        ).createContainer(EntityTopicNameParameters.builder().resource("resource-group").build());
    }


}
