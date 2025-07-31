package no.fintlabs.applicationResourceLocation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.kafka.entity.EntityConsumerFactoryService;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ApplicationResourceLocationConsumerConfiguration {

    private final EntityConsumerFactoryService entityConsumerFactoryService;

    @Bean
    @ConditionalOnProperty(name = "fint.kontroll.resource-catalog.source", havingValue = "fint")
    public ConcurrentMessageListenerContainer<String, ApplicationResourceLocation> applicationResourceLocationConsumer(
            ApplicationResourceLocationService applicationResourceLocationService,
            EntityConsumerFactoryService entityConsumerFactoryService
    ) {
        EntityTopicNameParameters entityTopicNameParameters = EntityTopicNameParameters
                .builder()
                .resource("applicationresource-location")
                .build();

        log.info("Source is FINT. Creating application resource location consumer for {}", entityTopicNameParameters);

        return entityConsumerFactoryService.createFactory(
                        ApplicationResourceLocation.class,
                        (ConsumerRecord<String, ApplicationResourceLocation> consumerRecord)
                                -> applicationResourceLocationService.save(consumerRecord.value()))
                .createContainer(entityTopicNameParameters);
    }

}

