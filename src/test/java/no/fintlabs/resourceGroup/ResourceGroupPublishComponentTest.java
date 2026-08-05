package no.fintlabs.resourceGroup;

import no.fintlabs.applicationResource.ApplicationResource;
import no.fintlabs.applicationResource.ApplicationResourceService;
import no.fintlabs.applicationResourceLocation.ApplicationResourceLocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceGroupPublishComponentTest {

    @Mock
    private ApplicationResourceService applicationResourceService;

    @Mock
    private ResourceGroupProducerService resourceGroupProducerService;

    @Mock
    private ApplicationResourceLocationService applicationResourceLocationService;

    private ResourceGroupPublishComponent resourceGroupPublishComponent;

    @BeforeEach
    void setup() {
        resourceGroupPublishComponent = new ResourceGroupPublishComponent(
                applicationResourceService,
                resourceGroupProducerService,
                applicationResourceLocationService
        );
    }

    @Test
    void shouldPublishOnlyNonDeletedResourcesUsingCacheByDefault() {
        ApplicationResource active = applicationResource(1L, "ACTIVE");
        ApplicationResource deleted = applicationResource(2L, "DELETED");
        when(applicationResourceService.getAllApplicationResources()).thenReturn(List.of(active, deleted));
        when(resourceGroupProducerService.publishResourceGroups(any())).thenReturn(List.of(active));

        resourceGroupPublishComponent.publishScheduledResourceGroups();

        verify(resourceGroupProducerService).publishResourceGroups(argThat(resources ->
                resources.size() == 1 && resources.get(0).getId().equals(active.getId())
        ));
        verify(resourceGroupProducerService, never()).publishAllResourceGroups(org.mockito.ArgumentMatchers.any());
        verify(applicationResourceLocationService).extractAndSendToPublish(active, false);
        verify(applicationResourceLocationService, never()).extractAndSendToPublish(deleted, false);
    }

    @Test
    void shouldForcePublishAllNonDeletedResourcesWhenRequested() {
        ApplicationResource active = applicationResource(1L, "ACTIVE");
        ApplicationResource pending = applicationResource(2L, "PENDING_ACTIVE");
        ApplicationResource deleted = applicationResource(3L, "DELETED");
        when(applicationResourceService.getAllApplicationResources()).thenReturn(List.of(active, pending, deleted));
        when(resourceGroupProducerService.publishAllResourceGroups(any())).thenReturn(List.of(active, pending));

        resourceGroupPublishComponent.publishResourceGroups(true);

        verify(resourceGroupProducerService).publishAllResourceGroups(argThat(resources ->
                resources.size() == 2
                        && resources.stream().map(ApplicationResource::getId).toList().containsAll(List.of(1L, 2L))
        ));
        verify(resourceGroupProducerService, never()).publishResourceGroups(org.mockito.ArgumentMatchers.any());
        verify(applicationResourceLocationService).extractAndSendToPublish(active, true);
        verify(applicationResourceLocationService).extractAndSendToPublish(pending, true);
        verify(applicationResourceLocationService, never()).extractAndSendToPublish(deleted, true);
    }

    private ApplicationResource applicationResource(Long id, String status) {
        ApplicationResource applicationResource = new ApplicationResource();
        applicationResource.setId(id);
        applicationResource.setStatus(status);
        return applicationResource;
    }
}
