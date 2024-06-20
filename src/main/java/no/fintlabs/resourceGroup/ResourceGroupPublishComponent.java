package no.fintlabs.resourceGroup;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.applicationResource.ApplicationResource;
import no.fintlabs.applicationResource.ApplicationResourceService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class ResourceGroupPublishComponent {
    private final ApplicationResourceService applicationResourceService;
    private final ResourceGroupProducerService resourceGroupProducerService;

    public ResourceGroupPublishComponent(ApplicationResourceService applicationResourceService, ResourceGroupProducerService resourceGroupProducerService) {
        this.applicationResourceService = applicationResourceService;
        this.resourceGroupProducerService = resourceGroupProducerService;
    }
    @Scheduled(initialDelayString = "30000",
            fixedDelayString = "900000")

    public void publishCompleteAndInCompleteResourceGroups() {

        List<ApplicationResource> allApplicationResourcesInDB = applicationResourceService.getAllApplicationResources();
        if (!allApplicationResourcesInDB.isEmpty()) {
            List<ApplicationResource> applicationResourcesReadyToBePublished =
                    allApplicationResourcesInDB
                            .stream()
                            .peek(applicationResource -> {
                                log.debug("Application resource {} from database added to list for publishing as resource-group", applicationResource.getId());
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
