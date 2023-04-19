package no.fintlabs.applicationResource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
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
        List<String> childresResourceIdsAppres1 = new ArrayList<>();
        childresResourceIdsAppres1.add("appResId2");
        List<String> validForRolesAppRes1_2 = new ArrayList<>();
        validForRolesAppRes1_2.add("student");

        //Overordna
        ApplicationResource appRes1 = new ApplicationResource();
        appRes1.setResourceId("appResId1");
        appRes1.setResourceName("Adobe K12 Utdanning");
        appRes1.setResourceLimit(1000L);
        appRes1.setResourceOwnerOrgUnitId("6");
        appRes1.setResourceOwnerName("KOMP Utdanning og kompetanse");
        appRes1.setResourceConsumerOrgUnitId("153");
        appRes1.setResourceConsumerName("KOMP Område sørvest");
        appRes1.setParentResourceId("appResId1");
        appRes1.setChildrenResourceId(childresResourceIdsAppres1);
        appRes1.setValidForRoles(validForRolesAppRes1_2);

        appRes1.setApplicationAccessType("ApplikasjonTilgang");
        appRes1.setApplicationAccessRole("Full access");
        appRes1.setPlatform("WIN");
        appRes1.setAccessType("device");
        appRes1.setApplicationId("AdobeK12");
        this.save(appRes1);


        //Underordna
        ApplicationResource appRes2 = new ApplicationResource();
        appRes2.setResourceId("appResId2");
        appRes2.setResourceName("Adobe K12 ved Storskog VGS");
        appRes2.setResourceType("ApplicationResource");
        appRes2.setResourceLimit(200L);
        appRes2.setResourceOwnerOrgUnitId("6");
        appRes2.setResourceOwnerName("KOMP Utdanning og kompetanse");
        appRes2.setResourceConsumerOrgUnitId("198");
        appRes2.setResourceConsumerName("VGSTOR Storskogen videregående skole");
        appRes2.setParentResourceId("appResId1");
        appRes2.setValidForRoles(validForRolesAppRes1_2);

        appRes2.setApplicationAccessType("ApplikasjonTilgang");
        appRes2.setApplicationAccessRole("Full access");
        appRes2.setPlatform("WIN");
        appRes2.setAccessType("device");
        appRes2.setApplicationId("AdobeK12");
        this.save(appRes2);
    }


}
