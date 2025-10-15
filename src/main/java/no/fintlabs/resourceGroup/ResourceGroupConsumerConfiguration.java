package no.fintlabs.resourceGroup;


import lombok.extern.slf4j.Slf4j;
import no.fintlabs.applicationResource.ApplicationResource;
import no.fintlabs.cache.FintCache;
import no.fintlabs.kafka.entity.EntityConsumerFactoryService;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import no.fintlabs.kodeverk.brukertype.Brukertype;
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
            FintCache<Long, Integer> publishedApplicationResourceCache
    ){
        return entityConsumerFactoryService.createFactory(
                ApplicationResource.class,
                consumerRecord -> {
                    ApplicationResource ar = consumerRecord.value();
                    if (ar != null && ar.getId() != null) {
                        publishedApplicationResourceCache.put(ar.getId(), ar.hashCode());
                    }
                }
        ).createContainer(EntityTopicNameParameters.builder().resource("resource-group").build());
    }
}
