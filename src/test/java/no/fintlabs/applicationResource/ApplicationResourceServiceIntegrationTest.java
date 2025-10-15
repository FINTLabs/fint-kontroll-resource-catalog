package no.fintlabs.applicationResource;

import no.fintlabs.DatabaseIntegrationTest;
import no.fintlabs.OrgUnitType;
import no.fintlabs.ResponseFactory;
import no.fintlabs.applicationResourceLocation.ApplicationResourceLocation;
import no.fintlabs.applicationResourceLocation.ApplicationResourceLocationRepository;
import no.fintlabs.authorization.AuthorizationUtil;
import no.fintlabs.cache.FintCache;
import no.fintlabs.opa.OpaService;
import no.fintlabs.resourceGroup.AzureGroup;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@Import({ApplicationResourceService.class})
class ApplicationResourceServiceIntegrationTest extends DatabaseIntegrationTest {

    @Autowired
    private ApplicationResourceRepository applicationResourceRepository;
    @Autowired
    private ApplicationResourceLocationRepository applicationResourceLocationRepository;
    @Autowired
    private ApplicationResourceService applicationResourceService;
    @MockBean
    private FintCache<Long, AzureGroup> azureGroupCache;
    @MockBean
    private ResponseFactory responseFactory;
    @MockBean
    private AuthorizationUtil authorizationUtil;
    @MockBean
    private OpaService opaService;

    private final String varfk = "varfk";
    private final String kompavd = "kompavd";

    private final String zip = "zip";
    private final String kabal = "kabal";
    private final String adobek12 = "adobek12";
    private final String m365 = "m365";
    private final String adobek12old = "adobek12old";

    private final String student = "Student";
    private final String employee = "Employee";
    private final String freeAll = "FREEALL";
    private final String freeStudent = "FREESTUDENT";
    private final String hardStop = "HARDSTOP";

    private final List<String> statusListActive = List.of("ACTIVE");

    FintJwtEndUserPrincipal fintJwtEndUserPrincipal = new FintJwtEndUserPrincipal();
    Sort sort;
    Pageable pageable;

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
            .resourceName("Adobe Creative Cloud")
            .licenseEnforcement(hardStop)
            .validForRoles(Set.of(student))
            .validForOrgUnits(new HashSet<>())
            .status("ACTIVE")
            .build();

    ApplicationResource inactiveResource = ApplicationResource.builder()
            .resourceId(adobek12old)
            .resourceName("Adobe Creative Cloud Old")
            .licenseEnforcement(hardStop)
            .validForRoles(Set.of(student))
            .status("INACTIVE")
            .build();

    ApplicationResource unrestrictedResourceForAllKabal = ApplicationResource.builder()
            .resourceId(kabal)
            .resourceName("Microsoft Kabal")
            .licenseEnforcement(freeAll)
            .validForRoles(Set.of(student, employee))
            .validForOrgUnits(Set.of(kabal_varfk))
            .status("ACTIVE")
            .build();

    ApplicationResource unrestrictedResourceForAllZip = ApplicationResource.builder()
            .resourceId(zip)
            .licenseEnforcement(freeAll)
            .validForRoles(Set.of(student, employee))
            .validForOrgUnits(Set.of(zip_varfk))
            .status("ACTIVE")
            .build();

    ApplicationResource unRestrictedResourceForStudents = ApplicationResource.builder()
            .resourceId(m365)
            .resourceName("Microsoft 365 Student")
            .licenseEnforcement(freeStudent)
            .validForRoles(Set.of(student))
            .validForOrgUnits(Set.of(m365_varfk))
            .status("ACTIVE")
            .build();

    @BeforeEach
    public void setUp() {
        applicationResourceRepository.deleteAll();
        applicationResourceRepository.save(inactiveResource);

        fintJwtEndUserPrincipal.setMail("test@novari.no");
        sort = Sort.by(Sort.Order.asc("resourceName"));
        pageable = PageRequest.of(0, 10, sort);
    }

    @Test
    public void searchApplicationResourcesListWithRestrictedScopeShouldReturnRestrictedResourceInScopeAndAllFreeResources() {

        adobek12_kompavd.setApplicationResource(restrictedResource);

        restrictedResource.getValidForOrgUnits().add(adobek12_kompavd);

        applicationResourceRepository.save(restrictedResource);

        applicationResourceRepository.save(unrestrictedResourceForAllKabal);
        applicationResourceRepository.save(unrestrictedResourceForAllZip);
        applicationResourceRepository.save(unRestrictedResourceForStudents);

        given((opaService.getOrgUnitsInScope(Mockito.any(String.class)))).willReturn(List.of(kompavd));

        Page<ApplicationResource> applicationResourcesPage = applicationResourceService.searchApplicationResources(
                fintJwtEndUserPrincipal,
                null,
                null,
                null,
                null,
                null,
                null,
                statusListActive,
                pageable);

        List<ApplicationResource> applicationResourcesList = applicationResourcesPage.getContent();

        assertEquals(4, applicationResourcesList.size());
        assertEquals(Set.of(zip, kabal, adobek12, m365),
                Set.of(
                        applicationResourcesList.get(0).getResourceId(),
                        applicationResourcesList.get(1).getResourceId(),
                        applicationResourcesList.get(2).getResourceId(),
                        applicationResourcesList.get(3).getResourceId())
                );
    }

