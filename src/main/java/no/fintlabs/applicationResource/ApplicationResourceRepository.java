package no.fintlabs.applicationResource;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ApplicationResourceRepository extends JpaRepository<ApplicationResource, Long> {

    Optional<ApplicationResource> findApplicationResourceByResourceIdEqualsIgnoreCase(String resourceId);

    @Query("SELECT DISTINCT ar FROM ApplicationResource ar LEFT JOIN FETCH ar.validForRoles")
    List<ApplicationResource> findAllApplicationResources();

    @Query("select a from ApplicationResource a where a.id = ?1")
    Optional<ApplicationResource> getApplicationResourceById(Long id);


    @Query("select a from ApplicationResource a where upper(a.resourceName) like upper(concat('%', ?1, '%'))")
    List<ApplicationResource> getApplicationResourceBySearch(String resourceName);

    @Query("""
            select a from ApplicationResource a inner join a.validForOrgUnits validForOrgUnits
            where 
            validForOrgUnits.orgunitId in ?2
            and upper(a.resourceName) like upper(concat('%', ?1, '%'))
            """)
    List<ApplicationResource> findApplicationResourceByOrgUnitIds(String resourceName,Collection<String> orgUnitIDs);


}
