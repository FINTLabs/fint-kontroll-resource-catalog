package no.fintlabs.applicationResource;

import no.fintlabs.applicationResourceLocation.ApplicationResourceLocation;
import no.fintlabs.authorization.AuthorizationUtil;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApplicationResourceServiceTest {
    private ApplicationResourceService applicationResourceService;
    private ApplicationResourceRepository applicationResourceRepository;
    private AuthorizationUtil authorizationUtil;

    @BeforeEach
    public void setup(){
        applicationResourceRepository = mock(ApplicationResourceRepository.class);
        authorizationUtil = mock(AuthorizationUtil.class);
        applicationResourceService = new ApplicationResourceService(applicationResourceRepository,
                null,
                authorizationUtil);
    }

    @Test
    public void getApplicationResourceByIdShouldReturnDTOIfAuthorized(){
        ApplicationResource appRes1 = new ApplicationResource();
        appRes1.setResourceId("adobek12");
        ApplicationResourceLocation applicationResourceLocation1 = ApplicationResourceLocation
                .builder()
                .resourceId("adobek12")
                .orgunitId("1")
                .orgUnitName("VGMIDT Midtbyen videregående skole")
                .resourceLimit(100L)
                .build();
        ApplicationResourceLocation applicationResourceLocation2 = ApplicationResourceLocation
                .builder()
                .resourceId("adobek12")
                .orgunitId("2")
                .orgUnitName("VGSTOR Storskog videregående skole")
                .resourceLimit(200L)
                .build();
        List<ApplicationResourceLocation> locationsAppRes1 = new ArrayList<>();
        locationsAppRes1.add(applicationResourceLocation1);
        locationsAppRes1.add(applicationResourceLocation2);
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
    }


    @Test
    public void getApplicationResourceByIdShouldReturnEmptyDTOIfNOTAuthorized(){
        ApplicationResource appRes1 = new ApplicationResource();
        appRes1.setResourceId("adobek12");
        ApplicationResourceLocation applicationResourceLocation1 = ApplicationResourceLocation
                .builder()
                .resourceId("adobek12")
                .orgunitId("1")
                .orgUnitName("VGMIDT Midtbyen videregående skole")
                .resourceLimit(100L)
                .build();
        ApplicationResourceLocation applicationResourceLocation2 = ApplicationResourceLocation
                .builder()
                .resourceId("adobek12")
                .orgunitId("2")
                .orgUnitName("VGSTOR Storskog videregående skole")
                .resourceLimit(200L)
                .build();
        List<ApplicationResourceLocation> locationsAppRes1 = new ArrayList<>();
        locationsAppRes1.add(applicationResourceLocation1);
        locationsAppRes1.add(applicationResourceLocation2);
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
}




