package no.fintlabs.applicationResource;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.KafkaConsumerConfigurationDefaults;
import no.fintlabs.kodeverk.brukertype.BrukertypeService;
import no.fintlabs.resourceGroup.EntraGroup;
import no.novari.kafka.consuming.*;
import no.novari.kafka.topic.name.EntityTopicNameParameters;
import no.novari.kafka.topic.name.EventTopicNameParameters;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Slf4j
@Configuration
public class ApplicationResourceConsumerConfiguration {
    private final KafkaConsumerConfigurationDefaults kafkaConsumerConfigurationDefaults;

    public ApplicationResourceConsumerConfiguration(KafkaConsumerConfigurationDefaults kafkaConsumerConfigurationDefaults) {
        this.kafkaConsumerConfigurationDefaults = kafkaConsumerConfigurationDefaults;
    }


    @Bean
    @ConditionalOnProperty(name = "fint.kontroll.resource-catalog.source", havingValue = "fint")
    public ConcurrentMessageListenerContainer<String,ApplicationResource> applicationResourceConsumer(
            ApplicationResourceService applicationResourceService,
            ParameterizedListenerContainerFactoryService parameterizedListenerContainerFactoryService
    ){
        ParameterizedListenerContainerFactory<ApplicationResource> recordListenerContainerFactory =
                parameterizedListenerContainerFactoryService.createRecordListenerContainerFactory(
                ApplicationResource.class,
                (ConsumerRecord<String, ApplicationResource> consumerRecord)
                        -> applicationResourceService.save(consumerRecord.value()),
                        kafkaConsumerConfigurationDefaults.defaultListenerConfiguration(),
                        kafkaConsumerConfigurationDefaults.defaultErrorHandler()
        );
        EntityTopicNameParameters entityTopicNameParameters =
                kafkaConsumerConfigurationDefaults.defaultEntityTopic("applicationresource");
        log.info("Source is FINT. Creating application resource consumer for {}", entityTopicNameParameters);

        return recordListenerContainerFactory.createContainer(entityTopicNameParameters);

    }


    @Bean
    @ConditionalOnProperty(name = "fint.kontroll.resource-catalog.source", havingValue = "fint")
    public ConcurrentMessageListenerContainer<String, ApplicationResourceUserType> userTypeConsumer(
            BrukertypeService brukertypeService,
            ParameterizedListenerContainerFactoryService parameterizedListenerContainerFactoryService
    ) {
        ParameterizedListenerContainerFactory<ApplicationResourceUserType> recordListenerContainerFactory =
                parameterizedListenerContainerFactoryService.createRecordListenerContainerFactory(
                        ApplicationResourceUserType.class,
                        (ConsumerRecord<String, ApplicationResourceUserType> consumerRecord)
                                -> brukertypeService.save(consumerRecord.value()),
                        kafkaConsumerConfigurationDefaults.defaultListenerConfiguration(),
                        kafkaConsumerConfigurationDefaults.defaultErrorHandler()
                );

        EntityTopicNameParameters entityTopicNameParameters =
                kafkaConsumerConfigurationDefaults.defaultEntityTopic("applicationresource-usertype");

        return recordListenerContainerFactory.createContainer(entityTopicNameParameters);
    }


    @Bean
    public ConcurrentMessageListenerContainer<String, EntraGroup> entraGroupConsumer(
            ApplicationResourceService applicationResourceService,
            ParameterizedListenerContainerFactoryService parameterizedListenerContainerFactoryService
    ){
        ParameterizedListenerContainerFactory<EntraGroup> recordListenerContainerFactory =
                parameterizedListenerContainerFactoryService.createRecordListenerContainerFactory(
                        EntraGroup.class,
                        consumerRecord -> applicationResourceService.saveEntraGroup(consumerRecord.value()),
                        kafkaConsumerConfigurationDefaults.defaultListenerConfiguration(),
                        kafkaConsumerConfigurationDefaults.defaultErrorHandler()
                );
        EventTopicNameParameters eventTopicNameParameters =
                kafkaConsumerConfigurationDefaults.defaultEventTopic("graph-group");

        return recordListenerContainerFactory.createContainer(eventTopicNameParameters);
    }
}
