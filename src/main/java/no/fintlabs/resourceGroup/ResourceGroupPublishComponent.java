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

    public ResourceGroupPublishComponent(
            ApplicationResourceService applicationResourceService,
            ResourceGroupProducerService resourceGroupProducerService,
            ApplicationResourceLocationService applicationResourceLocationService
    ) {
        this.applicationResourceService = applicationResourceService;
        this.resourceGroupProducerService = resourceGroupProducerService;
        this.applicationResourceLocationService = applicationResourceLocationService;
    }

    @Scheduled(
            cron = "${fint.kontroll.resource-catalog.publishing.cron}"
    )
    public void publishScheduledResourceGroups() {
        publishResourceGroups(false);
    }

    public void publishResourceGroups(boolean publishAll) {
        List<ApplicationResource> allApplicationResourcesInDB = applicationResourceService.getAllApplicationResources();
        if (!allApplicationResourcesInDB.isEmpty()) {
            List<ApplicationResource> applicationResourcesReadyToBePublished =
                    allApplicationResourcesInDB
                            .stream()
                            .filter(applicationResource -> !"DELETED".equalsIgnoreCase(applicationResource.getStatus()))
                            .toList();

            log.info("{} application resources added to list for publishing as resource-group", applicationResourcesReadyToBePublished.size());
            List<ApplicationResource> publishedResourceGroups = publishAll
                    ? resourceGroupProducerService.publishAllResourceGroups(applicationResourcesReadyToBePublished)
                    : resourceGroupProducerService.publishResourceGroups(applicationResourcesReadyToBePublished);
            applicationResourcesReadyToBePublished.forEach(applicationResource ->
                    applicationResourceLocationService.extractAndSendToPublish(applicationResource, publishAll)
            );
            log.info("Published {} resource groups of total {} applicationResource objects found in database. publishAll={}",
                    publishedResourceGroups.size(),
                    applicationResourcesReadyToBePublished.size(),
                    publishAll);
        }
    }
}
