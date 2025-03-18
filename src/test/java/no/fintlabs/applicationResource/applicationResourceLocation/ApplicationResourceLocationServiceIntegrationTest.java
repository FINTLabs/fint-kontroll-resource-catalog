package no.fintlabs.applicationResource.applicationResourceLocation;

import no.fintlabs.DatabaseIntegrationTest;
import no.fintlabs.applicationResource.ApplicationResource;
import no.fintlabs.applicationResource.ApplicationResourceRepository;
import no.fintlabs.applicationResource.ApplicationResourceService;
import no.fintlabs.applicationResourceLocation.ApplicationResourceLocation;
import no.fintlabs.applicationResourceLocation.ApplicationResourceLocationExtendedProduserService;
import no.fintlabs.applicationResourceLocation.ApplicationResourceLocationRepository;
import no.fintlabs.applicationResourceLocation.ApplicationResourceLocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;


@DataJpaTest
@Testcontainers
@Import({ApplicationResourceLocationService.class})
public class ApplicationResourceLocationServiceIntegrationTest extends DatabaseIntegrationTest {

    @Autowired
    private ApplicationResourceLocationService applicationResourceLocationService;
    @Autowired
    private ApplicationResourceLocationRepository applicationResourceLocationRepository;
    @Autowired
    private ApplicationResourceRepository applicationResourceRepository;
    @MockBean
    private ApplicationResourceService applicationResourceService;
    @MockBean
    private ApplicationResourceLocationExtendedProduserService applicationResourceLocationExtendedProduserService;

    private final String adobek12 = "adobek12";
    private final String adobeResourceName = "Adobe K12 Creative Cloud";

    private final String kompavd = "kompavd";
    private final String orgUnitName = "Adobe K12 Creative Cloud";

    ApplicationResource adobeK12ApplicationResource = ApplicationResource.builder()
            .resourceId(adobek12)
            .resourceName("Adobe K12 Creative Cloud")
            .build();

    ApplicationResourceLocation applicationResourceLocationAdobeK12 = ApplicationResourceLocation
            .builder()
            .resourceId(adobek12)
            .resourceName(adobeResourceName)
            .orgUnitId(kompavd)
            .orgUnitName(orgUnitName)
            .resourceLimit(50L)
            .build();

    ApplicationResourceLocation applicationResourceLocationUpdated = ApplicationResourceLocation
            .builder()
            .resourceId(adobek12)
            .orgUnitId(kompavd)
            .resourceLimit(250L)
            .build();

    ApplicationResourceLocation applicationResourceLocationKabal = ApplicationResourceLocation
            .builder()
            .resourceId("kabal")
            .resourceName("Kabal")
            .orgUnitId(kompavd)
            .orgUnitName(orgUnitName)
            .build();

    @BeforeEach
    public void setUp() {
        applicationResourceRepository.deleteAll();
        applicationResourceRepository.save(adobeK12ApplicationResource);
    }

    @Test
    public void shouldSaveNewApplicationResourceLocationWhenApplicationResourceExists() {
        given(applicationResourceService.getApplicationResourceByResourceId(adobeK12ApplicationResource.getResourceId())).willReturn(Optional.of(adobeK12ApplicationResource));

        Optional<ApplicationResourceLocation> savedApplicationResourceLocationOptional = applicationResourceLocationService.save(applicationResourceLocationAdobeK12);

        assertThat(savedApplicationResourceLocationOptional.isPresent()).isTrue();
        assertThat(applicationResourceLocationRepository.count()).isEqualTo(1L);

        ApplicationResourceLocation savedApplicationResourceLocation = savedApplicationResourceLocationOptional.get();

        assertThat(savedApplicationResourceLocation.getResourceId()).isEqualTo(adobek12);
        assertThat(savedApplicationResourceLocation.getResourceName()).isEqualTo(adobeResourceName);
        assertThat(savedApplicationResourceLocation.getOrgUnitId()).isEqualTo(kompavd);
        assertThat(savedApplicationResourceLocation.getOrgUnitName()).isEqualTo(orgUnitName);
        assertThat(savedApplicationResourceLocation.getResourceLimit()).isEqualTo(50L);
    }

    @Test
    public void shouldNotSaveApplicationResourceLocationWhenApplicationResourceDoesNotExists() {
        given(applicationResourceService.getApplicationResourceByResourceId(adobeK12ApplicationResource.getResourceId())).willReturn(Optional.of(adobeK12ApplicationResource));

        Optional<ApplicationResourceLocation> savedApplicationResourceLocationOptional = applicationResourceLocationService.save(applicationResourceLocationKabal);

        assertThat(savedApplicationResourceLocationOptional.isEmpty()).isTrue();
        assertThat(applicationResourceLocationRepository.count()).isEqualTo(0L);
    }

    @Test
    public void shouldUpdateExistingApplicationResourceLocation() {
        given(applicationResourceService.getApplicationResourceByResourceId(adobeK12ApplicationResource.getResourceId())).willReturn(Optional.of(adobeK12ApplicationResource));

        Optional<ApplicationResourceLocation> savedApplicationResourceLocationOptional = applicationResourceLocationService.save(applicationResourceLocationAdobeK12);

        assertThat(savedApplicationResourceLocationOptional.isPresent()).isTrue();
        assertThat(applicationResourceLocationRepository.count()).isEqualTo(1L);

        Optional<ApplicationResourceLocation> savedApplicationResourceLocationUpdatedOptional = applicationResourceLocationService.save(applicationResourceLocationUpdated);

        assertThat(savedApplicationResourceLocationUpdatedOptional.isPresent()).isTrue();
        assertThat(applicationResourceLocationRepository.count()).isEqualTo(1L);

        assertThat(savedApplicationResourceLocationOptional.get().getId()).isEqualTo(savedApplicationResourceLocationUpdatedOptional.get().getId());
        assertThat(savedApplicationResourceLocationUpdatedOptional.get().getResourceLimit()).isEqualTo(250L);
    }
}