    @Test
    public void getAllApplicationResourcesForAdminsShouldReturnAllActiveAndInactiveResources() {

        applicationResourceRepository.save(restrictedResource);
        applicationResourceRepository.save(unrestrictedResourceForAllKabal);
        applicationResourceRepository.save(unrestrictedResourceForAllZip);
        applicationResourceRepository.save(unRestrictedResourceForStudents);

        given((opaService.getOrgUnitsInScope(Mockito.any(String.class)))).willReturn(List.of(OrgUnitType.ALLORGUNITS.name()));

        Page<ApplicationResource> applicationResourcesPage = applicationResourceService.searchApplicationResources(
                fintJwtEndUserPrincipal,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                pageable);

        List<ApplicationResource> applicationResourcesList = applicationResourcesPage.getContent();

        assertEquals(5, applicationResourcesList.size());
        assertEquals(Set.of(adobek12old, zip, kabal, adobek12, m365),
                Set.of(
                        applicationResourcesList.get(0).getResourceId(),
                        applicationResourcesList.get(1).getResourceId(),
                        applicationResourcesList.get(2).getResourceId(),
                        applicationResourcesList.get(3).getResourceId(),
                        applicationResourcesList.get(4).getResourceId())
        );
    }
    @Test
    public void searchApplicationResourcesWithRestrictedScopeAndFilteredOrgUnitShouldReturnResourceInScope() {

        adobek12_kompavd.setApplicationResource(restrictedResource);

        restrictedResource.getValidForOrgUnits().add(adobek12_kompavd);

        applicationResourceRepository.save(restrictedResource);

        applicationResourceRepository.save(unrestrictedResourceForAllKabal);
        applicationResourceRepository.save(unRestrictedResourceForStudents);

        given((opaService.getOrgUnitsInScope(Mockito.any(String.class)))).willReturn(List.of(kompavd));

        Page<ApplicationResource> applicationResourcesPage = applicationResourceService.searchApplicationResources(
                fintJwtEndUserPrincipal,
                null,
                List.of(kompavd),
                null,
                null,
                null,
                null,
                statusListActive,
                pageable);

        List<ApplicationResource> applicationResourcesList = applicationResourcesPage.getContent();

        assertEquals(1, applicationResourcesList.size());
        assertEquals(Set.of(adobek12),
                Set.of(applicationResourcesList.getFirst().getResourceId())
        );
    }

    @Test
    public void findBySearchCriteriaShouldReturnListSortedByResourceName() {
        applicationResourceRepository.save(unrestrictedResourceForAllKabal);
        applicationResourceRepository.save(restrictedResource);
        applicationResourceRepository.save(unRestrictedResourceForStudents);

        PageRequest pageRequest = PageRequest.of(0, 100, Sort.by("resourceName"));

        given((opaService.getOrgUnitsInScope(Mockito.any(String.class)))).willReturn(List.of(OrgUnitType.ALLORGUNITS.name()));

        Page<ApplicationResource> findBySearchCriteria = applicationResourceService.searchApplicationResources(
                fintJwtEndUserPrincipal,
                null,
                null,
                null,
                null,
                null,
                null,
                statusListActive,
                pageRequest);

        assertEquals(3, findBySearchCriteria.getTotalElements());
        assertEquals(adobek12, findBySearchCriteria.getContent().get(0).getResourceId());
        assertEquals(m365, findBySearchCriteria.getContent().get(1).getResourceId());
        assertEquals(kabal, findBySearchCriteria.getContent().get(2).getResourceId());
    }

    @Test
    public void savingExistingApplicationResource_WithExistingAzureInfoAndAzureCacheIsEmpty_ThenReturnUpdatedApplicationResourceWithAzureInfoIntact() {

        UUID idpGroupObjectId = UUID.randomUUID();
        ApplicationResource appResNew = ApplicationResource.builder()
                .resourceId(m365)
                .identityProviderGroupObjectId(idpGroupObjectId)
                .identityProviderGroupName("app-varfk-m365-kon")
                .validForOrgUnits(new HashSet<>(List.of(m365_varfk)))  // Convert to mutable HashSet
                .build();

        ApplicationResource savedAppRes1 = applicationResourceRepository.save(appResNew);

        given(azureGroupCache.getOptional(savedAppRes1.getId())).willReturn(Optional.empty());

        ApplicationResource appResUpdated  = ApplicationResource.builder()
                .resourceId(m365)
                .resourceName("Microsoft 365 Student")
                .validForOrgUnits(new HashSet<>(savedAppRes1.getValidForOrgUnits())) // Ensure mutability
                .build();

        applicationResourceService.save(appResUpdated);
        ApplicationResource savedAppResUpdated = applicationResourceRepository.findById(savedAppRes1.getId()).get();

        assertEquals(savedAppRes1.getId(), savedAppResUpdated.getId());
        assertEquals("Microsoft 365 Student", savedAppResUpdated.getResourceName());
        assertEquals(savedAppRes1.getIdentityProviderGroupObjectId(), savedAppResUpdated.getIdentityProviderGroupObjectId());
        assertEquals(savedAppRes1.getIdentityProviderGroupName(), savedAppResUpdated.getIdentityProviderGroupName());

    }
}
