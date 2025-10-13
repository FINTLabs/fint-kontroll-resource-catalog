package no.fintlabs.resourceGroup;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.applicationResource.ApplicationResource;
import no.fintlabs.applicationResource.ApplicationResourceService;
import no.fintlabs.applicationResourceLocation.ApplicationResourceLocationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class ResourceGroupPublishComponent {
    private final ApplicationResourceService applicationResourceService;
    private final ResourceGroupProducerService resourceGroupProducerService;
    private final ApplicationResourceLocationService applicationResourceLocationService;

    public ResourceGroupPublishComponent(ApplicationResourceService applicationResourceService, ResourceGroupProducerService resourceGroupProducerService, ApplicationResourceLocationService applicationResourceLocationService) {
        this.applicationResourceService = applicationResourceService;
        this.resourceGroupProducerService = resourceGroupProducerService;
        this.applicationResourceLocationService = applicationResourceLocationService;
    }
    @Scheduled(initialDelayString = "100",
            fixedDelayString = "10000")

    public void publishCompleteAndInCompleteResourceGroups() {

        List<ApplicationResource> allApplicationResourcesInDB = applicationResourceService.getAllApplicationResources();
        if (!allApplicationResourcesInDB.isEmpty()) {
            List<ApplicationResource> applicationResourcesReadyToBePublished =
                    allApplicationResourcesInDB
                            .stream()
                            .peek(applicationResource -> {
                                log.debug("Application resource {} from database added to list for publishing as resource-group", applicationResource.getId());
                                applicationResourceLocationService.extractAndSendToPublish(applicationResource);
                            })
                            .toList();

            log.info("{} application resources added to list for publishing as resource-group", applicationResourcesReadyToBePublished.size());
            List<ApplicationResource> publishedResourceGroups = resourceGroupProducerService
                    .publishResourceGroups(applicationResourcesReadyToBePublished);
            log.info("Published {} resource groups of total {} applicationResource objects found in database",
                    publishedResourceGroups.size(),
                    applicationResourcesReadyToBePublished.size());


        }
    }
}
