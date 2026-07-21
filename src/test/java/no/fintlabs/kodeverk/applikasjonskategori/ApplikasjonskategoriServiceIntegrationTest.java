package no.fintlabs.kodeverk.applikasjonskategori;

import no.fintlabs.DatabaseIntegrationTest;
import no.fintlabs.applicationResource.ApplicationResource;
import no.fintlabs.applicationResource.ApplicationResourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@Import({ApplikasjonskategoriService.class})
class ApplikasjonskategoriServiceIntegrationTest extends DatabaseIntegrationTest {

    @Autowired
    private ApplikasjonskategoriRepository applikasjonskategoriRepository;

    @Autowired
    private ApplikasjonskategoriService applikasjonskategoriService;

    @Autowired
    private ApplicationResourceRepository applicationResourceRepository;

    @BeforeEach
    void setUp() {
        applicationResourceRepository.deleteAll();
        applikasjonskategoriRepository.deleteAll();
    }

    @Test
    void shouldReturnUpdatedApplikasjonskategoriNameWhenNameChanges() {
        Applikasjonskategori applikasjonskategori = applikasjonskategoriRepository.saveAndFlush(
                Applikasjonskategori.builder()
                        .name("Saksbehandling")
                        .build()
        );

        ApplicationResource applicationResource = new ApplicationResource();
        applicationResource.setResourceId("app-1");
        applicationResource.setApplicationCategory(Set.of(applikasjonskategori));
        applicationResourceRepository.saveAndFlush(applicationResource);

        applikasjonskategoriService.saveApplikasjonskategori(
                Applikasjonskategori.builder()
                        .id(applikasjonskategori.getId())
                        .name("Arkiv")
                        .build()
        );

        assertEquals(Set.of("Arkiv"), Set.copyOf(applicationResourceRepository.findAllDistinctApplicationCategories()));
    }

    @Test
    void shouldDeleteApplicationResourceCategoryRowsWhenApplikasjonskategoriIsDeleted() {
        Applikasjonskategori applikasjonskategori = applikasjonskategoriRepository.saveAndFlush(
                Applikasjonskategori.builder()
                        .name("Saksbehandling")
                        .build()
        );

        ApplicationResource applicationResource = new ApplicationResource();
        applicationResource.setResourceId("app-1");
        applicationResource.setApplicationCategory(Set.of(applikasjonskategori));
        applicationResourceRepository.saveAndFlush(applicationResource);

        applikasjonskategoriService.deleteApplikasjonskategori(applikasjonskategori.getId());

        assertEquals(Set.of(), Set.copyOf(applicationResourceRepository.findAllDistinctApplicationCategories()));
    }
}
