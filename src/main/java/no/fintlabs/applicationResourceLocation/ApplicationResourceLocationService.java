package no.fintlabs.applicationResourceLocation;


import lombok.extern.slf4j.Slf4j;
import no.fintlabs.applicationResource.ApplicationResource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ApplicationResourceLocationService {
    private final ApplicationResourceLocationProduserService applicationResourceLocationProduserService;

    public ApplicationResourceLocationService(ApplicationResourceLocationProduserService applicationResourceLocationProduserService) {
        this.applicationResourceLocationProduserService = applicationResourceLocationProduserService;
    }

    public void extractAndSendToPublish(ApplicationResource applicationResource) {

        List<ApplicationResourceLocation> applicationResourceLocationsToPublishing =
                new ArrayList<>(applicationResource.getValidForOrgUnits());

        List<ApplicationResourceLocationExtended> publishedApplicationResourceLocationsExtended =
                applicationResourceLocationProduserService.publish(applicationResource.getId(),applicationResourceLocationsToPublishing);

        log.info("Published extended applicationResourceLocations: {}", publishedApplicationResourceLocationsExtended.size());

    }
}
