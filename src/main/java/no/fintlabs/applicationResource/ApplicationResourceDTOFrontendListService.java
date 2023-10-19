package no.fintlabs.applicationResource;

import org.springframework.stereotype.Service;

@Service
public class ApplicationResourceDTOFrontendListService {
    private final ApplicationResourceEntityProducerService applicationResourceEntityProducerService;
    private final ApplicationResourceRepository applicationResourceRepository;

    public ApplicationResourceDTOFrontendListService(ApplicationResourceEntityProducerService applicationResourceEntityProducerService,
                                                     ApplicationResourceRepository applicationResourceRepository) {
        this.applicationResourceEntityProducerService = applicationResourceEntityProducerService;
        this.applicationResourceRepository = applicationResourceRepository;
    }

}
