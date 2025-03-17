package no.fintlabs.applicationResourceLocation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ApplicationResourceLocationRepository extends JpaRepository<ApplicationResourceLocation, Long> {
    Optional<ApplicationResourceLocation> findByResourceRefAndOrgUnitId(Long resourceRef, String orgUnitId);

    List<ApplicationResourceLocation> getDistinctByOrOrgUnitIdIsIn(List<String> orgUnitsInScope);
}
