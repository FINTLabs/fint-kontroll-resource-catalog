package no.fintlabs.kodeverk.applikasjonskategori;

import no.fintlabs.applicationResource.ApplicationResourceNotFoundException;
import no.fintlabs.applicationResource.ApplicationResourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplikasjonskategoriServiceTest {

    @Mock
    private ApplikasjonskategoriRepository applikasjonskategoriRepository;

    @Mock
    private ApplicationResourceRepository applicationResourceRepository;

    private ApplikasjonskategoriService applikasjonskategoriService;

    @BeforeEach
    void setUp() {
        applikasjonskategoriService = new ApplikasjonskategoriService(
                applikasjonskategoriRepository,
                applicationResourceRepository
        );
    }

    @Test
    void shouldSaveUpdatedApplikasjonskategoriWithoutUpdatingApplicationResources() {
        Applikasjonskategori updatedApplikasjonskategori = Applikasjonskategori.builder()
                .id(1L)
                .name("Arkiv")
                .build();

        when(applikasjonskategoriRepository.saveAndFlush(updatedApplikasjonskategori)).thenReturn(updatedApplikasjonskategori);

        applikasjonskategoriService.saveApplikasjonskategori(updatedApplikasjonskategori);

        verify(applikasjonskategoriRepository).saveAndFlush(updatedApplikasjonskategori);
    }

    @Test
    void shouldFindApplikasjonskategoriByNames() {
        Applikasjonskategori category = Applikasjonskategori.builder()
                .id(1L)
                .name("Saksbehandling")
                .build();

        when(applikasjonskategoriRepository.findByNameIn(java.util.Set.of("Saksbehandling"))).thenReturn(java.util.List.of(category));

        java.util.Set<Applikasjonskategori> categories = applikasjonskategoriService.getApplikasjonskategoriByNames(
                java.util.List.of("Saksbehandling")
        );

        assertThat(categories).containsExactly(category);
    }

    @Test
    void shouldThrowWhenApplikasjonskategoriNameDoesNotExist() {
        when(applikasjonskategoriRepository.findByNameIn(java.util.Set.of("Ukjent"))).thenReturn(java.util.List.of());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> applikasjonskategoriService.getApplikasjonskategoriByNames(java.util.List.of("Ukjent"))
        );

        assertThat(exception.getMessage()).contains("Ukjent");
    }

    @Test
    void shouldCreateMissingApplikasjonskategoriByNames() {
        Applikasjonskategori existingCategory = Applikasjonskategori.builder()
                .id(1L)
                .name("Saksbehandling")
                .build();
        Applikasjonskategori createdCategory = Applikasjonskategori.builder()
                .id(2L)
                .name("Pedagogisk programvare")
                .build();

        when(applikasjonskategoriRepository.findByNameIn(java.util.Set.of("Saksbehandling", "Pedagogisk programvare")))
                .thenReturn(java.util.List.of(existingCategory));
        when(applikasjonskategoriRepository.saveAllAndFlush(anyList())).thenReturn(java.util.List.of(createdCategory));

        java.util.Set<Applikasjonskategori> categories = applikasjonskategoriService.getOrCreateApplikasjonskategoriByNames(
                java.util.List.of("Saksbehandling", "Pedagogisk programvare")
        );

        assertThat(categories)
                .extracting(Applikasjonskategori::getName)
                .containsExactly("Saksbehandling", "Pedagogisk programvare");
        assertThat(categories)
                .extracting(Applikasjonskategori::getId)
                .containsExactly(1L, 2L);
    }

    @Test
    void shouldDeleteApplikasjonskategori() {
        Applikasjonskategori applikasjonskategori = Applikasjonskategori.builder()
                .id(1L)
                .name("Saksbehandling")
                .build();

        when(applikasjonskategoriRepository.findById(1L)).thenReturn(Optional.of(applikasjonskategori));

        applikasjonskategoriService.deleteApplikasjonskategori(1L);

        verify(applicationResourceRepository).deleteApplicationCategories(java.util.List.of(1L));
        verify(applikasjonskategoriRepository).delete(applikasjonskategori);
    }

    @Test
    void shouldNotDeleteWhenApplikasjonskategoriDoesNotExist() {
        when(applikasjonskategoriRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ApplicationResourceNotFoundException.class,
                () -> applikasjonskategoriService.deleteApplikasjonskategori(1L));

        verify(applikasjonskategoriRepository, never()).deleteById(1L);
    }
}
