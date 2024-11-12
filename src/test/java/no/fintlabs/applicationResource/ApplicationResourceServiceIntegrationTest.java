package no.fintlabs.applicationResource;

import no.fintlabs.applicationResourceLocation.ApplicationResourceLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.domain.Specification;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@Import({ApplicationResource.class, ApplicationResourceLocation.class})
class ApplicationResourceServiceIntegrationTest extends DatabaseIntegrationTest {

    @Autowired
    private ApplicationResourceRepository applicationResourceRepository;


    @BeforeEach
    public void setUp() {
        ApplicationResourceLocation restrictedResourceLocation = ApplicationResourceLocation.builder()
                .orgUnitId("orgUnitId1")
                .build();
        ApplicationResourceLocation unrestrictedResourceLocation1 = ApplicationResourceLocation.builder()
                .orgUnitId("orgUnitId1")
                .build();
        ApplicationResourceLocation unrestrictedResourceLocation2 = ApplicationResourceLocation.builder()
                .orgUnitId("orgUnitId2")
                .build();

        ApplicationResource restrictedResource = ApplicationResource.builder()
                .resourceId("res1")
                .licenseEnforcement("HARDSTOP")
                .validForOrgUnits(List.of(restrictedResourceLocation))
                .build();

        ApplicationResource unrestrictedResource = ApplicationResource.builder()
                .resourceId("res2")
                .licenseEnforcement("FREE-ALL")
                .validForOrgUnits(List.of(unrestrictedResourceLocation1, unrestrictedResourceLocation2))
                .build();

        applicationResourceRepository.save(restrictedResource);
        applicationResourceRepository.save(unrestrictedResource);
    }
    @Test
    void allAuthorizedOrgUnitIds() {
        Specification<ApplicationResource> specification = AppicationResourceSpesificationBuilder.allAuthorizedOrgUnitIds(List.of("orgUnitId1"));

        List<ApplicationResource> resources = applicationResourceRepository.findAll(specification);
        assertEquals(1, resources.size());
        assertEquals("res2", resources.get(0).getResourceId());
    }
}