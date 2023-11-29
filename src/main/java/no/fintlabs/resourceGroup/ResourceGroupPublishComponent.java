package no.fintlabs.resourceGroup;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.applicationResource.ApplicationResource;
import no.fintlabs.applicationResource.ApplicationResourceService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class ResourceGroupPublishComponent {
    private final AzureGroupService azureGroupService;
    private final ApplicationResourceService applicationResourceService;
    private final ResourceGroupProducerService resourceGroupProducerService;

    public ResourceGroupPublishComponent(AzureGroupService azureGroupService, ApplicationResourceService applicationResourceService, ResourceGroupProducerService resourceGroupProducerService) {
        this.azureGroupService = azureGroupService;
        this.applicationResourceService = applicationResourceService;
        this.resourceGroupProducerService = resourceGroupProducerService;
    }
    @Scheduled(initialDelayString = "30000",
    fixedDelayString = "900000")

    public void publishCompleteAndInCompleteResourceGroups() {
        List<ApplicationResource> applicationResourcesWithAzureGroupId =
                azureGroupService.getAllAzureGroups()
                        .stream()
                        .map(azureGroup -> azureGroup.getResourceGroupID())
                        .map(id->applicationResourceService.getApplicationResourceFromId(id))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .peek(applicationResource -> {
                            log.info("Found application resource "+ applicationResource.getId()
                                    + " with Azure groupObjectId" + applicationResource.getIdentityProviderGroupObjectId()
                                    + ". Complete resource is published") ;
                        })
                        .toList();
        applicationResourceService.saveApplicationResources(applicationResourcesWithAzureGroupId);
        resourceGroupProducerService.publishResourceGroups(applicationResourcesWithAzureGroupId);

        List<ApplicationResource> applicationResources = applicationResourceService.getAllApplicationResources();

        if (!applicationResources.isEmpty()) {
            List<ApplicationResource> applicationResourcesWithOutAzureGroupId =
                    applicationResources
                            .stream()
                            .filter(applicationResource -> (applicationResource.getIdentityProviderGroupObjectId() == null))
                            .peek(applicationResource -> {
                                log.info("Application resource "+ applicationResource.getId()
                                        + " is missing Azure groupObjectId. Resource is republished");
                            })
                            .toList();

            resourceGroupProducerService.publishResourceGroups(applicationResourcesWithOutAzureGroupId);
        }
    }
}
