package no.fintlabs.applicationResourceLocation;

import no.fintlabs.applicationResource.ApplicationResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ApplicationResourceLocationRepository extends JpaRepository<ApplicationResourceLocation, Long> {
    Optional<ApplicationResourceLocation> findByApplicationResourceAndOrgUnitId(ApplicationResource resource, String orgUnitId);

    List<ApplicationResourceLocation> getDistinctByOrgUnitIdIsIn(List<String> orgUnitsInScope);
}
