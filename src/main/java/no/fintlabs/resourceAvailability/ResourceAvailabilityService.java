package no.fintlabs.resourceAvailability;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ResourceAvailabilityService {
    private final ResourceAvailabilityRepository resourceAvailabilityRepository;

    public ResourceAvailabilityService(ResourceAvailabilityRepository resourceAvailabilityRepository) {
        this.resourceAvailabilityRepository = resourceAvailabilityRepository;
    }


    public void save(ResourceAvailability resourceAvailability) {
        log.info("Save resource availability: {}", resourceAvailability.toString());
        resourceAvailabilityRepository.save(resourceAvailability);

    }
}
