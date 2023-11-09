package no.fintlabs.applicationResource;

import no.fintlabs.kafka.entity.EntityConsumerFactoryService;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Configuration
public class ApplicationResourceConsumerConfiguration {

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

}
