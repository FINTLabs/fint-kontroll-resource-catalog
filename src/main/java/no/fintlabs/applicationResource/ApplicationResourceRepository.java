package no.fintlabs.applicationResource;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApplicationResourceRepository extends JpaRepository<ApplicationResource, Long>, JpaSpecificationExecutor<ApplicationResource> {

    Optional<ApplicationResource> findApplicationResourceByResourceIdEqualsIgnoreCase(String resourceId);

    Optional<ApplicationResource> findApplicationResourceByIdentityProviderGroupObjectId(UUID identityProviderGroupObjectId);

    List<ApplicationResource> findAllByEntraState(String entraState);

    @Query(value = """
            SELECT DISTINCT ak.name
            FROM application_resource_application_category arac
            JOIN applikasjonskategori_kodeverk ak
                ON ak.id = arac.applikasjonskategori_id
            ORDER BY ak.name
            """, nativeQuery = true)
    List<String> findAllDistinctApplicationCategories();

    @Modifying
    @Query(value = "DELETE FROM application_resource_application_category WHERE applikasjonskategori_id IN (:applicationCategories)", nativeQuery = true)
    int deleteApplicationCategories(@Param("applicationCategories") Collection<Long> applicationCategories);

    @Query(value = "SELECT distinct access_type from application_resource", nativeQuery = true)
    List<String> findAllDistinctAccessTypes();
}
