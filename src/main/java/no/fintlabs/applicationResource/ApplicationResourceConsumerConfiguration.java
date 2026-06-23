package no.fintlabs.applicationResource;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.KafkaConsumerConfigurationDefaults;
import no.fintlabs.cache.FintCache;
import no.fintlabs.kodeverk.brukertype.BrukertypeService;
import no.fintlabs.resourceGroup.AzureGroup;
import no.novari.kafka.consuming.*;
import no.novari.kafka.topic.name.EntityTopicNameParameters;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import java.util.Optional;

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
    public ConcurrentMessageListenerContainer<String, AzureGroup> azureGroupConsumer(
            FintCache<Long, AzureGroup> azureGroupCache,
            ApplicationResourceService applicationResourceService,
            ParameterizedListenerContainerFactoryService parameterizedListenerContainerFactoryService
    ){
        ParameterizedListenerContainerFactory<AzureGroup> recordListenerContainerFactory =
                parameterizedListenerContainerFactoryService.createRecordListenerContainerFactory(
                        AzureGroup.class,
                        consumerRecord -> {
                            AzureGroup azureGroup = consumerRecord.value();
                            log.debug("Saving: " + azureGroup.getId() + " to cache");
                            Optional<ApplicationResource> applicationResourceOptional =
                                    applicationResourceService.findApplicationResourceById(azureGroup.getResourceGroupID());

                            if (applicationResourceOptional.isPresent()) {
                                ApplicationResource applicationResource = applicationResourceOptional.get();
                                applicationResource.setIdentityProviderGroupObjectId(azureGroup.getId());
                                applicationResource.setIdentityProviderGroupName(azureGroup.getDisplayName());
                                log.debug("Saving " + applicationResource.getId() + " with Azure groupObjectId " + azureGroup.getId());
                                applicationResourceService.save(applicationResource);
                                azureGroupCache.put(azureGroup.getResourceGroupID(),azureGroup);
                            }
                        },
                        kafkaConsumerConfigurationDefaults.defaultListenerConfiguration(),
                        kafkaConsumerConfigurationDefaults.defaultErrorHandler()
                );
        EntityTopicNameParameters entityTopicNameParameters =
                kafkaConsumerConfigurationDefaults.defaultEntityTopic("azuread-resource-group");

        return recordListenerContainerFactory.createContainer(entityTopicNameParameters);
    }
}

