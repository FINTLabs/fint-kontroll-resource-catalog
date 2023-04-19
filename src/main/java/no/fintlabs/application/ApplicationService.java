package no.fintlabs.application;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.applicationResource.ApplicationResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Service
public class ApplicationService {
    private final ApplicationRepository applicationRepository;

    public ApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    public void save(Application application){
        applicationRepository
                .findApplicationByApplicationIdEndingWithIgnoreCase(application.getApplicationId())
                .ifPresentOrElse(onSaveExsistingApplication(application), onSaveNewApplication(application));
    }

    private Runnable onSaveNewApplication(Application application) {
        return () -> {
            Application newApplication =  applicationRepository.save(application);
        };
    }

    private Consumer<Application> onSaveExsistingApplication(Application application) {
        return exsistingApplication -> {
            application.setApplicationId(application.getApplicationId());
            applicationRepository.save(application);
        };
    }

    @PostConstruct
    public void init(){
        log.info("Starting up.....");
        List<String> appResId1 = new ArrayList<>();
        appResId1.add("Skole1");
        appResId1.add("Skole2");
        Application a1 = Application
                .builder()
                .applicationId("AdobeK12")
                .applicationName("Adobe Creative Cloud for education")
                .applicationResourceIds(appResId1)
                .build();
        this.save(a1);

    }

}
