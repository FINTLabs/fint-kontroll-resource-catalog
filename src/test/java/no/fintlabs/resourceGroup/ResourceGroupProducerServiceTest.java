package no.fintlabs.resourceGroup;

import no.fintlabs.applicationResource.ApplicationResource;
import no.fintlabs.cache.FintCache;
import no.novari.kafka.producing.ParameterizedProducerRecord;
import no.novari.kafka.producing.ParameterizedTemplate;
import no.novari.kafka.producing.ParameterizedTemplateFactory;
import no.novari.kafka.topic.EntityTopicService;
import no.novari.kafka.topic.EventTopicService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceGroupProducerServiceTest {

    @Mock
    private EntityTopicService entityTopicService;

    @Mock
    private EventTopicService eventTopicService;

    @Mock
    private FintCache<Long, Integer> publishedApplicationResourceCache;

    @Mock
    private ParameterizedTemplateFactory parameterizedTemplateFactory;

    @Mock
    private ParameterizedTemplate<ApplicationResource> resourceGroupTemplate;

    @Mock
    private ParameterizedTemplate<ResourceGroup> resourceGroupMsGraphTemplate;

    private ResourceGroupProducerService resourceGroupProducerService;

    @BeforeEach
    void setup() {
        when(parameterizedTemplateFactory.createTemplate(ApplicationResource.class))
                .thenReturn(resourceGroupTemplate);
        when(parameterizedTemplateFactory.createTemplate(ResourceGroup.class))
                .thenReturn(resourceGroupMsGraphTemplate);

        resourceGroupProducerService = new ResourceGroupProducerService(
                entityTopicService,
                eventTopicService,
                publishedApplicationResourceCache,
                parameterizedTemplateFactory
        );
    }

    @Test
    void publishShouldSendApplicationResourceToOldTopicAndCommandToMsGraphTopic() {
        UUID idpGroupObjectId = UUID.randomUUID();
        ApplicationResource applicationResource = new ApplicationResource();
        applicationResource.setId(61L);
        applicationResource.setResourceName("Resource 61");
        applicationResource.setIdentityProviderGroupObjectId(idpGroupObjectId);
        applicationResource.setStatus("ACTIVE");

        resourceGroupProducerService.publish(applicationResource);

        ArgumentCaptor<ParameterizedProducerRecord<ApplicationResource>> oldTopicCaptor =
                ArgumentCaptor.forClass(ParameterizedProducerRecord.class);
        verify(resourceGroupTemplate).send(oldTopicCaptor.capture());
        assertEquals("61", oldTopicCaptor.getValue().getKey());
        assertSame(applicationResource, oldTopicCaptor.getValue().getValue());

        ArgumentCaptor<ParameterizedProducerRecord<ResourceGroup>> msGraphTopicCaptor =
                ArgumentCaptor.forClass(ParameterizedProducerRecord.class);
        verify(resourceGroupMsGraphTemplate).send(msGraphTopicCaptor.capture());
        ResourceGroup resourceGroup = msGraphTopicCaptor.getValue().getValue();
        assertNotEquals("61", msGraphTopicCaptor.getValue().getKey());
        assertDoesNotThrow(() -> UUID.fromString(msGraphTopicCaptor.getValue().getKey()));
        assertEquals(ResourceGroupOperation.UPDATE, resourceGroup.getOperation());
        assertEquals("61", resourceGroup.getResourceId());
        assertEquals(idpGroupObjectId.toString(), resourceGroup.getIdpGroupObjectId());
        assertEquals("Resource 61", resourceGroup.getResourceName());
        verify(publishedApplicationResourceCache).put(61L, applicationResource.hashCode());
    }

    @Test
    void publishResourceGroupMsGraphShouldOnlySendCommandToMsGraphTopic() {
        ApplicationResource applicationResource = new ApplicationResource();
        applicationResource.setId(62L);
        applicationResource.setResourceName("Resource 62");
        applicationResource.setStatus("PENDING_ACTIVE");

        resourceGroupProducerService.publishResourceGroupMsGraph(applicationResource);

        verify(resourceGroupTemplate, never()).send(any());
        verify(resourceGroupMsGraphTemplate).send(any());
    }

    @Test
    void publishResourceGroupMsGraphShouldSkipDeletedResource() {
        ApplicationResource applicationResource = new ApplicationResource();
        applicationResource.setId(63L);
        applicationResource.setResourceName("Resource 63");
        applicationResource.setStatus("DELETED");
        applicationResource.setIdentityProviderGroupObjectId(UUID.randomUUID());

        resourceGroupProducerService.publishResourceGroupMsGraph(applicationResource);

        verify(resourceGroupTemplate, never()).send(any());
        verify(resourceGroupMsGraphTemplate, never()).send(any());
    }

    @Test
    void publishShouldStillSendOldTopicAndSkipMsGraphForDeletedResourceWithoutIdpGroupObjectId() {
        ApplicationResource applicationResource = new ApplicationResource();
        applicationResource.setId(64L);
        applicationResource.setResourceName("Resource 64");
        applicationResource.setStatus("DELETED");

        resourceGroupProducerService.publish(applicationResource);

        verify(resourceGroupTemplate).send(any());
        verify(resourceGroupMsGraphTemplate, never()).send(any());
        verify(publishedApplicationResourceCache).put(64L, applicationResource.hashCode());
    }

    @Test
    void publishResourceGroupsShouldOnlyPublishChangedResourcesToEntityTopic() {
        ApplicationResource unchanged = new ApplicationResource();
        unchanged.setId(1L);
        unchanged.setStatus("ACTIVE");
        ApplicationResource changed = new ApplicationResource();
        changed.setId(2L);
        changed.setStatus("ACTIVE");

        when(publishedApplicationResourceCache.getNumberOfEntries()).thenReturn(2L);
        when(publishedApplicationResourceCache.getOptional(1L)).thenReturn(Optional.of(unchanged.hashCode()));
        when(publishedApplicationResourceCache.getOptional(2L)).thenReturn(Optional.empty());

        List<ApplicationResource> published =
                resourceGroupProducerService.publishResourceGroups(List.of(unchanged, changed));

        assertEquals(List.of(changed), published);
        verify(resourceGroupTemplate).send(any());
        verify(resourceGroupMsGraphTemplate, never()).send(any());
        verify(publishedApplicationResourceCache).put(2L, changed.hashCode());
    }

    @Test
    void publishAllResourceGroupsShouldOnlyPublishEntityTopic() {
        ApplicationResource active = new ApplicationResource();
        active.setId(1L);
        active.setStatus("ACTIVE");

        List<ApplicationResource> published =
                resourceGroupProducerService.publishAllResourceGroups(List.of(active));

        assertEquals(List.of(active), published);
        verify(resourceGroupTemplate).send(any());
        verify(resourceGroupMsGraphTemplate, never()).send(any());
        verify(publishedApplicationResourceCache).put(1L, active.hashCode());
    }

    @Test
    void publishResourceGroupsMsGraphShouldSkipDeletedResources() {
        UUID idpGroupObjectId = UUID.randomUUID();
        ApplicationResource skipped = new ApplicationResource();
        skipped.setId(1L);
        skipped.setStatus("DELETED");

        ApplicationResource skippedWithIdpGroupObjectId = new ApplicationResource();
        skippedWithIdpGroupObjectId.setId(2L);
        skippedWithIdpGroupObjectId.setStatus("DELETED");
        skippedWithIdpGroupObjectId.setIdentityProviderGroupObjectId(idpGroupObjectId);

        List<ApplicationResource> publishedResources =
                resourceGroupProducerService.publishResourceGroupsMsGraph(List.of(skipped, skippedWithIdpGroupObjectId));

        assertEquals(List.of(), publishedResources);
        verify(resourceGroupMsGraphTemplate, never()).send(any());
    }
}
