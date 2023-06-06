package no.fintlabs.applicationResource;

import org.springframework.stereotype.Service;

@Service
public class ApplicationResourceDTOSimplifiedService {
    private final ApplicationResourceEntityProducerService applicationResourceEntityProducerService;
    private final ApplicationResourceRepository applicationResourceRepository;

    public ApplicationResourceDTOSimplifiedService(ApplicationResourceEntityProducerService applicationResourceEntityProducerService,
                                                   ApplicationResourceRepository applicationResourceRepository) {
        this.applicationResourceEntityProducerService = applicationResourceEntityProducerService;
        this.applicationResourceRepository = applicationResourceRepository;
    }
    public void process(ApplicationResource applicationResource) {
        applicationResourceEntityProducerService.publish(applicationResource);
    }
}
