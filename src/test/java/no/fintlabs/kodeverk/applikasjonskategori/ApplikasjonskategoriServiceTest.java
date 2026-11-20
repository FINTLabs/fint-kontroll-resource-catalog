package no.fintlabs.kodeverk.applikasjonskategori;

import no.fintlabs.applicationResource.ApplicationResourceNotFoundException;
import no.fintlabs.applicationResource.ApplicationResourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
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
    void shouldFindApplikasjonskategoriByIdsWithoutUsingNames() {
        Applikasjonskategori category = Applikasjonskategori.builder()
                .id(1L)
                .name("Renamed category")
                .build();
        when(applikasjonskategoriRepository.findAllById(List.of(1L))).thenReturn(List.of(category));

        Set<Applikasjonskategori> categories = applikasjonskategoriService.getApplikasjonskategoriByIds(
                List.of(1L, 1L));

        assertThat(categories).containsExactly(category);
        verify(applikasjonskategoriRepository).findAllById(List.of(1L));
        verifyNoMoreInteractions(applikasjonskategoriRepository);
    }

    @Test
    void shouldSkipUnknownCategoryIdsAndKeepExistingCategories() {
        Applikasjonskategori category = Applikasjonskategori.builder().id(1L).name("Existing").build();
        when(applikasjonskategoriRepository.findAllById(List.of(1L, 999L))).thenReturn(List.of(category));

        Set<Applikasjonskategori> categories = applikasjonskategoriService.getApplikasjonskategoriByIds(
                List.of(1L, 999L));

        assertThat(categories).containsExactly(category);
        verify(applikasjonskategoriRepository).findAllById(List.of(1L, 999L));
        verifyNoMoreInteractions(applikasjonskategoriRepository);
    }

    @Test
    void shouldSkipNullCategoryIds() {
        Applikasjonskategori category = Applikasjonskategori.builder().id(1L).name("Existing").build();
        when(applikasjonskategoriRepository.findAllById(List.of(1L))).thenReturn(List.of(category));

        Set<Applikasjonskategori> categories = applikasjonskategoriService.getApplikasjonskategoriByIds(
                Arrays.asList(1L, null));

        assertThat(categories).containsExactly(category);
        verify(applikasjonskategoriRepository).findAllById(List.of(1L));
        verifyNoMoreInteractions(applikasjonskategoriRepository);
    }

    @Test
    void shouldReturnEmptyCategoriesWhenIdsContainOnlyNull() {
        assertThat(applikasjonskategoriService.getApplikasjonskategoriByIds(Arrays.asList((Long) null))).isEmpty();

        verify(applikasjonskategoriRepository).findAllById(List.of());
        verifyNoMoreInteractions(applikasjonskategoriRepository);
    }

    @Test
    void shouldReturnEmptyCategoriesWhenNoIdsExist() {
        when(applikasjonskategoriRepository.findAllById(List.of(999L))).thenReturn(List.of());

        assertThat(applikasjonskategoriService.getApplikasjonskategoriByIds(List.of(999L))).isEmpty();
        verify(applikasjonskategoriRepository).findAllById(List.of(999L));
        verifyNoMoreInteractions(applikasjonskategoriRepository);
    }

    @Test
    void shouldReturnMutableEmptyCategoriesWhenIdsAreOmitted() {
        Set<Applikasjonskategori> categories = applikasjonskategoriService.getApplikasjonskategoriByIds(null);

        assertThat(categories).isEmpty();
        categories.clear();
        verifyNoInteractions(applikasjonskategoriRepository);
    }

    @Test
    void shouldReturnEmptyCategoriesWhenIdsAreEmpty() {
        assertThat(applikasjonskategoriService.getApplikasjonskategoriByIds(List.of())).isEmpty();

        verifyNoInteractions(applikasjonskategoriRepository);
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
