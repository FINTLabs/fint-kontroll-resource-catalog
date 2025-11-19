package no.fintlabs.applicationResource;

import no.fintlabs.applicationResourceLocation.ApplicationResourceLocation;
import no.fintlabs.applicationResourceLocation.ApplicationResourceLocationRepository;
import no.fintlabs.authorization.AuthorizationUtil;
import no.fintlabs.cache.FintCache;
import no.fintlabs.opa.OpaService;
import no.fintlabs.kodeverk.handhevingstype.Handhevingstype;
import no.fintlabs.kodeverk.handhevingstype.HandhevingstypeLabels;
import no.fintlabs.resourceGroup.AzureGroup;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationResourceServiceTest {

    @Mock
    private ApplicationResourceRepository applicationResourceRepository;

    @Mock
    private ApplicationResourceLocationRepository applicationResourceLocationRepository;

    @Mock
    private FintCache<Long, AzureGroup> azureGroupCache;

    @Mock
    private AuthorizationUtil authorizationUtil;

    @Mock
    private OpaService opaService;

    private ApplicationResourceService applicationResourceService;

    @Captor
    private ArgumentCaptor<ApplicationResource> appResourceCaptor;

    @BeforeEach
    void setup() {
        applicationResourceService = new ApplicationResourceService(
                applicationResourceRepository,
                applicationResourceLocationRepository,
                azureGroupCache,
                authorizationUtil,
                opaService
        );
    }

    @Test
    public void getApplicationResourceByIdShouldReturnDTOIfAuthorized() {
        ApplicationResource appRes1 = new ApplicationResource();
        appRes1.setResourceId("adobek12");
        appRes1.setResourceOwnerOrgUnitId("3");
        appRes1.setIdentityProviderGroupName("fint-app-adobe-k12-agg-kon");
        ApplicationResourceLocation applicationResourceLocation1 = ApplicationResourceLocation
                .builder()
                .resourceId("adobek12")
                .orgUnitId("1")
                .orgUnitName("VGMIDT Midtbyen videregående skole")
                .resourceLimit(100L)
                .build();
        ApplicationResourceLocation applicationResourceLocation2 = ApplicationResourceLocation
                .builder()
                .resourceId("adobek12")
                .orgUnitId("2")
                .orgUnitName("VGSTOR Storskog videregående skole")
                .resourceLimit(200L)
                .build();

        Set<ApplicationResourceLocation> locationsAppRes1 = Set.of(applicationResourceLocation1, applicationResourceLocation2);
        appRes1.getValidForOrgUnits().addAll(locationsAppRes1);

        when(authorizationUtil.getAllAuthorizedOrgUnitIDs()).thenReturn(List.of("1", "2", "3"));
        when(applicationResourceRepository.findById(1L)).thenReturn(Optional.of(appRes1));

        ApplicationResourceDTOFrontendDetail applicationResourceDTOFrontendDetail = applicationResourceService
                .getApplicationResourceDTOFrontendDetailById(1L);

        String resourceIdDTO = applicationResourceDTOFrontendDetail.getResourceId();
        String resourceIdAppres = appRes1.getResourceId();

        assertEquals(resourceIdDTO, resourceIdAppres, "resourceIdDTO should be adobek12");
        System.out.println("resourceId should be adobek12: " + resourceIdDTO);

        verify(applicationResourceRepository, times(1)).findById(1L);
    }

    @Test
    public void getApplicationResourceByIdShouldReturnEmptyDTOIfNOTAuthorized() {
        ApplicationResource appRes1 = new ApplicationResource();
        appRes1.setResourceId("adobek12");
        appRes1.setResourceOwnerOrgUnitId("3");
        appRes1.setLicenseEnforcement(HandhevingstypeLabels.HARDSTOP.name());

        ApplicationResourceLocation applicationResourceLocation1 = ApplicationResourceLocation
                .builder()
                .resourceId("adobek12")
                .orgUnitId("1")
                .orgUnitName("VGMIDT Midtbyen videregående skole")
                .resourceLimit(100L)
                .build();

        ApplicationResourceLocation applicationResourceLocation2 = ApplicationResourceLocation
                .builder()
                .resourceId("adobek12")
                .orgUnitId("2")
                .orgUnitName("VGSTOR Storskog videregående skole")
                .resourceLimit(200L)
                .build();

        Set<ApplicationResourceLocation> locationsAppRes1 = Set.of(applicationResourceLocation1, applicationResourceLocation2);
        appRes1.getValidForOrgUnits().addAll(locationsAppRes1);

        when(authorizationUtil.getAllAuthorizedOrgUnitIDs()).thenReturn(List.of("4", "5", "6"));
        when(applicationResourceRepository.findById(1L)).thenReturn(Optional.of(appRes1));

        ApplicationResourceDTOFrontendDetail applicationResourceDTOFrontendDetail = applicationResourceService
                .getApplicationResourceDTOFrontendDetailById(1L);

        String resourceIdDTO = applicationResourceDTOFrontendDetail.getResourceId();
        String resourceIdAppres = appRes1.getResourceId();

        assertNotEquals(resourceIdDTO, resourceIdAppres, "resourceIdDTO should be null");
        System.out.println("resourceId should be null : " + resourceIdDTO);
    }

    @Test
    public void getApplicationResourceByIdShouldReturnDTOIfResourceIsUnRestricted() {
        ApplicationResource appRes1 = new ApplicationResource();
        appRes1.setResourceId("zip");
        appRes1.setResourceOwnerOrgUnitId("3");
        appRes1.setLicenseEnforcement(HandhevingstypeLabels.FREEALL.name());

        ApplicationResourceLocation applicationResourceLocation1 = ApplicationResourceLocation
                .builder()
                .resourceId("zip")
                .orgUnitId("1")
                .orgUnitName("VGMIDT Midtbyen videregående skole")
                .resourceLimit(100L)
                .build();

        ApplicationResourceLocation applicationResourceLocation2 = ApplicationResourceLocation
                .builder()
                .resourceId("zip")
                .orgUnitId("2")
                .orgUnitName("VGSTOR Storskog videregående skole")
                .resourceLimit(200L)
                .build();

        Set<ApplicationResourceLocation> locationsAppRes1 = Set.of(applicationResourceLocation1, applicationResourceLocation2);
        appRes1.getValidForOrgUnits().addAll(locationsAppRes1);

        when(authorizationUtil.getAllAuthorizedOrgUnitIDs()).thenReturn(List.of("4", "5", "6"));
        when(applicationResourceRepository.findById(1L)).thenReturn(Optional.of(appRes1));

        ApplicationResourceDTOFrontendDetail applicationResourceDTOFrontendDetail = applicationResourceService
                .getApplicationResourceDTOFrontendDetailById(1L);

        String resourceIdDTO = applicationResourceDTOFrontendDetail.getResourceId();
        String resourceIdAppres = appRes1.getResourceId();

        assertEquals(resourceIdDTO, resourceIdAppres, "resourceIdDTO should equal");
    }

    @Test
    public void getApplicationResourceByIdShouldReturnDTOIfAuthorizedForResourceOwnerOrgUnitId() {
        ApplicationResource appRes1 = new ApplicationResource();
        appRes1.setResourceId("adobek12");
        appRes1.setResourceOwnerOrgUnitId("3");
        ApplicationResourceLocation applicationResourceLocation1 = ApplicationResourceLocation
                .builder()
                .resourceId("adobek12")
                .orgUnitId("1")
                .orgUnitName("VGMIDT Midtbyen videregående skole")
                .resourceLimit(100L)
                .build();
        ApplicationResourceLocation applicationResourceLocation2 = ApplicationResourceLocation
                .builder()
                .resourceId("adobek12")
                .orgUnitId("2")
                .orgUnitName("VGSTOR Storskog videregående skole")
                .resourceLimit(200L)
                .build();
        Set<ApplicationResourceLocation> locationsAppRes1 = Set.of(applicationResourceLocation1, applicationResourceLocation2);
        appRes1.getValidForOrgUnits().addAll(locationsAppRes1);


        when(authorizationUtil.getAllAuthorizedOrgUnitIDs()).thenReturn(List.of("3", "4", "5", "6"));
        when(applicationResourceRepository.findById(1L)).thenReturn(Optional.of(appRes1));

        ApplicationResourceDTOFrontendDetail applicationResourceDTOFrontendDetail = applicationResourceService
                .getApplicationResourceDTOFrontendDetailById(1L);

        String resourceIdDTO = applicationResourceDTOFrontendDetail.getResourceId();
        String resourceIdAppres = appRes1.getResourceId();

        assertEquals(resourceIdDTO, resourceIdAppres, "resourceIdDTO should be adobek12");
        System.out.println("resourceId should be: " + resourceIdDTO);
    }

    @Test
    void shouldSaveNewApplicationResource() {
        String resourceId = "APP-1";
        ApplicationResource newResource = new ApplicationResource();
        newResource.setId(1L);
        newResource.setResourceId(resourceId);
        newResource.setResourceName("My New App");

        when(applicationResourceRepository
                .findApplicationResourceByResourceIdEqualsIgnoreCase(resourceId))
                .thenReturn(Optional.empty());

        applicationResourceService.save(newResource);

        verify(applicationResourceRepository).save(newResource);
        verify(azureGroupCache, never()).getOptional(anyLong());
    }


    @Test
    void shouldUpdateExistingApplicationResourceAndPopulateAllFields() {
        String resourceId = "APP-1";

        // ---------- INCOMING (new data) ----------
        ApplicationResource incoming = new ApplicationResource();
        incoming.setId(10L);
        incoming.setResourceId(resourceId);

        incoming.setApplicationAccessType("NEW_ACCESS_TYPE");
        incoming.setApplicationAccessRole("NEW_ROLE");
        incoming.setPlatform(Set.of("NEW_PLATFORM"));
        incoming.setAccessType("NEW_ACCESS_TYPE");
        incoming.setResourceLimit(999L);

        incoming.setResourceOwnerOrgUnitId("NEW_OU_ID");
        incoming.setResourceOwnerOrgUnitName("New Org Unit Name");

        incoming.setLicenseEnforcement("ENFORCE");
        incoming.setHasCost(true);
        incoming.setUnitCost(123L);

        incoming.setStatus("ACTIVE");
        Date statusChanged = new Date();
        incoming.setStatusChanged(statusChanged);

        incoming.setNeedApproval(true);

        Set<String> validForRoles = new HashSet<>();
        validForRoles.add("ROLE_A");
        validForRoles.add("ROLE_B");
        incoming.setValidForRoles(validForRoles);

        incoming.setApplicationCategory(Set.of("NEW_CATEGORY"));
        incoming.setResourceName("New Resource Name");
        incoming.setResourceType("NEW_TYPE");

        // locations in incoming
        ApplicationResourceLocation newLocation1 = new ApplicationResourceLocation();
        newLocation1.setOrgUnitId("OU_1");
        newLocation1.setResourceId(resourceId);
        newLocation1.setResourceLimit(50L);
        newLocation1.setResourceName("ResName OU_1");
        newLocation1.setOrgUnitName("Org Unit 1");

        ApplicationResourceLocation newLocation2 = new ApplicationResourceLocation();
        newLocation2.setOrgUnitId("OU_2");
        newLocation2.setResourceId(resourceId);
        newLocation2.setResourceLimit(75L);
        newLocation2.setResourceName("ResName OU_2");
        newLocation2.setOrgUnitName("Org Unit 2");

        Set<ApplicationResourceLocation> newLocations = new HashSet<>();
        newLocations.add(newLocation1);
        newLocations.add(newLocation2);
        incoming.getValidForOrgUnits().addAll(newLocations);

        // ---------- EXISTING (in DB) ----------
        ApplicationResource existing = getApplicationResource(resourceId);

        // existing locations – one should get updated, one removed
        ApplicationResourceLocation existingLocToUpdate = new ApplicationResourceLocation();
        existingLocToUpdate.setOrgUnitId("OU_1");
        existingLocToUpdate.setResourceId(resourceId);
        existingLocToUpdate.setResourceLimit(5L);
        existingLocToUpdate.setResourceName("Old ResName OU_1");
        existingLocToUpdate.setOrgUnitName("Old Org Unit 1");
        existingLocToUpdate.setApplicationResource(existing);

        ApplicationResourceLocation existingLocToRemove = new ApplicationResourceLocation();
        existingLocToRemove.setOrgUnitId("OU_OLD");
        existingLocToRemove.setResourceId(resourceId);
        existingLocToRemove.setResourceLimit(10L);
        existingLocToRemove.setResourceName("Old ResName OU_OLD");
        existingLocToRemove.setOrgUnitName("Old Org Unit OLD");
        existingLocToRemove.setApplicationResource(existing);

        Set<ApplicationResourceLocation> existingLocations = new HashSet<>();
        existingLocations.add(existingLocToUpdate);
        existingLocations.add(existingLocToRemove);
        existing.getValidForOrgUnits().addAll(existingLocations);

        // repository returns existing when searching by resourceId
        when(applicationResourceRepository
                .findApplicationResourceByResourceIdEqualsIgnoreCase(resourceId))
                .thenReturn(Optional.of(existing));

        // Azure group found in cache for existing.getId()
        AzureGroup azureGroup = new AzureGroup();
        UUID azureId = UUID.randomUUID();
        azureGroup.setId(azureId);
        azureGroup.setDisplayName("Azure Group Name");
        when(azureGroupCache.getOptional(10L))
                .thenReturn(Optional.of(azureGroup));

        // save returns same instance for convenience
        when(applicationResourceRepository.save(any(ApplicationResource.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // ---------- WHEN ----------
        applicationResourceService.save(incoming);

        // ---------- THEN ----------
        verify(applicationResourceRepository).save(appResourceCaptor.capture());
        ApplicationResource saved = appResourceCaptor.getValue();

        // All fields from mapApplicationResource:

        // 1) applicationAccessType / Role / Platform / AccessType
        assertEquals("NEW_ACCESS_TYPE", saved.getApplicationAccessType());
        assertEquals("NEW_ROLE", saved.getApplicationAccessRole());
        assertEquals(Set.of("NEW_PLATFORM"), saved.getPlatform());
        assertEquals("NEW_ACCESS_TYPE", saved.getAccessType());

        // 2) resourceLimit
        assertEquals(999, saved.getResourceLimit());

        // 3) resource owner org unit id / name
        assertEquals("NEW_OU_ID", saved.getResourceOwnerOrgUnitId());
        assertEquals("New Org Unit Name", saved.getResourceOwnerOrgUnitName());

        // 4) licenseEnforcement / hasCost / unitCost
        assertEquals("ENFORCE", saved.getLicenseEnforcement());
        assertTrue(saved.isHasCost());
        assertEquals(123L, saved.getUnitCost());
        // 5) status + statusChanged
        assertEquals("ACTIVE", saved.getStatus());
        assertEquals(statusChanged, saved.getStatusChanged());

        // 6) needApproval
        assertTrue(saved.isNeedApproval());

        // 7) validForRoles
        assertNotNull(saved.getValidForRoles());
        assertEquals(2, saved.getValidForRoles().size());
        assertTrue(saved.getValidForRoles().contains("ROLE_A"));
        assertTrue(saved.getValidForRoles().contains("ROLE_B"));

        // 8) applicationCategory / resourceName / resourceType
        assertEquals(Set.of("NEW_CATEGORY"), saved.getApplicationCategory());
        assertEquals("New Resource Name", saved.getResourceName());
        assertEquals("NEW_TYPE", saved.getResourceType());

        // 9) Azure group fields
        assertEquals(azureId, saved.getIdentityProviderGroupObjectId());
        assertEquals("Azure Group Name", saved.getIdentityProviderGroupName());

        // 10) Locations: should now contain OU_1 (updated) and OU_2 (new), OU_OLD removed
        assertNotNull(saved.getValidForOrgUnits());
        assertEquals(2, saved.getValidForOrgUnits().size());

        Map<String, ApplicationResourceLocation> savedByOuId = new HashMap<>();
        for (ApplicationResourceLocation l : saved.getValidForOrgUnits()) {
            savedByOuId.put(l.getOrgUnitId(), l);
        }

        assertTrue(savedByOuId.containsKey("OU_1"));
        assertTrue(savedByOuId.containsKey("OU_2"));
        assertFalse(savedByOuId.containsKey("OU_OLD"));

        ApplicationResourceLocation savedOu1 = savedByOuId.get("OU_1");
        assertEquals("OU_1", savedOu1.getOrgUnitId());
        assertEquals(resourceId, savedOu1.getResourceId());
        assertEquals(50, savedOu1.getResourceLimit());
        assertEquals("ResName OU_1", savedOu1.getResourceName());
        assertEquals("Org Unit 1", savedOu1.getOrgUnitName());
        assertSame(saved, savedOu1.getApplicationResource());

        ApplicationResourceLocation savedOu2 = savedByOuId.get("OU_2");
        assertEquals("OU_2", savedOu2.getOrgUnitId());
        assertEquals(resourceId, savedOu2.getResourceId());
        assertEquals(75, savedOu2.getResourceLimit());
        assertEquals("ResName OU_2", savedOu2.getResourceName());
        assertEquals("Org Unit 2", savedOu2.getOrgUnitName());
        assertSame(saved, savedOu2.getApplicationResource());
    }

    private static @NotNull ApplicationResource getApplicationResource(String resourceId) {
        ApplicationResource existing = new ApplicationResource();
        existing.setId(10L);
        existing.setResourceId(resourceId);

        existing.setApplicationAccessType("OLD_ACCESS_TYPE");
        existing.setApplicationAccessRole("OLD_ROLE");
        existing.setPlatform(Set.of("OLD_PLATFORM"));
        existing.setAccessType("OLD_ACCESS_TYPE");
        existing.setResourceLimit(111L);

        existing.setResourceOwnerOrgUnitId("OLD_OU_ID");
        existing.setResourceOwnerOrgUnitName("Old Org Unit Name");

        existing.setLicenseEnforcement("false");
        existing.setHasCost(false);
        existing.setUnitCost(1L);

        existing.setStatus("INACTIVE");
        existing.setStatusChanged(new Date(0L));

        existing.setNeedApproval(false);

        Set<String> oldValidRoles = new HashSet<>();
        oldValidRoles.add("ROLE_OLD");
        existing.setValidForRoles(oldValidRoles);

        existing.setApplicationCategory(Set.of("OLD_CATEGORY"));
        existing.setResourceName("Old Resource Name");
        existing.setResourceType("OLD_TYPE");
        return existing;
    }
    @Test
    void shouldUpdateExistingApplicationResourceWithoutAzureGroup() {
        String resourceId = "APP-NO-AZURE";

        // incoming
        ApplicationResource incoming = new ApplicationResource();
        incoming.setId(20L);
        incoming.setResourceId(resourceId);
        incoming.setApplicationAccessType("NEW_TYPE");

        // existing in DB, with some identity provider values already set
        UUID testUUID = UUID.randomUUID();
        ApplicationResource existing = new ApplicationResource();
        existing.setId(20L);
        existing.setResourceId(resourceId);
        existing.setApplicationAccessType("OLD_TYPE");
        existing.setIdentityProviderGroupObjectId(testUUID);
        existing.setIdentityProviderGroupName("OLD_NAME");

        when(applicationResourceRepository
                .findApplicationResourceByResourceIdEqualsIgnoreCase(resourceId))
                .thenReturn(Optional.of(existing));

        // Azure group cache returns empty → identity fields must stay as is
        when(azureGroupCache.getOptional(20L)).thenReturn(Optional.empty());

        when(applicationResourceRepository.save(any(ApplicationResource.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        applicationResourceService.save(incoming);

        // then
        verify(applicationResourceRepository).save(appResourceCaptor.capture());
        ApplicationResource saved = appResourceCaptor.getValue();

        // applicationAccessType should be updated
        assertEquals("NEW_TYPE", saved.getApplicationAccessType());

        // identity provider fields must remain unchanged
        assertEquals(testUUID, saved.getIdentityProviderGroupObjectId());
        assertEquals("OLD_NAME", saved.getIdentityProviderGroupName());
    }
}






