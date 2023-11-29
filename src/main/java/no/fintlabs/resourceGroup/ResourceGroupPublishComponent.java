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
    @Scheduled(initialDelayString = "10000",
    fixedDelayString = "20000")

    public void publishCompleteResourceGroups() {
        List<ApplicationResource> applicationResources =
                azureGroupService.getAllAzureGroups()
                        .stream()
                        .map(azureGroup -> azureGroup.getResourceGroupID())
                        .map(id->applicationResourceService.getApplicationResourceFromId(id))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .peek(applicationResource -> {log.info("Found application resource "+ applicationResource.getId());})
                        .toList();
        applicationResourceService.saveApplicationResources(applicationResources);
        resourceGroupProducerService.publishResourceGroups(applicationResources);
    }
}
