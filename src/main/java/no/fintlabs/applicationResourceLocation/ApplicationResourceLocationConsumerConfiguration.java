package no.fintlabs.applicationResourceLocation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.novari.kafka.consuming.*;
import no.novari.kafka.topic.name.EntityTopicNameParameters;
import no.fintlabs.KafkaConsumerConfigurationDefaults;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ApplicationResourceLocationConsumerConfiguration {

    private final KafkaConsumerConfigurationDefaults kafkaConsumerConfigurationDefaults;

    @Bean
    @ConditionalOnProperty(name = "fint.kontroll.resource-catalog.source", havingValue = "fint")
    public ConcurrentMessageListenerContainer<String, ApplicationResourceLocation> applicationResourceLocationConsumer(
            ApplicationResourceLocationService applicationResourceLocationService,
            ParameterizedListenerContainerFactoryService parameterizedListenerContainerFactoryService
    ) {
        ParameterizedListenerContainerFactory<ApplicationResourceLocation> recordListenerContainerFactory =
                parameterizedListenerContainerFactoryService.createRecordListenerContainerFactory(
                        ApplicationResourceLocation.class,
                        (ConsumerRecord<String, ApplicationResourceLocation> consumerRecord)
                                -> applicationResourceLocationService.save(consumerRecord.value()),
                        kafkaConsumerConfigurationDefaults.seekToBeginningListenerConfiguration(),
                        kafkaConsumerConfigurationDefaults.defaultErrorHandler()
                );
        EntityTopicNameParameters entityTopicNameParameters =
                kafkaConsumerConfigurationDefaults.defaultEntityTopic("applicationresource-location");


        return recordListenerContainerFactory.createContainer(entityTopicNameParameters);
    }
}

