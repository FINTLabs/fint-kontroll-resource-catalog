package no.fintlabs.applicationResource;

import no.fintlabs.ResponseFactory;
import no.fintlabs.applicationResourceLocation.ApplicationResourceLocation;
import no.fintlabs.authorization.AuthorizationUtil;
import no.fintlabs.cache.FintCache;
import no.fintlabs.resourceGroup.AzureGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@Import({ApplicationResourceService.class})
class ApplicationResourceServiceIntegrationTest extends DatabaseIntegrationTest {

    @Autowired
    private ApplicationResourceRepository applicationResourceRepository;
    @Autowired
    private ApplicationResourceService applicationResourceService;
    @MockBean
    private FintCache<Long, AzureGroup> azureGroupCache;
    @MockBean
    private ResponseFactory responseFactory;
    @MockBean
    private AuthorizationUtil authorizationUtil;
    
    private final String varfk = "varfk";
    private final String kompavd = "kompavd";
    
    private final String zip = "zip";
    private final String kabal = "kabal";
    private final String adobek12 = "adobek12";
    private final String m365 = "m365";

    private final String student = "Student";
    private final String employee = "Employee";
    private final String freeAll = "FREEALL";
    private final String freeStudent = "FREESTUDENT";
    private final String hardStop = "HARDSTOP";

    ApplicationResourceLocation zip_varfk = ApplicationResourceLocation.builder()
            .resourceId(zip)
            .orgUnitId(varfk)
            .build();

    ApplicationResourceLocation kabal_varfk = ApplicationResourceLocation.builder()
            .resourceId(kabal)
            .orgUnitId(varfk)
            .build();

    ApplicationResourceLocation adobek12_kompavd = ApplicationResourceLocation.builder()
            .resourceId(adobek12)
            .orgUnitId(kompavd)
            .build();

    ApplicationResourceLocation m365_varfk = ApplicationResourceLocation.builder()
            .resourceId(m365)
            .orgUnitId(varfk)
            .build();

    ApplicationResource restrictedResource = ApplicationResource.builder()
            .resourceId(adobek12)
            .licenseEnforcement(hardStop)
            .validForRoles(List.of(student))
            .validForOrgUnits(List.of(adobek12_kompavd))
            .build();

    ApplicationResource unrestrictedResourceForAllKabal = ApplicationResource.builder()
            .resourceId(kabal)
            .licenseEnforcement(freeAll)
            .validForRoles(List.of(student, employee))
            .validForOrgUnits(List.of(kabal_varfk))
            .build();
    ApplicationResource unrestrictedResourceForAllZip = ApplicationResource.builder()
            .resourceId(zip)
            .licenseEnforcement(freeAll)
            .validForRoles(List.of(student, employee))
            .validForOrgUnits(List.of(zip_varfk))
            .build();

    ApplicationResource unRestrictedResourceForStudents = ApplicationResource.builder()
            .resourceId(m365)
            .licenseEnforcement(freeStudent)
            .validForRoles(List.of(student))
            .validForOrgUnits(List.of(m365_varfk))
            .build();

    @BeforeEach
    public void setUp() {
        applicationResourceRepository.deleteAll();
    }
    @Test
    public void getApplicationResourceDTOFrontendListWithRestrictedScopeShouldReturnRestrictedResourceInScopeAndAllFreeResources() {
        applicationResourceRepository.save(restrictedResource);
        applicationResourceRepository.save(unrestrictedResourceForAllKabal);
        applicationResourceRepository.save(unrestrictedResourceForAllZip);
        applicationResourceRepository.save(unRestrictedResourceForStudents);

        given(authorizationUtil.getAllAuthorizedOrgUnitIDs()).willReturn(List.of(kompavd));

        List<ApplicationResourceDTOFrontendList> resourceDTOFrontendList =
                applicationResourceService.getApplicationResourceDTOFrontendList(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);
        assertEquals(4, resourceDTOFrontendList.size());
        assertEquals(Set.of(zip, kabal, adobek12, m365),
                Set.of(
                    resourceDTOFrontendList.get(0).getResourceId(),
                    resourceDTOFrontendList.get(1).getResourceId(),
                    resourceDTOFrontendList.get(2).getResourceId(),
                    resourceDTOFrontendList.get(3).getResourceId())
                );
    }
    @Test
    public void getApplicationResourceDTOFrontendListWithRestrictedScopeAndFilteredOrgUnitShouldReturnResourceInScope() {
        applicationResourceRepository.save(restrictedResource);
        applicationResourceRepository.save(unrestrictedResourceForAllKabal);
        applicationResourceRepository.save(unRestrictedResourceForStudents);

        given(authorizationUtil.getAllAuthorizedOrgUnitIDs()).willReturn(List.of(kompavd));

        List<ApplicationResourceDTOFrontendList> resourceDTOFrontendList =
                applicationResourceService.getApplicationResourceDTOFrontendList(
                        null,
                        List.of(kompavd),
                        null,
                        null,
                        null,
                        null,
                        null);
        assertEquals(1, resourceDTOFrontendList.size());
        assertEquals(Set.of(adobek12),
                Set.of(resourceDTOFrontendList.getFirst().getResourceId())
        );
    }

    @Test
    public void savingExistingApplicationResource_WithExistingAzureInfoAndAzureCacheIsEmpty_ThenReturnUpdatedApplicationResourceWithAzureInfoIntact() {

        UUID idpGroupObjectId = UUID.randomUUID();
        ApplicationResource appResNew = ApplicationResource.builder()
                .resourceId(m365)
                .identityProviderGroupObjectId(idpGroupObjectId)
                .identityProviderGroupName("app-varfk-m365-kon")
                .build();

        ApplicationResource appResUpdated  = ApplicationResource.builder()
                .resourceId(m365)
                .resourceName("Microsoft 365 Student")
                .build();

        ApplicationResource savedAppRes1 = applicationResourceRepository.saveAndFlush(appResNew);

        given(azureGroupCache.getOptional(savedAppRes1.getId())).willReturn(Optional.empty());

        applicationResourceService.save(appResUpdated);
        ApplicationResource savedAppResUpdated = applicationResourceRepository.findById(savedAppRes1.getId()).get();

        assertEquals(savedAppRes1.getId(), savedAppResUpdated.getId());
        assertEquals("Microsoft 365 Student", savedAppResUpdated.getResourceName());
        assertEquals(savedAppRes1.getIdentityProviderGroupObjectId(), savedAppResUpdated.getIdentityProviderGroupObjectId());
        assertEquals(savedAppRes1.getIdentityProviderGroupName(), savedAppResUpdated.getIdentityProviderGroupName());

    }
}