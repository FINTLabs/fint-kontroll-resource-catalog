package no.fintlabs.applicationResource;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ApplicationResourceRepository extends JpaRepository<ApplicationResource, Long> {

    Optional<ApplicationResource> findApplicationResourceByResourceIdEqualsIgnoreCase(String resourceId);

    @Query("SELECT DISTINCT ar FROM ApplicationResource ar LEFT JOIN FETCH ar.validForRoles")
    List<ApplicationResource> findAllApplicationResources();

    @Query(value = "SELECT ar.*, vr.*, vo.* " +
            "FROM application_resource ar LEFT JOIN (SELECT * FROM application_resource_valid_for_roles valid_for_role) " +
            "vr ON ar.id = vr.application_resource_id LEFT JOIN (SELECT * FROM application_resource_valid_for_org_units) " +
            "vo ON ar.id = vo.application_resource_id",nativeQuery = true)
    List<ApplicationResource> findAllEagerlyNative();



}
