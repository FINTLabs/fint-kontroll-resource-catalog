package no.fintlabs.kodeverk.plattform;


import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.applicationResource.ApplicationResourceNotFoundException;
import no.fintlabs.applicationResource.ApplicationResourceRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PlattformService {
    private final PlattformRepository plattformRepository;
    private final ApplicationResourceRepository applicationResourceRepository;

    public PlattformService(PlattformRepository plattformRepository, ApplicationResourceRepository applicationResourceRepository) {
        this.plattformRepository = plattformRepository;
        this.applicationResourceRepository = applicationResourceRepository;
    }

    public Set<Plattform> getAllPlattform() {
        return plattformRepository.findAllBy();
    }

    public Plattform getPlattformById(Long id) {

        return plattformRepository.findById(id).orElse(null);
    }


    public Set<Plattform> getPlattformByIds(List<Long> ids){
        if (ids == null || ids.isEmpty()){
            return new LinkedHashSet<>();
        }
        return new LinkedHashSet<>(plattformRepository.findAllById(
                ids.stream()
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList()));
    }


    public Set<Plattform> getPlattformByNames(List<String> names){
        Set<String> requestedNames = normalizeNames(names);
        if (requestedNames.isEmpty()){
            return new LinkedHashSet<>();
        }

        List<Plattform> plattforms = plattformRepository.findByNameIn(requestedNames);
        Set<String> foundNames = plattforms.stream()
                .map(Plattform::getName)
                .collect(Collectors.toSet());

        List<String> missingNames = requestedNames.stream()
                .filter(name -> !foundNames.contains(name))
                .toList();

        if (!missingNames.isEmpty()){
            throw new IllegalArgumentException("Unknown plattform names: " + String.join(",",missingNames));
        }

        return new LinkedHashSet<>(plattforms);
    }

    public Plattform savePlattform(Plattform plattform) {
        boolean toBeCreated = plattform.getId() == null;
        Plattform newOrUpdatedPlattform = plattformRepository.saveAndFlush(plattform);
        if (toBeCreated){
            log.info("Plattform entity created: {} - {}", newOrUpdatedPlattform.getId(), newOrUpdatedPlattform.getName());
        } else {
            log.info("Plattform entity updated: {} - {}", newOrUpdatedPlattform.getId(), newOrUpdatedPlattform.getName());
        }
        return newOrUpdatedPlattform;
    }

    @Transactional
    public void deletePlattform(Long id) throws ApplicationResourceNotFoundException {
        Plattform plattform = plattformRepository.findById(id)
                .orElseThrow(() -> new ApplicationResourceNotFoundException(id));

        applicationResourceRepository.deletePlattforms(List.of(id));
        plattformRepository.delete(plattform);
    }


    //TODO: same in ApplikasjonskategoriService. Ta ut i egen function
    private Set<String> normalizeNames(List<String> names) {
        if (names == null || names.isEmpty()) {
            return Set.of();
        }

        return names.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}