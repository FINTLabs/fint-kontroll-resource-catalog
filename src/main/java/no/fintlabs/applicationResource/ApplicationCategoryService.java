package no.fintlabs.applicationResource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ApplicationCategoryService {
    private final ApplicationResourceRepository applicationResourceRepository;

    public ApplicationCategoryService(ApplicationResourceRepository applicationResourceRepository) {
        this.applicationResourceRepository = applicationResourceRepository;
    }


    public List<String> getAllApplicationCategories() {

        return applicationResourceRepository.findAllDistinctApplicationCategories();
    }
}
