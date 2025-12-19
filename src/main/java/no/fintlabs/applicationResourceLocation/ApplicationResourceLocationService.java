package no.fintlabs.applicationResourceLocation;


import lombok.extern.slf4j.Slf4j;
import no.fintlabs.applicationResource.ApplicationResource;
import no.fintlabs.applicationResource.ApplicationResourceService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ApplicationResourceLocationService {
    private final ApplicationResourceService applicationResourceService;
    private final ApplicationResourceLocationExtendedProduserService applicationResourceLocationExtendedProduserService;
    private final ApplicationResourceLocationRepository applicationResourceLocationRepository;

    public ApplicationResourceLocationService(
            ApplicationResourceService applicationResourceService,
            ApplicationResourceLocationExtendedProduserService applicationResourceLocationExtendedProduserService,
            ApplicationResourceLocationRepository applicationResourceLocationRepository
    ) {
        this.applicationResourceService = applicationResourceService;
        this.applicationResourceLocationExtendedProduserService = applicationResourceLocationExtendedProduserService;
        this.applicationResourceLocationRepository = applicationResourceLocationRepository;
    }

    public void save(ApplicationResourceLocation appResLocationFromKafka) {
        String orgUnitId = appResLocationFromKafka.getOrgUnitId();
        String resourceId = appResLocationFromKafka.getResourceId();

        log.info("Trying to save application resource location - resource: {} {} orgunit: {} {}",
                resourceId, appResLocationFromKafka.getResourceName(), orgUnitId, appResLocationFromKafka.getOrgUnitName());

        Optional<ApplicationResource> optionalResource = applicationResourceService.getApplicationResourceByResourceId(resourceId);

        if (optionalResource.isEmpty()) {
            log.warn("Application resource with referenced resourceId {} does not exist, skipping save", resourceId);
            return;
        }

        ApplicationResource resource = optionalResource.get();
        appResLocationFromKafka.setApplicationResource(resource);

        Optional<ApplicationResourceLocation> AppResLocationOptionalFromDB =
                applicationResourceLocationRepository.findByApplicationResourceAndOrgUnitId(resource, orgUnitId);

        final boolean updated;

        if (AppResLocationOptionalFromDB.isPresent()) {
            ApplicationResourceLocation existingAppResLocation = AppResLocationOptionalFromDB.get();
            existingAppResLocation.setResourceLimit(appResLocationFromKafka.getResourceLimit());
            existingAppResLocation.setResourceName(appResLocationFromKafka.getResourceName());
            existingAppResLocation.setOrgUnitName(appResLocationFromKafka.getOrgUnitName());
            existingAppResLocation.setIsTopOrgunit(appResLocationFromKafka.getIsTopOrgunit());
            appResLocationFromKafka = existingAppResLocation;
            updated = true;
        } else {
            updated = false;
        }

        ApplicationResourceLocation saved = applicationResourceLocationRepository.save(appResLocationFromKafka);

        log.info("{} application resource location - resource: {} ({}) {} orgunit: {} {}",
                updated ? "Updated existing" : "Saved new",
                saved.getApplicationResource().getId(),
                saved.getResourceId(),
                saved.getResourceName(),
                saved.getOrgUnitId(),
                saved.getOrgUnitName()
        );
    }


    public void extractAndSendToPublish(ApplicationResource applicationResource) {

        List<ApplicationResourceLocation> applicationResourceLocationsToPublishing =
                new ArrayList<>(applicationResource.getValidForOrgUnits());

        List<ApplicationResourceLocationExtended> publishedApplicationResourceLocationsExtended =
                applicationResourceLocationExtendedProduserService.publish(applicationResource.getId(),applicationResourceLocationsToPublishing);

        log.info("Published extended applicationResourceLocations: {}", publishedApplicationResourceLocationsExtended.size());

    }
    public List<ApplicationResourceLocation> saveAll(List<ApplicationResourceLocation> applicationResourceLocations) {
        return applicationResourceLocationRepository.saveAll(applicationResourceLocations);
    }

}
