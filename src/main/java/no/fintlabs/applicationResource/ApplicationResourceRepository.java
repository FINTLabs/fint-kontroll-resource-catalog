package no.fintlabs.applicationResource;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ApplicationResourceRepository extends JpaRepository<ApplicationResource, Long>, JpaSpecificationExecutor<ApplicationResource> {

    Optional<ApplicationResource> findApplicationResourceByResourceIdEqualsIgnoreCase(String resourceId);

    @Query("""
            select a from ApplicationResource a inner join a.validForOrgUnits validForOrgUnits
            where
            validForOrgUnits.orgUnitId in ?2
            and upper(a.resourceName) like upper(concat('%', ?1, '%'))
            """)
    List<ApplicationResource> findApplicationResourceByOrgUnitIds(String resourceName,Collection<String> orgUnitIDs);

    @Query("""
            select a from ApplicationResource a
            where upper(a.resourceName) like upper(concat('%', ?1, '%'))
            """)
    List<ApplicationResource> findApplicationResourceByResourceName(String resourceName);


    @Query(value = "SELECT DISTINCT application_category FROM application_resource_application_category", nativeQuery = true)
    List<String> findAllDistinctApplicationCategories();

    @Query(value = "SELECT distinct access_type from application_resource",nativeQuery = true)
    List<String> findAllDistinctAccessTypes();





}
