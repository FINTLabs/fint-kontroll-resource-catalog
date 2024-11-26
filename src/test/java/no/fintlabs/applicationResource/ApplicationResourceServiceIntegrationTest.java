package no.fintlabs.applicationResource;

import no.fintlabs.applicationResourceLocation.ApplicationResourceLocation;
import no.fintlabs.authorization.AuthorizationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
class ApplicationResourceServiceIntegrationTest extends DatabaseIntegrationTest {

    @Autowired
    private ApplicationResourceRepository applicationResourceRepository;
    @Autowired
    private ApplicationResourceService applicationResourceService;
    @Autowired
    private AuthorizationUtil authorizationUtil;

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
    List<ApplicationResourceLocation> resourceLocationsRes2 = List.of(res2_OrgUnitId1, res2_OrgUnitId2);


    ApplicationResourceLocation res3_OrgUnitId1 = ApplicationResourceLocation.builder()
            .resourceId("res3")
            .orgUnitId("orgUnitId1")
            .build();
    ApplicationResourceLocation res3_OrgUnitId2 = ApplicationResourceLocation.builder()
            .resourceId("res3")
            .orgUnitId("orgUnitId2")
            .build();
    List<ApplicationResourceLocation> resourceLocationsRes3 = List.of(res3_OrgUnitId1, res3_OrgUnitId2);

    ApplicationResourceLocation res4_OrgUnitId1 = ApplicationResourceLocation.builder()
            .resourceId("res4")
            .orgUnitId("orgUnitId1")
            .build();

    ApplicationResource restrictedResource = ApplicationResource.builder()
            .resourceId("res1")
            .licenseEnforcement("HARDSTOP")
            .validForRoles(List.of("Student"))
            .validForOrgUnits(List.of(res1_orgUnitId1))
            .build();

    ApplicationResource unrestrictedResourceForAll = ApplicationResource.builder()
            .resourceId("res2")
            .licenseEnforcement("FREE-ALL")
            .validForRoles(List.of("Student", "Employee"))
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
            .validForOrgUnits(List.of(res4_OrgUnitId1))
            .build();

    @BeforeEach
    public void setUp() {
        applicationResourceRepository.deleteAll();
    }
    @Test
    public void getApplicationResourceDTOFrontendListWithRestrictedScopeShouldReturnRestrictedResourceInScopeAndAllFreeResources() {
        applicationResourceRepository.save(restrictedResource);
        applicationResourceRepository.save(unrestrictedResourceForAll);
        applicationResourceRepository.save(unRestrictedResourceForStudents);

        given(authorizationUtil.getAllAuthorizedOrgUnitIDs()).willReturn(List.of("orgUnitId1"));

        List<ApplicationResourceDTOFrontendList> resourceDTOFrontendList =
                applicationResourceService.getApplicationResourceDTOFrontendList(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);
        assertEquals(3, resourceDTOFrontendList.size());
        assertEquals(Set.of("res1","res2", "res3"),
                Set.of(
                    resourceDTOFrontendList.get(0).getResourceId(),
                    resourceDTOFrontendList.get(1).getResourceId()),
                    resourceDTOFrontendList.get(2).getResourceId()
                );

    }
}