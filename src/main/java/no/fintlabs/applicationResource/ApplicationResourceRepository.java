package no.fintlabs.applicationResource;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicationResourceRepository extends JpaRepository<ApplicationResource,Long> {

    Optional<ApplicationResource> findApplicationResourceByResourceIdEqualsIgnoreCase(String resourceId);
}
