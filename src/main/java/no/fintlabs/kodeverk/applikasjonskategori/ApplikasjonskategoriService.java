package no.fintlabs.kodeverk.applikasjonskategori;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.applicationResource.ApplicationResourceNotFoundException;
import no.fintlabs.applicationResource.ApplicationResourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ApplikasjonskategoriService {
    private final ApplikasjonskategoriRepository applikasjonskategoriRepository;
    private final ApplicationResourceRepository applicationResourceRepository;

    public ApplikasjonskategoriService(
            ApplikasjonskategoriRepository repository,
            ApplicationResourceRepository applicationResourceRepository
    ) {
        this.applikasjonskategoriRepository = repository;
        this.applicationResourceRepository = applicationResourceRepository;
    }

    public List<Applikasjonskategori> getAllApplikasjonskategori() {
        return applikasjonskategoriRepository.findAll();
    }

    public Applikasjonskategori getApplikasjonskategori(Long id) {
        return applikasjonskategoriRepository.findById(id).orElse(null);
    }

    public Set<Applikasjonskategori> getApplikasjonskategoriByNames(List<String> names) {
        if (names == null || names.isEmpty()) {
            return Set.of();
        }

        Set<String> requestedNames = names.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (requestedNames.isEmpty()) {
            return Set.of();
        }

        List<Applikasjonskategori> applikasjonskategorier = applikasjonskategoriRepository.findByNameIn(requestedNames);
        Set<String> foundNames = applikasjonskategorier.stream()
                .map(Applikasjonskategori::getName)
                .collect(Collectors.toSet());

        List<String> missingNames = requestedNames.stream()
                .filter(name -> !foundNames.contains(name))
                .toList();

        if (!missingNames.isEmpty()) {
            throw new IllegalArgumentException("Unknown application categories: " + String.join(", ", missingNames));
        }

        return new LinkedHashSet<>(applikasjonskategorier);
    }

    public Applikasjonskategori saveApplikasjonskategori(Applikasjonskategori applikasjonskategori) {
        boolean toBeCreated = applikasjonskategori.getId() == null;
        Applikasjonskategori newOrUpdatedApplikasjonskategori = applikasjonskategoriRepository.saveAndFlush(applikasjonskategori);

        if (toBeCreated) {
            log.info("Created applikasjonskategori: {} - {}", newOrUpdatedApplikasjonskategori.getId(),newOrUpdatedApplikasjonskategori.getName());
        } else {
            log.info("Updated applikasjonskategori: {} - {}", newOrUpdatedApplikasjonskategori.getId(),newOrUpdatedApplikasjonskategori.getName());
        }

        return newOrUpdatedApplikasjonskategori;
    }

    @Transactional
    public void deleteApplikasjonskategori(Long id) throws ApplicationResourceNotFoundException {
        Applikasjonskategori applikasjonskategori = applikasjonskategoriRepository.findById(id)
                .orElseThrow(() -> new ApplicationResourceNotFoundException(id));

        applicationResourceRepository.deleteApplicationCategories(List.of(id));
        applikasjonskategoriRepository.delete(applikasjonskategori);

        log.info("deleted applikasjonskategori: {}", id);
    }
}
