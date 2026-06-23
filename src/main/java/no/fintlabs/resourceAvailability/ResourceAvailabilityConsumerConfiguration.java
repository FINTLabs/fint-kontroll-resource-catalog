package no.fintlabs.resourceAvailability;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.novari.kafka.consuming.*;
import no.novari.kafka.topic.name.EntityTopicNameParameters;
import no.fintlabs.KafkaConsumerConfigurationDefaults;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class ResourceAvailabilityConsumerConfiguration {

    private final KafkaConsumerConfigurationDefaults kafkaConsumerConfigurationDefaults;
    @Bean
    public ConcurrentMessageListenerContainer<String, ResourceAvailabilityDTO> resourceAvailabilityConsumer(
           ParameterizedListenerContainerFactoryService parameterizedListenerContainerFactoryService,
            ResourceAvailabilityService resourceAvailabilityService
    ) {
        ParameterizedListenerContainerFactory<ResourceAvailabilityDTO> recordListenerContainerFactory =
                parameterizedListenerContainerFactoryService.createRecordListenerContainerFactory(
                        ResourceAvailabilityDTO.class,
                        (ConsumerRecord<String, ResourceAvailabilityDTO> consumerRecord) -> {
                            ResourceAvailability resourceAvailability = ResourceAvailabilityMapper.toResourceAvailability(consumerRecord.value());
                            resourceAvailabilityService.save(resourceAvailability);
                        },
                        kafkaConsumerConfigurationDefaults.defaultListenerConfiguration(),
                        kafkaConsumerConfigurationDefaults.defaultErrorHandler()
                );
        EntityTopicNameParameters entityTopicNameParameters = kafkaConsumerConfigurationDefaults.defaultEntityTopic("resourceavailability");

        return recordListenerContainerFactory.createContainer(entityTopicNameParameters);
    }

}
