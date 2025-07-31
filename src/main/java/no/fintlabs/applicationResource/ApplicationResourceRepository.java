package no.fintlabs.applicationResource;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationResourceRepository extends JpaRepository<ApplicationResource, Long>, JpaSpecificationExecutor<ApplicationResource> {

    Optional<ApplicationResource> findApplicationResourceByResourceIdEqualsIgnoreCase(String resourceId);

    @Query(value = "SELECT DISTINCT application_category FROM application_resource_application_category", nativeQuery = true)
    List<String> findAllDistinctApplicationCategories();

    @Query(value = "SELECT distinct access_type from application_resource", nativeQuery = true)
    List<String> findAllDistinctAccessTypes();
}
