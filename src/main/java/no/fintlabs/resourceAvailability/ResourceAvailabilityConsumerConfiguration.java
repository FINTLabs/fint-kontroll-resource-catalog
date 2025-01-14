package no.fintlabs.resourceAvailability;


import lombok.extern.slf4j.Slf4j;
import no.fintlabs.kafka.entity.EntityConsumerFactoryService;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Configuration
@Slf4j
public class ResourceAvailabilityConsumerConfiguration {

    @Bean
    public ConcurrentMessageListenerContainer<String,ResourceAvailability> resourceAvailabilityConsumer(
            EntityConsumerFactoryService entityConsumerFactoryService,
            ResourceAvailabilityService resourceAvailabilityService
    ) {
        EntityTopicNameParameters entityTopicNameParameters = EntityTopicNameParameters
                .builder()
                .resource("resourceavailability")
                .build();

       return entityConsumerFactoryService.createFactory(
               ResourceAvailability.class,
               (ConsumerRecord<String,ResourceAvailability> consumerRecord) ->{
                   log.debug("Consumer record: {}", consumerRecord);
                   resourceAvailabilityService.save(consumerRecord.value());
               }).createContainer(entityTopicNameParameters);
    }

}
