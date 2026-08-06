package no.fintlabs.applicationResource;

import no.fintlabs.applicationResourceLocation.ApplicationResourceLocation;
import no.fintlabs.applicationResourceLocation.ApplicationResourceLocationRepository;
import no.fintlabs.authorization.AuthorizationUtil;
import no.fintlabs.cache.FintCache;
import no.fintlabs.kodeverk.applikasjonskategori.Applikasjonskategori;
import no.fintlabs.kodeverk.applikasjonskategori.ApplikasjonskategoriService;
import no.fintlabs.opa.OpaService;
import no.fintlabs.kodeverk.handhevingstype.HandhevingstypeLabels;
import no.fintlabs.resourceGroup.EntraGroup;
import no.fintlabs.resourceGroup.EntraStatus;
import no.fintlabs.resourceGroup.ResourceGroupProducerService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationResourceServiceTest {

    @Mock
    private ApplicationResourceRepository applicationResourceRepository;

    @Mock
    private ApplicationResourceLocationRepository applicationResourceLocationRepository;

    @Mock
    private FintCache<Long, EntraGroup> entraGroupCache;

    @Mock
    private AuthorizationUtil authorizationUtil;

    @Mock
    private OpaService opaService;

    @Mock
    private ResourceGroupProducerService resourceGroupProducerService;

    @Mock
    private ApplikasjonskategoriService applikasjonskategoriService;

    private ApplicationResourceService applicationResourceService;

    @Captor
    private ArgumentCaptor<ApplicationResource> appResourceCaptor;

    @BeforeEach
    void setup() {
        applicationResourceService = new ApplicationResourceService(
                applicationResourceRepository,
                applicationResourceLocationRepository,
                entraGroupCache,
                authorizationUtil,
                opaService,
                resourceGroupProducerService,
                applikasjonskategoriService
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
        newResource.setStatus("ACTIVE");

        when(applicationResourceRepository
                .findApplicationResourceByResourceIdEqualsIgnoreCase(resourceId))
                .thenReturn(Optional.empty());
        when(applicationResourceRepository.save(any(ApplicationResource.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        applicationResourceService.save(newResource);

        verify(applicationResourceRepository).save(newResource);
        assertEquals("PENDING_ACTIVE", newResource.getStatus());
        verify(resourceGroupProducerService).publish(newResource, true);
        verify(entraGroupCache, never()).getOptional(anyLong());
    }

    @Test
    void shouldAcceptInactiveStatusFromKafkaUpdate() {
        String resourceId = "APP-KAFKA-INACTIVE";

        ApplicationResource existing = new ApplicationResource();
        existing.setId(11L);
        existing.setResourceId(resourceId);
        existing.setStatus("ACTIVE");

        ApplicationResource incoming = new ApplicationResource();
        incoming.setResourceId(resourceId);
        incoming.setStatus("INACTIVE");

        when(applicationResourceRepository.findApplicationResourceByResourceIdEqualsIgnoreCase(resourceId))
                .thenReturn(Optional.of(existing));
        when(applicationResourceRepository.save(any(ApplicationResource.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        applicationResourceService.save(incoming);

        verify(applicationResourceRepository).save(appResourceCaptor.capture());
        ApplicationResource saved = appResourceCaptor.getValue();
        assertEquals("INACTIVE", saved.getStatus());
        verify(resourceGroupProducerService).publish(saved, false);
    }

    @Test
    void shouldCreateApplicationResourceAndPublishResourceGroupCommand() {
        ApplicationResource newResource = new ApplicationResource();
        newResource.setId(1L);
        newResource.setResourceId("APP-1");
        newResource.setResourceName("My New App");
        newResource.setStatus("ACTIVE");

        when(applicationResourceRepository.saveAndFlush(newResource))
                .thenReturn(newResource);

        ApplicationResource saved = applicationResourceService.createApplicationResource(newResource);

        assertEquals("PENDING_ACTIVE", saved.getStatus());
        verify(applicationResourceRepository).saveAndFlush(newResource);
        verify(resourceGroupProducerService).publish(newResource, true);
    }

    @Test
    void shouldKeepDisabledStatusWhenCreatingApplicationResource() {
        ApplicationResource newResource = new ApplicationResource();
        newResource.setId(2L);
        newResource.setResourceId("APP-2");
        newResource.setResourceName("My Disabled App");
        newResource.setStatus("DISABLED");

        when(applicationResourceRepository.saveAndFlush(newResource))
                .thenReturn(newResource);

        ApplicationResource saved = applicationResourceService.createApplicationResource(newResource);

        assertEquals("DISABLED", saved.getStatus());
        verify(applicationResourceRepository).saveAndFlush(newResource);
        verify(resourceGroupProducerService).publish(newResource, true);
    }

    @Test
    void shouldKeepPendingDisabledStatusWhenCreatingApplicationResource() {
        ApplicationResource newResource = new ApplicationResource();
        newResource.setId(21L);
        newResource.setResourceId("APP-21");
        newResource.setStatus("PENDING_DISABLED");

        when(applicationResourceRepository.saveAndFlush(newResource))
                .thenReturn(newResource);

        ApplicationResource saved = applicationResourceService.createApplicationResource(newResource);

        assertEquals("PENDING_DISABLED", saved.getStatus());
        verify(applicationResourceRepository).saveAndFlush(newResource);
        verify(resourceGroupProducerService).publish(newResource, true);
    }

    @Test
    void shouldKeepActiveStatusWhenUiUpdateDoesNotChangeStatus() {
        Long applicationResourceId = 3L;
        String resourceId = "APP-3";
        Date originalStatusChanged = new Date(1000L);

        ApplicationResource existing = new ApplicationResource();
        existing.setId(applicationResourceId);
        existing.setResourceId(resourceId);
        existing.setResourceName("Old name");
        existing.setStatus("ACTIVE");
        existing.setStatusChanged(originalStatusChanged);
        existing.setIdentityProviderGroupObjectId(UUID.randomUUID());

        ApplicationResource incoming = new ApplicationResource();
        incoming.setId(applicationResourceId);
        incoming.setResourceId(resourceId);
        incoming.setResourceName("New name");
        incoming.setStatus("ACTIVE");
        incoming.setStatusChanged(new Date(2000L));

        when(applicationResourceRepository.findById(applicationResourceId))
                .thenReturn(Optional.of(existing));
        when(applicationResourceRepository.findApplicationResourceByResourceIdEqualsIgnoreCase(resourceId))
                .thenReturn(Optional.of(existing));
        when(applicationResourceRepository.save(any(ApplicationResource.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationResource updated = applicationResourceService.updateApplicationResource(incoming);

        verify(applicationResourceRepository).save(appResourceCaptor.capture());
        ApplicationResource saved = appResourceCaptor.getValue();
        assertEquals("New name", saved.getResourceName());
        assertEquals("ACTIVE", saved.getStatus());
        assertEquals(originalStatusChanged, saved.getStatusChanged());
        assertEquals("ACTIVE", updated.getStatus());
        verify(resourceGroupProducerService).publish(saved, true);
    }

    @Test
    void shouldSetDisabledWhenUiUpdateChangesStatusFromActiveToDisabled() {
        Long applicationResourceId = 4L;
        String resourceId = "APP-4";

        ApplicationResource existing = new ApplicationResource();
        existing.setId(applicationResourceId);
        existing.setResourceId(resourceId);
        existing.setStatus("ACTIVE");
        existing.setStatusChanged(new Date(1000L));

        ApplicationResource incoming = new ApplicationResource();
        incoming.setId(applicationResourceId);
        incoming.setResourceId(resourceId);
        incoming.setStatus("DISABLED");

        when(applicationResourceRepository.findById(applicationResourceId))
                .thenReturn(Optional.of(existing));
        when(applicationResourceRepository.findApplicationResourceByResourceIdEqualsIgnoreCase(resourceId))
                .thenReturn(Optional.of(existing));
        when(applicationResourceRepository.save(any(ApplicationResource.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        applicationResourceService.updateApplicationResource(incoming);

        verify(applicationResourceRepository).save(appResourceCaptor.capture());
        ApplicationResource saved = appResourceCaptor.getValue();
        assertEquals("DISABLED", saved.getStatus());
        assertNotNull(saved.getStatusChanged());
        assertNotEquals(new Date(1000L), saved.getStatusChanged());
        verify(resourceGroupProducerService).publish(saved, false);
    }

    @Test
    void shouldAcceptInactiveStatusFromUiUpdate() {
        Long applicationResourceId = 41L;

        ApplicationResource existing = new ApplicationResource();
        existing.setId(applicationResourceId);
        existing.setResourceId("APP-41");
        existing.setStatus("ACTIVE");
        existing.setStatusChanged(new Date(1000L));

        ApplicationResource incoming = new ApplicationResource();
        incoming.setId(applicationResourceId);
        incoming.setResourceId("APP-41");
        incoming.setStatus("INACTIVE");

        when(applicationResourceRepository.findById(applicationResourceId))
                .thenReturn(Optional.of(existing));
        when(applicationResourceRepository.findApplicationResourceByResourceIdEqualsIgnoreCase("APP-41"))
                .thenReturn(Optional.of(existing));
        when(applicationResourceRepository.save(any(ApplicationResource.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        applicationResourceService.updateApplicationResource(incoming);

        verify(applicationResourceRepository).save(appResourceCaptor.capture());
        ApplicationResource saved = appResourceCaptor.getValue();
        assertEquals("INACTIVE", saved.getStatus());
        verify(resourceGroupProducerService).publish(saved, false);
    }

    @Test
    void shouldSetPendingActiveWhenUiUpdateChangesDisabledResourceWithoutEntraGroupToActive() {
        Long applicationResourceId = 42L;
        String resourceId = "APP-42";

        ApplicationResource existing = new ApplicationResource();
        existing.setId(applicationResourceId);
        existing.setResourceId(resourceId);
        existing.setStatus("DISABLED");
        existing.setStatusChanged(new Date(1000L));

        ApplicationResource incoming = new ApplicationResource();
        incoming.setId(applicationResourceId);
        incoming.setResourceId(resourceId);
        incoming.setStatus("ACTIVE");

        when(applicationResourceRepository.findById(applicationResourceId))
                .thenReturn(Optional.of(existing));
        when(applicationResourceRepository.findApplicationResourceByResourceIdEqualsIgnoreCase(resourceId))
                .thenReturn(Optional.of(existing));
        when(applicationResourceRepository.save(any(ApplicationResource.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        applicationResourceService.updateApplicationResource(incoming);

        verify(applicationResourceRepository).save(appResourceCaptor.capture());
        ApplicationResource saved = appResourceCaptor.getValue();
        assertEquals("PENDING_ACTIVE", saved.getStatus());
        assertNotNull(saved.getStatusChanged());
        assertNotEquals(new Date(1000L), saved.getStatusChanged());
        verify(resourceGroupProducerService).publish(saved, true);
    }

    @Test
    void shouldPublishOnlyResourceGroupEntityWhenUiUpdateChangesStatusToDeleted() {
        Long applicationResourceId = 5L;
        String resourceId = "APP-5";
        UUID idpGroupObjectId = UUID.randomUUID();

        ApplicationResource existing = new ApplicationResource();
        existing.setId(applicationResourceId);
        existing.setResourceId(resourceId);
        existing.setResourceName("Resource name");
        existing.setStatus("ACTIVE");
        existing.setIdentityProviderGroupObjectId(idpGroupObjectId);

        ApplicationResource incoming = new ApplicationResource();
        incoming.setId(applicationResourceId);
        incoming.setResourceId(resourceId);
        incoming.setResourceName("Resource name");
        incoming.setStatus("DELETED");

        when(applicationResourceRepository.findById(applicationResourceId))
                .thenReturn(Optional.of(existing));
        when(applicationResourceRepository.findApplicationResourceByResourceIdEqualsIgnoreCase(resourceId))
                .thenReturn(Optional.of(existing));
        when(applicationResourceRepository.save(any(ApplicationResource.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        applicationResourceService.updateApplicationResource(incoming);

        verify(applicationResourceRepository).save(appResourceCaptor.capture());
        ApplicationResource saved = appResourceCaptor.getValue();
        assertEquals("DELETED", saved.getStatus());
        verify(resourceGroupProducerService).publish(saved, false);
    }

    @Test
    void shouldSetPendingActiveAndClearEntraGroupWhenUiUpdateReactivatesDeletedResource() {
        Long applicationResourceId = 51L;
        String resourceId = "APP-51";
        UUID staleIdpGroupObjectId = UUID.randomUUID();

        ApplicationResource existing = new ApplicationResource();
        existing.setId(applicationResourceId);
        existing.setResourceId(resourceId);
        existing.setResourceName("Resource name");
        existing.setStatus("DELETED");
        existing.setIdentityProviderGroupObjectId(staleIdpGroupObjectId);
        existing.setIdentityProviderGroupName("Old Entra group");

        ApplicationResource incoming = new ApplicationResource();
        incoming.setId(applicationResourceId);
        incoming.setResourceId(resourceId);
        incoming.setResourceName("Resource name");
        incoming.setStatus("ACTIVE");

        when(applicationResourceRepository.findById(applicationResourceId))
                .thenReturn(Optional.of(existing));
        when(applicationResourceRepository.findApplicationResourceByResourceIdEqualsIgnoreCase(resourceId))
                .thenReturn(Optional.of(existing));
        when(applicationResourceRepository.save(any(ApplicationResource.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        applicationResourceService.updateApplicationResource(incoming);

        verify(applicationResourceRepository).save(appResourceCaptor.capture());
        ApplicationResource saved = appResourceCaptor.getValue();
        assertEquals("PENDING_ACTIVE", saved.getStatus());
        assertNull(saved.getIdentityProviderGroupObjectId());
        assertNull(saved.getIdentityProviderGroupName());
        assertNotNull(saved.getStatusChanged());
        verify(resourceGroupProducerService).publish(saved, true);
    }

    @Test
    void shouldSetStatusDeletedAndPublishOnlyResourceGroupEntityWhenDeletingApplicationResource() {
        Long applicationResourceId = 6L;
        Date originalStatusChanged = new Date(1000L);

        ApplicationResource existing = new ApplicationResource();
        existing.setId(applicationResourceId);
        existing.setResourceId("APP-6");
        existing.setStatus("ACTIVE");
        existing.setStatusChanged(originalStatusChanged);

        when(applicationResourceRepository.findById(applicationResourceId))
                .thenReturn(Optional.of(existing));
        when(applicationResourceRepository.saveAndFlush(any(ApplicationResource.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        applicationResourceService.deleteApplicationResource(applicationResourceId);

        verify(applicationResourceRepository).saveAndFlush(appResourceCaptor.capture());
        ApplicationResource saved = appResourceCaptor.getValue();
        assertEquals("DELETED", saved.getStatus());
        assertNotNull(saved.getStatusChanged());
        assertNotEquals(originalStatusChanged, saved.getStatusChanged());
        verify(resourceGroupProducerService).publish(saved, false);
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

        Applikasjonskategori newCategory = Applikasjonskategori.builder()
                .id(1L)
                .name("New category")
                .build();
        incoming.setApplicationCategory(Set.of(newCategory));
        when(applikasjonskategoriService.getOrCreateApplikasjonskategoriByNames(List.of("New category")))
                .thenReturn(Set.of(newCategory));
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

        // Existing already has a linked Entra group from a prior graph-group response.
        // This update must not touch it - only saveEntraGroup() is allowed to.
        UUID existingEntraId = UUID.randomUUID();
        existing.setIdentityProviderGroupObjectId(existingEntraId);
        existing.setIdentityProviderGroupName("Existing Entra Group Name");

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
        assertEquals(Set.of(newCategory), saved.getApplicationCategory());
        assertEquals("New Resource Name", saved.getResourceName());
        assertEquals("NEW_TYPE", saved.getResourceType());

        // 9) Entra group fields: untouched by this update path, preserved from existing
        assertEquals(existingEntraId, saved.getIdentityProviderGroupObjectId());
        assertEquals("Existing Entra Group Name", saved.getIdentityProviderGroupName());
        verify(entraGroupCache, never()).getOptional(anyLong());

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

        existing.setApplicationCategory(Set.of(Applikasjonskategori.builder()
                .id(2L)
                .name("Old category")
                .build()));
        existing.setResourceName("Old Resource Name");
        existing.setResourceType("OLD_TYPE");
        return existing;
    }
    @Test
    void shouldUpdateExistingApplicationResourceWithoutEntraGroup() {
        String resourceId = "APP-NO-ENTRA";

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

        when(applicationResourceRepository.save(any(ApplicationResource.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        applicationResourceService.save(incoming);

        // then
        verify(applicationResourceRepository).save(appResourceCaptor.capture());
        ApplicationResource saved = appResourceCaptor.getValue();

        // applicationAccessType should be updated
        assertEquals("NEW_TYPE", saved.getApplicationAccessType());

        // identity provider fields must remain unchanged, and this path never consults the cache
        assertEquals(testUUID, saved.getIdentityProviderGroupObjectId());
        assertEquals("OLD_NAME", saved.getIdentityProviderGroupName());
        verify(entraGroupCache, never()).getOptional(anyLong());
    }

    @Test
    void shouldSaveEntraGroupForExistingApplicationResource() {
        Long applicationResourceId = 30L;
        String resourceId = "APP-WITH-ENTRA-GROUP";
        UUID entraGroupId = UUID.randomUUID();

        ApplicationResource applicationResource = new ApplicationResource();
        applicationResource.setId(applicationResourceId);
        applicationResource.setResourceId(resourceId);
        applicationResource.setStatus("PENDING_ACTIVE");

        EntraGroup entraGroup = new EntraGroup();
        entraGroup.setObjectId(entraGroupId.toString());
        entraGroup.setDisplayName("Entra Group Name");
        entraGroup.setResourceGroupId(applicationResourceId);
        entraGroup.setStatus(EntraStatus.CREATED);

        when(applicationResourceRepository.findById(applicationResourceId))
                .thenReturn(Optional.of(applicationResource));
        when(applicationResourceRepository.save(any(ApplicationResource.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        applicationResourceService.saveEntraGroup(entraGroup);

        verify(applicationResourceRepository).save(appResourceCaptor.capture());
        ApplicationResource saved = appResourceCaptor.getValue();
        assertEquals(entraGroupId, saved.getIdentityProviderGroupObjectId());
        assertEquals("Entra Group Name", saved.getIdentityProviderGroupName());
        assertEquals("CREATED", saved.getEntraState());
        assertEquals("ACTIVE", saved.getStatus());
        assertNotNull(saved.getStatusChanged());
        verify(entraGroupCache).put(applicationResourceId, entraGroup);
        verify(resourceGroupProducerService, never()).publish(any(ApplicationResource.class));
    }

    @Test
    void shouldSaveEntraGroupWithoutResourceGroupIdUsingResolvedApplicationResourceIdAsCacheKey() {
        Long applicationResourceId = 32L;
        UUID entraGroupId = UUID.randomUUID();

        ApplicationResource applicationResource = new ApplicationResource();
        applicationResource.setId(applicationResourceId);
        applicationResource.setIdentityProviderGroupObjectId(entraGroupId);
        applicationResource.setStatus("ACTIVE");

        EntraGroup entraGroup = new EntraGroup();
        entraGroup.setObjectId(entraGroupId.toString());
        entraGroup.setDisplayName("Reconciled Entra Group Name");
        entraGroup.setStatus(EntraStatus.UPDATED);

        when(applicationResourceRepository.findApplicationResourceByIdentityProviderGroupObjectId(entraGroupId))
                .thenReturn(Optional.of(applicationResource));
        when(applicationResourceRepository.save(any(ApplicationResource.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        applicationResourceService.saveEntraGroup(entraGroup);

        verify(applicationResourceRepository).save(appResourceCaptor.capture());
        ApplicationResource saved = appResourceCaptor.getValue();
        assertEquals(entraGroupId, saved.getIdentityProviderGroupObjectId());
        assertEquals("Reconciled Entra Group Name", saved.getIdentityProviderGroupName());
        assertEquals("UPDATED", saved.getEntraState());
        assertEquals(applicationResourceId, entraGroup.getResourceGroupId());
        verify(entraGroupCache).put(applicationResourceId, entraGroup);
        verify(entraGroupCache, never()).put(org.mockito.ArgumentMatchers.<Long>isNull(), any(EntraGroup.class));
        verify(resourceGroupProducerService, never()).publish(any(ApplicationResource.class));
    }

    @Test
    void shouldKeepInactiveStatusWhenGraphGroupReturnsObjectId() {
        Long applicationResourceId = 31L;
        UUID entraGroupId = UUID.randomUUID();

        ApplicationResource applicationResource = new ApplicationResource();
        applicationResource.setId(applicationResourceId);
        applicationResource.setStatus("INACTIVE");

        EntraGroup entraGroup = new EntraGroup();
        entraGroup.setObjectId(entraGroupId.toString());
        entraGroup.setDisplayName("Entra Group Name");
        entraGroup.setResourceGroupId(applicationResourceId);
        entraGroup.setStatus(EntraStatus.CREATED);

        when(applicationResourceRepository.findById(applicationResourceId))
                .thenReturn(Optional.of(applicationResource));
        when(applicationResourceRepository.save(any(ApplicationResource.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        applicationResourceService.saveEntraGroup(entraGroup);

        verify(applicationResourceRepository).save(appResourceCaptor.capture());
        ApplicationResource saved = appResourceCaptor.getValue();
        assertEquals(entraGroupId, saved.getIdentityProviderGroupObjectId());
        assertEquals("CREATED", saved.getEntraState());
        assertEquals("INACTIVE", saved.getStatus());
        assertNull(saved.getStatusChanged());
    }

    @Test
    void shouldKeepDisabledStatusWhenGraphGroupReturnsObjectId() {
        Long applicationResourceId = 33L;
        UUID entraGroupId = UUID.randomUUID();

        ApplicationResource applicationResource = new ApplicationResource();
        applicationResource.setId(applicationResourceId);
        applicationResource.setStatus("DISABLED");

        EntraGroup entraGroup = new EntraGroup();
        entraGroup.setObjectId(entraGroupId.toString());
        entraGroup.setDisplayName("Entra Group Name");
        entraGroup.setResourceGroupId(applicationResourceId);
        entraGroup.setStatus(EntraStatus.CREATED);

        when(applicationResourceRepository.findById(applicationResourceId))
                .thenReturn(Optional.of(applicationResource));
        when(applicationResourceRepository.save(any(ApplicationResource.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        applicationResourceService.saveEntraGroup(entraGroup);

        verify(applicationResourceRepository).save(appResourceCaptor.capture());
        ApplicationResource saved = appResourceCaptor.getValue();
        assertEquals(entraGroupId, saved.getIdentityProviderGroupObjectId());
        assertEquals("CREATED", saved.getEntraState());
        assertEquals("DISABLED", saved.getStatus());
        assertNull(saved.getStatusChanged());
    }

    @Test
    void shouldIgnoreEntraGroupWhenApplicationResourceDoesNotExist() {
        Long applicationResourceId = 40L;

        EntraGroup entraGroup = new EntraGroup();
        entraGroup.setObjectId(UUID.randomUUID().toString());
        entraGroup.setDisplayName("Entra Group Name");
        entraGroup.setResourceGroupId(applicationResourceId);

        when(applicationResourceRepository.findById(applicationResourceId))
                .thenReturn(Optional.empty());

        applicationResourceService.saveEntraGroup(entraGroup);

        verify(applicationResourceRepository, never()).save(any(ApplicationResource.class));
        verify(entraGroupCache, never()).put(anyLong(), any(EntraGroup.class));
    }

    @Test
    void shouldSetEntraStateWhenEntraGroupResponseFailed() {
        Long applicationResourceId = 50L;
        ApplicationResource applicationResource = new ApplicationResource();
        applicationResource.setId(applicationResourceId);
        applicationResource.setStatus("ACTIVE");

        EntraGroup entraGroup = new EntraGroup();
        entraGroup.setResourceGroupId(applicationResourceId);
        entraGroup.setStatus(EntraStatus.FAILED);

        when(applicationResourceRepository.findById(applicationResourceId))
                .thenReturn(Optional.of(applicationResource));
        when(applicationResourceRepository.save(any(ApplicationResource.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        applicationResourceService.saveEntraGroup(entraGroup);

        verify(applicationResourceRepository).save(appResourceCaptor.capture());
        ApplicationResource saved = appResourceCaptor.getValue();
        assertEquals("ACTIVE", saved.getStatus());
        assertEquals("FAILED", saved.getEntraState());
        assertNull(saved.getStatusChanged());
        verify(entraGroupCache, never()).put(anyLong(), any(EntraGroup.class));
        verify(resourceGroupProducerService, never()).publish(any(ApplicationResource.class));
    }

    @Test
    void shouldIgnoreGraphGroupResponseWithNoChangesWhenEntraGroupMatchesCatalogState() {
        Long applicationResourceId = 51L;
        UUID entraGroupId = UUID.randomUUID();

        ApplicationResource applicationResource = new ApplicationResource();
        applicationResource.setId(applicationResourceId);
        applicationResource.setIdentityProviderGroupObjectId(entraGroupId);
        applicationResource.setIdentityProviderGroupName("Existing Entra Group Name");

        EntraGroup entraGroup = new EntraGroup();
        entraGroup.setResourceGroupId(applicationResourceId);
        entraGroup.setObjectId(entraGroupId.toString());
        entraGroup.setDisplayName("Existing Entra Group Name");
        entraGroup.setStatus(EntraStatus.NO_CHANGES);

        when(applicationResourceRepository.findById(applicationResourceId))
                .thenReturn(Optional.of(applicationResource));

        applicationResourceService.saveEntraGroup(entraGroup);

        verify(applicationResourceRepository, never()).save(any(ApplicationResource.class));
        verify(entraGroupCache, never()).put(anyLong(), any(EntraGroup.class));
        verify(resourceGroupProducerService, never()).publish(any(ApplicationResource.class));
    }

    @Test
    void shouldTreatNoChangesAsUpdatedWhenEntraGroupDiffersFromCatalogState() {
        Long applicationResourceId = 52L;
        UUID entraGroupId = UUID.randomUUID();

        ApplicationResource applicationResource = new ApplicationResource();
        applicationResource.setId(applicationResourceId);
        applicationResource.setStatus("PENDING_ACTIVE");

        EntraGroup entraGroup = new EntraGroup();
        entraGroup.setResourceGroupId(applicationResourceId);
        entraGroup.setObjectId(entraGroupId.toString());
        entraGroup.setDisplayName("Existing Entra Group Name");
        entraGroup.setStatus(EntraStatus.NO_CHANGES);

        when(applicationResourceRepository.findById(applicationResourceId))
                .thenReturn(Optional.of(applicationResource));
        when(applicationResourceRepository.save(any(ApplicationResource.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        applicationResourceService.saveEntraGroup(entraGroup);

        verify(applicationResourceRepository).save(appResourceCaptor.capture());
        ApplicationResource saved = appResourceCaptor.getValue();
        assertEquals(entraGroupId, saved.getIdentityProviderGroupObjectId());
        assertEquals("Existing Entra Group Name", saved.getIdentityProviderGroupName());
        assertEquals("UPDATED", saved.getEntraState());
        assertEquals("ACTIVE", saved.getStatus());
        assertNotNull(saved.getStatusChanged());
        verify(entraGroupCache).put(applicationResourceId, entraGroup);
        verify(resourceGroupProducerService, never()).publish(any(ApplicationResource.class));
    }

    @Test
    void shouldClearEntraGroupForDeletedGraphGroupResponse() {
        Long applicationResourceId = 60L;
        ApplicationResource applicationResource = new ApplicationResource();
        applicationResource.setId(applicationResourceId);
        applicationResource.setIdentityProviderGroupObjectId(UUID.randomUUID());
        applicationResource.setIdentityProviderGroupName("Old Entra Group Name");

        EntraGroup entraGroup = new EntraGroup();
        entraGroup.setResourceGroupId(applicationResourceId);
        entraGroup.setStatus(EntraStatus.DELETED);

        when(applicationResourceRepository.findById(applicationResourceId))
                .thenReturn(Optional.of(applicationResource));
        when(applicationResourceRepository.save(any(ApplicationResource.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        applicationResourceService.saveEntraGroup(entraGroup);

        verify(applicationResourceRepository).save(appResourceCaptor.capture());
        ApplicationResource saved = appResourceCaptor.getValue();
        assertNull(saved.getIdentityProviderGroupObjectId());
        assertNull(saved.getIdentityProviderGroupName());
        assertEquals("DELETED", saved.getEntraState());
        verify(entraGroupCache).remove(applicationResourceId);
        verify(resourceGroupProducerService, never()).publish(any(ApplicationResource.class));
    }

    @Test
    void shouldClearEntraGroupForDeletedGraphGroupResponseWithoutResourceGroupId() {
        Long applicationResourceId = 61L;
        UUID entraObjectId = UUID.randomUUID();
        ApplicationResource applicationResource = new ApplicationResource();
        applicationResource.setId(applicationResourceId);
        applicationResource.setIdentityProviderGroupObjectId(entraObjectId);
        applicationResource.setIdentityProviderGroupName("Old Entra Group Name");

        EntraGroup entraGroup = new EntraGroup();
        entraGroup.setObjectId(entraObjectId.toString());
        entraGroup.setStatus(EntraStatus.DELETED);

        when(applicationResourceRepository.findApplicationResourceByIdentityProviderGroupObjectId(entraObjectId))
                .thenReturn(Optional.of(applicationResource));
        when(applicationResourceRepository.save(any(ApplicationResource.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        applicationResourceService.saveEntraGroup(entraGroup);

        verify(applicationResourceRepository).save(appResourceCaptor.capture());
        ApplicationResource saved = appResourceCaptor.getValue();
        assertNull(saved.getIdentityProviderGroupObjectId());
        assertNull(saved.getIdentityProviderGroupName());
        assertEquals("DELETED", saved.getEntraState());
        verify(entraGroupCache).remove(applicationResourceId);
        verify(resourceGroupProducerService, never()).publish(any(ApplicationResource.class));
    }

    @Test
    void shouldIgnoreGraphGroupResponseWithInvalidObjectId() {
        Long applicationResourceId = 70L;
        ApplicationResource applicationResource = new ApplicationResource();
        applicationResource.setId(applicationResourceId);

        EntraGroup entraGroup = new EntraGroup();
        entraGroup.setObjectId("not-a-uuid");
        entraGroup.setDisplayName("Entra Group Name");
        entraGroup.setResourceGroupId(applicationResourceId);
        entraGroup.setStatus(EntraStatus.CREATED);

        when(applicationResourceRepository.findById(applicationResourceId))
                .thenReturn(Optional.of(applicationResource));

        applicationResourceService.saveEntraGroup(entraGroup);

        verify(applicationResourceRepository).save(appResourceCaptor.capture());
        ApplicationResource saved = appResourceCaptor.getValue();
        assertEquals("CREATED", saved.getEntraState());
        assertNull(saved.getIdentityProviderGroupObjectId());
        verify(entraGroupCache, never()).put(anyLong(), any(EntraGroup.class));
        verify(resourceGroupProducerService, never()).publish(any(ApplicationResource.class));
    }

    @DisplayName("Test for getOrgUnitsValidAndInScope - validOrgUnits is null")
    @Test
    public void givenNullValidOrgUnits_whenGetOrgUnitsValidAndInScope_thenReturnOrgUnitsInScope() {
        List<String> orgUnitsInScope = List.of("198", "205", "211");

        List<String> result = ApplicationResourceService.getOrgUnitsValidAndInScope(orgUnitsInScope, null);

        assertThat(result).isEqualTo(orgUnitsInScope);
    }

    @DisplayName("Test for getOrgUnitsValidAndInScope - validOrgUnits is empty")
    @Test
    public void givenEmptyValidOrgUnits_whenGetOrgUnitsValidAndInScope_thenReturnOrgUnitsInScope() {
        List<String> orgUnitsInScope = List.of("198", "205", "211");

        List<String> result = ApplicationResourceService.getOrgUnitsValidAndInScope(orgUnitsInScope, new ArrayList<>());

        assertThat(result).isEqualTo(orgUnitsInScope);
    }

    @DisplayName("Test for getOrgUnitsValidAndInScope - orgUnitsInScope contains ALLORGUNITS")
    @Test
    public void givenOrgUnitsInScopeContainsALLORGUNITS_whenGetOrgUnitsValidAndInScope_thenReturnValidOrgUnits() {
        List<String> orgUnitsInScope = List.of("ALLORGUNITS", "198", "205");
        List<String> validOrgUnits = List.of("211", "218");

        List<String> result = ApplicationResourceService.getOrgUnitsValidAndInScope(orgUnitsInScope, validOrgUnits);

        assertThat(result).isEqualTo(validOrgUnits);
    }

    @DisplayName("Test for getOrgUnitsValidAndInScope - intersection of orgUnitsInScope and validOrgUnits")
    @Test
    public void givenNonEmptyOrgUnitsInScopeAndValidOrgUnits_whenGetOrgUnitsValidAndInScope_thenReturnIntersection() {
        List<String> orgUnitsInScope = List.of("198", "205", "211", "218");
        List<String> validOrgUnits = List.of("211", "218", "219");

        List<String> result = ApplicationResourceService.getOrgUnitsValidAndInScope(orgUnitsInScope, validOrgUnits);

        assertThat(result).isEqualTo(List.of("211", "218"));
    }

    @DisplayName("Test for getOrgUnitsValidAndInScope - no intersection between orgUnitsInScope and validOrgUnits")
    @Test
    public void givenNoIntersectionBetweenOrgUnitsInScopeAndValidOrgUnits_whenGetOrgUnitsValidAndInScope_thenReturnEmptyList() {
        List<String> orgUnitsInScope = List.of("198", "205");
        List<String> validOrgUnits = List.of("211", "218");

        List<String> result = ApplicationResourceService.getOrgUnitsValidAndInScope(orgUnitsInScope, validOrgUnits);

        assertThat(result).isEqualTo(new ArrayList<>());
    }
}
