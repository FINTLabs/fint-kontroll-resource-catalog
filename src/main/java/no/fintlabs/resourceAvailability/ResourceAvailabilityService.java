package no.fintlabs.resourceAvailability;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
@Slf4j
public class ResourceAvailabilityService {
    private final ResourceAvailabilityRepository resourceAvailabilityRepository;

    public ResourceAvailabilityService(ResourceAvailabilityRepository resourceAvailabilityRepository) {
        this.resourceAvailabilityRepository = resourceAvailabilityRepository;
    }


    public void save(ResourceAvailability resourceAvailability) {
        log.info("Trying to save resourceAvailability: {}", resourceAvailability.toString());

        resourceAvailabilityRepository.findByResourceId(resourceAvailability.getResourceId())
                .ifPresentOrElse(saveExistingResourceAvailability(resourceAvailability), saveNewResourceAvailability(resourceAvailability));
    }

    private Runnable saveNewResourceAvailability(ResourceAvailability resourceAvailability) {
        return () -> {
            resourceAvailabilityRepository.save(resourceAvailability);
            log.info("Save new resourceAvailability with Id: {}", resourceAvailability.getId());
        };
    }

    private Consumer<ResourceAvailability> saveExistingResourceAvailability(ResourceAvailability resourceAvailability) {
        return existingResourceAvailability -> {
            resourceAvailability.setId(existingResourceAvailability.getId());
            resourceAvailabilityRepository.save(resourceAvailability);
            log.info("Update resourceAvailability with id: {}", resourceAvailability.getId());
        };
    }
}
