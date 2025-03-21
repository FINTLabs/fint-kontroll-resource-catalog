package no.fintlabs.applicationResource;

import no.fintlabs.ResponseFactory;
import no.fintlabs.applicationResourceLocation.ApplicationResourceLocation;
import no.fintlabs.applicationResourceLocation.ApplicationResourceLocationRepository;
import no.fintlabs.authorization.AuthorizationUtil;
import no.fintlabs.cache.FintCache;
import no.fintlabs.opa.OpaService;
import no.fintlabs.kodeverk.handhevingstype.Handhevingstype;
import no.fintlabs.kodeverk.handhevingstype.HandhevingstypeLabels;
import no.fintlabs.resourceGroup.AzureGroup;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApplicationResourceServiceTest {
    private ApplicationResourceService applicationResourceService;
    private ApplicationResourceRepository applicationResourceRepository;
    private ApplicationResourceLocationRepository applicationResourceLocationRepository;
    @Mock
    private FintCache<Long, AzureGroup> azureGroupCache;
    @Mock
    private OpaService opaService;
    private AuthorizationUtil authorizationUtil;
    private ResponseFactory responseFactory;

    @BeforeEach
    public void setup(){
        applicationResourceRepository = mock(ApplicationResourceRepository.class);
        authorizationUtil = mock(AuthorizationUtil.class);
        applicationResourceService = new ApplicationResourceService(
                applicationResourceRepository,
                applicationResourceLocationRepository,
                azureGroupCache,
                authorizationUtil,
                responseFactory,
                opaService)
        ;
    }

    @Test
    public void getApplicationResourceByIdShouldReturnDTOIfAuthorized(){
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
        appRes1.setValidForOrgUnits(locationsAppRes1);

        FintJwtEndUserPrincipal fintJwtEndUserPrincipal = new FintJwtEndUserPrincipal();
        fintJwtEndUserPrincipal.setMail("titten@tei.no");
        when(authorizationUtil.getAllAuthorizedOrgUnitIDs()).thenReturn(List.of("1","2","3"));
        when(applicationResourceRepository.findById(1L)).thenReturn(Optional.of(appRes1));

        ApplicationResourceDTOFrontendDetail applicationResourceDTOFrontendDetail = applicationResourceService
                .getApplicationResourceDTOFrontendDetailById(fintJwtEndUserPrincipal,1L);

        String resourceIdDTO = applicationResourceDTOFrontendDetail.getResourceId();
        String resourceIdAppres = appRes1.getResourceId();

        assertEquals(resourceIdDTO,resourceIdAppres,"resourceIdDTO should be adobek12");
        System.out.println("resourceId should be adobek12: " + resourceIdDTO);

        verify(applicationResourceRepository, times(1)).findById(1L);
    }

    @Test
    public void getApplicationResourceByIdShouldReturnEmptyDTOIfNOTAuthorized(){
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
        appRes1.setValidForOrgUnits(locationsAppRes1);

        FintJwtEndUserPrincipal fintJwtEndUserPrincipal = new FintJwtEndUserPrincipal();
        fintJwtEndUserPrincipal.setMail("titten@tei.no");
        when(authorizationUtil.getAllAuthorizedOrgUnitIDs()).thenReturn(List.of("4","5","6"));
        when(applicationResourceRepository.findById(1L)).thenReturn(Optional.of(appRes1));

        ApplicationResourceDTOFrontendDetail applicationResourceDTOFrontendDetail = applicationResourceService
                .getApplicationResourceDTOFrontendDetailById(fintJwtEndUserPrincipal,1L);

        String resourceIdDTO = applicationResourceDTOFrontendDetail.getResourceId();
        String resourceIdAppres = appRes1.getResourceId();

        assertNotEquals(resourceIdDTO,resourceIdAppres,"resourceIdDTO should be null");
        System.out.println("resourceId should be null : " + resourceIdDTO);
    }

    @Test
    public void getApplicationResourceByIdShouldReturnDTOIfResourceIsUnRestricted(){
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
        appRes1.setValidForOrgUnits(locationsAppRes1);

        FintJwtEndUserPrincipal fintJwtEndUserPrincipal = new FintJwtEndUserPrincipal();
        fintJwtEndUserPrincipal.setMail("titten@tei.no");
        when(authorizationUtil.getAllAuthorizedOrgUnitIDs()).thenReturn(List.of("4","5","6"));
        when(applicationResourceRepository.findById(1L)).thenReturn(Optional.of(appRes1));

        ApplicationResourceDTOFrontendDetail applicationResourceDTOFrontendDetail = applicationResourceService
                .getApplicationResourceDTOFrontendDetailById(fintJwtEndUserPrincipal,1L);

        String resourceIdDTO = applicationResourceDTOFrontendDetail.getResourceId();
        String resourceIdAppres = appRes1.getResourceId();

        assertEquals(resourceIdDTO,resourceIdAppres,"resourceIdDTO should equal");
    }

    @Test
    public void getApplicationResourceByIdShouldReturnDTOIfAuthorizedForResourceOwnerOrgUnitId(){
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
        appRes1.setValidForOrgUnits(locationsAppRes1);

        FintJwtEndUserPrincipal fintJwtEndUserPrincipal = new FintJwtEndUserPrincipal();
        fintJwtEndUserPrincipal.setMail("titten@tei.no");
        when(authorizationUtil.getAllAuthorizedOrgUnitIDs()).thenReturn(List.of("3","4","5","6"));
        when(applicationResourceRepository.findById(1L)).thenReturn(Optional.of(appRes1));

        ApplicationResourceDTOFrontendDetail applicationResourceDTOFrontendDetail = applicationResourceService
                .getApplicationResourceDTOFrontendDetailById(fintJwtEndUserPrincipal,1L);

        String resourceIdDTO = applicationResourceDTOFrontendDetail.getResourceId();
        String resourceIdAppres = appRes1.getResourceId();

        assertEquals(resourceIdDTO,resourceIdAppres,"resourceIdDTO should be adobek12");
        System.out.println("resourceId should be: " + resourceIdDTO);
    }
}




