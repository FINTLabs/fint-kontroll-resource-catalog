package no.fintlabs.resourceGroup;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.applicationResource.ApplicationResource;
import no.fintlabs.cache.FintCache;
import no.novari.kafka.consuming.*;
import no.novari.kafka.topic.name.EntityTopicNameParameters;
import no.fintlabs.KafkaConsumerConfigurationDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class ResourceGroupConsumerConfiguration {

    private final KafkaConsumerConfigurationDefaults kafkaConsumerConfigurationDefaults;

    @Bean
    public ConcurrentMessageListenerContainer<String, ApplicationResource> resourceGroupConsumer(
            FintCache<Long, Integer> publishedApplicationResourceCache,
            ParameterizedListenerContainerFactoryService parameterizedListenerContainerFactoryService
    ) {
        ParameterizedListenerContainerFactory<ApplicationResource> recordListenerContainerFactory =
                parameterizedListenerContainerFactoryService.createRecordListenerContainerFactory(
                        ApplicationResource.class,
                        consumerRecord -> {
                            ApplicationResource ar = consumerRecord.value();
                            if (ar != null && ar.getId() != null) {
                                publishedApplicationResourceCache.put(ar.getId(), ar.hashCode());
                            }
                        },
                        seekToBeginningListenerConfiguration(),
                        kafkaConsumerConfigurationDefaults.defaultErrorHandler()
                );
        EntityTopicNameParameters entityTopicNameParameters =
                kafkaConsumerConfigurationDefaults.defaultEntityTopic("resource-group");


        return recordListenerContainerFactory.createContainer(entityTopicNameParameters);
    }

    private ListenerConfiguration seekToBeginningListenerConfiguration() {

        return ListenerConfiguration.stepBuilder()
                .groupIdApplicationDefault()
                .maxPollRecordsKafkaDefault()
                .maxPollIntervalKafkaDefault()
                .seekToBeginningOnAssignment()
                .build();
    }
}

