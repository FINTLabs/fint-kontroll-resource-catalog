package no.fintlabs.resourceAvailability;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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
                .ifPresentOrElse(saveExistingResourceAvailability(resourceAvailability),
                        saveNewResourceAvailability(resourceAvailability));
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

            List<ResourceConsumerAssignment> mergedAssignments = new ArrayList<>(existingResourceAvailability.getResourceConsumerAssignments());

            for (ResourceConsumerAssignment newAssignment : resourceAvailability.getResourceConsumerAssignments()) {
                ResourceConsumerAssignment existingAssignment = mergedAssignments.stream()
                        .filter(a -> a.getOrgUnitId().equals(newAssignment.getOrgUnitId()))
                        .findFirst()
                        .orElse(null);

                if (existingAssignment != null) {
                    existingAssignment.setAssignedResources(newAssignment.getAssignedResources());
                } else {
                    mergedAssignments.add(newAssignment);
                }
            }

            resourceAvailability.setResourceConsumerAssignments(mergedAssignments);
            resourceAvailabilityRepository.save(resourceAvailability);
            log.info("Updated resourceAvailability with id: {}", existingResourceAvailability.getId());
        };
    }
}


