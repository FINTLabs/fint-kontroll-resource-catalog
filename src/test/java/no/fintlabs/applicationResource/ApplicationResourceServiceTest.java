package no.fintlabs.applicationResource;

import no.fintlabs.applicationResourceLocation.ApplicationResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationResourceServiceTest {

    @Test
    void init() {

        List<String> validForRolesAppRes1 = new ArrayList<>();
        validForRolesAppRes1.add("student");
        List<String> plattformAppres1 = new ArrayList<>();
        plattformAppres1.add("WIN");
        plattformAppres1.add("Linux");

        //ApplicationResource1
        ApplicationResource appRes1 = new ApplicationResource();
        appRes1.setResourceId("adobek12");
        appRes1.setResourceName("Adobe K12 Utdanning");
        appRes1.setResourceType("ApplicationResource");
        appRes1.setIdentityProviderGroupObjectId(UUID.fromString("735e619a-8905-4f68-9dab-b908076c097b"));

        appRes1.setResourceLimit(1000L);
        appRes1.setResourceOwnerOrgUnitId("6");
        appRes1.setResourceOwnerOrgUnitName("KOMP Utdanning og kompetanse");
        appRes1.setValidForRoles(validForRolesAppRes1);
        ApplicationResourceLocation applicationResourceLocation1 = ApplicationResourceLocation
                .builder()
                .resourceId("adobek12")
                .orgunitId("194")
                .orgUnitName("VGMIDT Midtbyen videregående skole")
                .resourceLimit(100L)
                .build();
        ApplicationResourceLocation applicationResourceLocation2 = ApplicationResourceLocation
                .builder()
                .resourceId("adobek12")
                .orgunitId("198")
                .orgUnitName("VGSTOR Storskog videregående skole")
                .resourceLimit(200L)
                .build();
        List<ApplicationResourceLocation> locationsAppRes1 = new ArrayList<>();
        locationsAppRes1.add(applicationResourceLocation1);
        locationsAppRes1.add(applicationResourceLocation2);
        appRes1.setValidForOrgUnits(locationsAppRes1);
        appRes1.setApplicationAccessType("ApplikasjonTilgang");
        appRes1.setApplicationAccessRole("Full access");
        appRes1.setPlatform(plattformAppres1);
        appRes1.setAccessType("device");

        System.out.println(appRes1.getIdentityProviderGroupObjectId().toString());
    }
}