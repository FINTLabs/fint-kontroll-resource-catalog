package no.fintlabs.applicationResourceLocation;


import lombok.extern.slf4j.Slf4j;
import no.fintlabs.applicationResource.ApplicationResource;
import no.fintlabs.applicationResource.ApplicationResourceService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    public void save(ApplicationResourceLocation applicationResourceLocation) {
        String orgUnitId = applicationResourceLocation.getOrgUnitId();
        log.info("Trying to save application resource location - resource: {} {} orgunit: {} {}",
                applicationResourceLocation.getResourceId(),
                applicationResourceLocation.getResourceName(),
                orgUnitId,
                applicationResourceLocation.getOrgUnitName()
                );

        Optional<ApplicationResource> applicationResource =
                applicationResourceService.getApplicationResourceByResourceId(applicationResourceLocation.getResourceId());

        if (applicationResource.isEmpty()) {
           log.warn("Application resource with referenced resourceId {} does not exist in database, application resource location will not be saved",
                   applicationResourceLocation.getResourceId()
           );
           return;
        }
        Long applicationResourceId = applicationResource.get().getId();
        log.info("Found application resource with id {} in database based on referenced resourceId {}",
                applicationResourceId,
                applicationResourceLocation.getResourceId()
        );
        applicationResourceLocation.setResourceRef(applicationResource.get().getId());

        Optional<ApplicationResourceLocation> existingApplicationResourceLocation =
                applicationResourceLocationRepository.findByResourceRefAndOrgUnitId(applicationResourceId, orgUnitId);

        if (existingApplicationResourceLocation.isPresent()) {
            log.info("Application Resource location already exists in database. Updating existing application resource location {}",
                    existingApplicationResourceLocation.get().getId());
            applicationResourceLocation.setId(existingApplicationResourceLocation.get().getId());

        }

        ApplicationResourceLocation savedApplicationResourceLocation =
                applicationResourceLocationRepository.save(applicationResourceLocation);

        log.info("{} application resource location - resource: {} ({}) {} orgunit: {} {}",
                existingApplicationResourceLocation.isPresent() ? "Updated existing": "Saved new",
                savedApplicationResourceLocation.getResourceRef(),
                savedApplicationResourceLocation.getResourceId(),
                savedApplicationResourceLocation.getResourceName(),
                savedApplicationResourceLocation.getOrgUnitId(),
                savedApplicationResourceLocation.getOrgUnitName()
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
