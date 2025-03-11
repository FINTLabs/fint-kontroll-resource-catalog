package no.fintlabs.applicationResource;

import no.fintlabs.DatabaseIntegrationTest;
import no.fintlabs.applicationResourceLocation.ApplicationResourceLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
class ApplicationResourceSpesificationBuilderIntegrationTest extends DatabaseIntegrationTest {

    @Autowired
    private ApplicationResourceRepository applicationResourceRepository;

    ApplicationResourceLocation res1_orgUnitId1 = ApplicationResourceLocation.builder()
            .orgUnitId("orgUnitId1")
            .resourceId("res1")
            .build();

    ApplicationResourceLocation res2_OrgUnitId1 = ApplicationResourceLocation.builder()
            .resourceId("res2")
            .orgUnitId("orgUnitId1")
            .build();
    ApplicationResourceLocation res2_OrgUnitId2 = ApplicationResourceLocation.builder()
            .resourceId("res2")
            .orgUnitId("orgUnitId2")
            .build();
    Set<ApplicationResourceLocation> resourceLocationsRes2 = Set.of(res2_OrgUnitId1, res2_OrgUnitId2);


    ApplicationResourceLocation res3_OrgUnitId1 = ApplicationResourceLocation.builder()
            .resourceId("res3")
            .orgUnitId("orgUnitId1")
            .build();
    ApplicationResourceLocation res3_OrgUnitId2 = ApplicationResourceLocation.builder()
            .resourceId("res3")
            .orgUnitId("orgUnitId2")
            .build();
    Set<ApplicationResourceLocation> resourceLocationsRes3 = Set.of(res3_OrgUnitId1, res3_OrgUnitId2);

    ApplicationResourceLocation res4_OrgUnitId1 = ApplicationResourceLocation.builder()
            .resourceId("res4")
            .orgUnitId("orgUnitId1")
            .build();

    ApplicationResource restrictedResource = ApplicationResource.builder()
            .resourceId("res1")
            .licenseEnforcement("HARDSTOP")
            .validForRoles(List.of("Student"))
            .validForOrgUnits(Set.of(res1_orgUnitId1))
            .build();

    ApplicationResource unrestrictedResourceForAll = ApplicationResource.builder()
            .resourceId("res2")
            .licenseEnforcement("FREE-ALL")
            .validForRoles(List.of("Student","Employee"))
            .validForOrgUnits(resourceLocationsRes2)
            .build();

    ApplicationResource unRestrictedResourceForStudents = ApplicationResource.builder()
            .resourceId("res3")
            .licenseEnforcement("FREE-STUDENT")
            .validForRoles(List.of("Student"))
            .validForOrgUnits(resourceLocationsRes3)
            .build();

    ApplicationResource restrictedResourceFloating = ApplicationResource.builder()
            .resourceId("res4")
            .licenseEnforcement("FLOATING")
            .validForRoles(List.of("Employee"))
            .validForOrgUnits(Set.of(res4_OrgUnitId1))
            .build();

//    @BeforeEach
//    public void setUp() {
//        applicationResourceRepository.deleteAll();
//        applicationResourceRepository.save(restrictedResource);
//    }
//    @Test
//    void shouldGetAllResourcesWhenAuthorizedToRestrictedResource() {
//        applicationResourceRepository.save(unrestrictedResourceForAll);
//
//        Specification<ApplicationResource> specification =
//                new AppicationResourceSpesificationBuilder(null, null, null, List.of("orgUnitId1"),null, null,
//                null, null, null, null).build();
//
//        List<ApplicationResource> resources = applicationResourceRepository.findAll(specification);
//        assertEquals(2, resources.size());
//    }
//    @Test
//    void shouldGetOnlyFilteredOrgUnitResourcesWhenAuthorizedToRestrictedResourceAndFilterOrgUnitsIsSet() {
//        applicationResourceRepository.save(restrictedResourceFloating);
//        applicationResourceRepository.save(unrestrictedResourceForAll);
//        applicationResourceRepository.save(unRestrictedResourceForStudents);
//
//        Specification<ApplicationResource> specification =
//                new AppicationResourceSpesificationBuilder(null, List.of("orgUnitId2"),List.of("orgUnitId1"), null,
//                null, null, null, null).build();
//
//        List<ApplicationResource> resources = applicationResourceRepository.findAll(specification);
//        assertEquals(2, resources.size());
//        assertEquals(Set.of("res2", "res3"), Set.of(resources.get(0).getResourceId(), resources.get(1).getResourceId()));
//    }
//    @Test
//    void shouldGetResourceWithUserTypeEmployeeWhenFilteredByEmployeeUserType() {
//        applicationResourceRepository.save(unrestrictedResourceForAll);
//        Specification<ApplicationResource> specification = new AppicationResourceSpesificationBuilder(null, List.of("orgUnitId1"), null,null,
//                List.of("Employee"), null, null, null).build();
//
//        List<ApplicationResource> resources = applicationResourceRepository.findAll(specification);
//        assertEquals(1, resources.size());
//        assertTrue(resources.getFirst().getValidForRoles().contains("Employee"));
//    }
//    @Test
//    void shouldGetAllResourcesWithUserTypeStudentWhenFilteredByStudentUserType() {
//        applicationResourceRepository.save(unrestrictedResourceForAll);
//        applicationResourceRepository.save(unRestrictedResourceForStudents);
//        Specification<ApplicationResource> specification = new AppicationResourceSpesificationBuilder(null, List.of("orgUnitId1"), null,null,
//                List.of("Student"), null, null, null).build();
//
//        List<ApplicationResource> resources = applicationResourceRepository.findAll(specification);
//        assertEquals(3, resources.size());
//    }
//    @Test
//    void shouldGetTwoFreeResourcesWhenNotAuthorizedToRestrictedResource() {
//        applicationResourceRepository.save(unrestrictedResourceForAll);
//        applicationResourceRepository.save(unRestrictedResourceForStudents);
//        Specification<ApplicationResource> specification = new AppicationResourceSpesificationBuilder(null, List.of("orgUnitId2"),null, null,
//                null, null, null, null).build();
//
//        List<ApplicationResource> resources = applicationResourceRepository.findAll(specification);
//        assertEquals(2, resources.size());
//        assertEquals(Set.of("res2", "res3"), Set.of(resources.get(0).getResourceId(), resources.get(1).getResourceId()));
//    }
//    @Test
//    void shouldGetOneFreeResourcesWhenNotAuthorizedToRestrictedResourceAndFilteredByUserTypeEmployee() {
//        applicationResourceRepository.save(unrestrictedResourceForAll);
//        applicationResourceRepository.save(unRestrictedResourceForStudents);
//        Specification<ApplicationResource> specification = new AppicationResourceSpesificationBuilder( null,List.of("orgUnitId2"), null,null,
//                List.of("Employee"), null, null, null).build();
//
//        List<ApplicationResource> resources = applicationResourceRepository.findAll(specification);
//        assertEquals(1, resources.size());
//        assertEquals("res2", resources.getFirst().getResourceId());
//    }
//    @Test
//    void shouldGetNoResourcesWhenFilteredByUserTypeEmployeeAndNotAuthorizedToRestrictedResource() {
//        applicationResourceRepository.save(unRestrictedResourceForStudents);
//        Specification<ApplicationResource> specification = new AppicationResourceSpesificationBuilder(null, List.of("orgUnitId2"),null, null,
//                List.of("Employee"), null, null, null).build();
//
//        List<ApplicationResource> resources = applicationResourceRepository.findAll(specification);
//        assertEquals(0, resources.size());
//    }
}