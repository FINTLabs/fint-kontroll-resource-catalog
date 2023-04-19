package no.fintlabs.applicationResource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.function.Consumer;

@Slf4j
@Service
public class ApplicationResourceService {
    private final ApplicationResourceRepository applicationResourceRepository;

    public ApplicationResourceService(ApplicationResourceRepository applicationResourceRepository) {
        this.applicationResourceRepository = applicationResourceRepository;
    }

    public void save(ApplicationResource applicationResource){
        applicationResourceRepository
                .findApplicationResourceByResourceIdEqualsIgnoreCase(applicationResource.getResourceId())
                .ifPresentOrElse(onSaveExistingApplicationResource(applicationResource),
                        onSaveNewApplicationResource(applicationResource));
    }

    private Runnable onSaveNewApplicationResource(ApplicationResource applicationResource) {
        return () -> {
            ApplicationResource newApplicationResource = applicationResourceRepository.save(applicationResource);
        };
    }

    private Consumer<ApplicationResource> onSaveExistingApplicationResource(ApplicationResource applicationResource) {
        return existingApplicationResource -> {
            applicationResource.setId(existingApplicationResource.getId());
            applicationResourceRepository.save(applicationResource);
        };
    }

    @PostConstruct
    public void init(){
        log.info("Starting applicationResourceService....");
        ApplicationResource appRes1 = new ApplicationResource();
        appRes1.setResourceId("appResId1");
        appRes1.setResourceName("Adobe K12 ved Skole1");
        appRes1.setApplicationId("AdobeK12");

        this.save(appRes1);

    }


}
