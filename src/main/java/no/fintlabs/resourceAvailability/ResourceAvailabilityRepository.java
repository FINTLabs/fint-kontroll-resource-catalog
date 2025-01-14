package no.fintlabs.resourceAvailability;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResourceAvailabilityRepository extends JpaRepository<ResourceAvailability,Long> {
    Optional<ResourceAvailability> findByResourceId(String resourceId);

}
