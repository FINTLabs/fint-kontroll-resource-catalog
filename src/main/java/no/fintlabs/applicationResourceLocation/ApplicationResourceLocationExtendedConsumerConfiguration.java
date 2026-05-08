package no.fintlabs.applicationResourceLocation;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.cache.FintCache;
import no.novari.kafka.consuming.*;
import no.novari.kafka.topic.name.EntityTopicNameParameters;
import no.fintlabs.KafkaConsumerConfigurationDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ApplicationResourceLocationExtendedConsumerConfiguration {

    private final KafkaConsumerConfigurationDefaults kafkaConsumerConfigurationDefaults;


    @Bean
    public ConcurrentMessageListenerContainer<String, ApplicationResourceLocationExtended> applicationResourceLocationExtendedConsumer(
            FintCache<Long, ApplicationResourceLocationExtended> publishedExtendedApplicationResourceLocationCache,
            ParameterizedListenerContainerFactoryService parameterizedListenerContainerFactoryService
    ) {
        ParameterizedListenerContainerFactory<ApplicationResourceLocationExtended> recordListenerContainerFactory =
                parameterizedListenerContainerFactoryService.createRecordListenerContainerFactory(
                        ApplicationResourceLocationExtended.class,
                        consumerRecord -> {
                            Long recordId = Long.valueOf(consumerRecord.key());
                            if (consumerRecord.value() != null) {
                                publishedExtendedApplicationResourceLocationCache.put(
                                        recordId,
                                        consumerRecord.value());
                            } else publishedExtendedApplicationResourceLocationCache.remove(recordId);
                        },
                        kafkaConsumerConfigurationDefaults.seekToBeginningListenerConfiguration(),
                        kafkaConsumerConfigurationDefaults.defaultErrorHandler()
                );
        EntityTopicNameParameters entityTopicNameParameters =
                kafkaConsumerConfigurationDefaults.defaultEntityTopic("applicationresourcelocation-extended");


        return recordListenerContainerFactory.createContainer(entityTopicNameParameters);
    }
}