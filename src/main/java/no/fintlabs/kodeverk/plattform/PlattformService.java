package no.fintlabs.kodeverk.plattform;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
public class PlattformService {
    private final PlattformRepository plattformRepository;

    public PlattformService(PlattformRepository plattformRepository) {
        this.plattformRepository = plattformRepository;
    }

    public Set<Plattform> getAllPlattform() {
        return plattformRepository.findAllBy();
    }

    public Plattform getPlattformById(Long id) {

        return plattformRepository.findById(id).orElse(null);
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
}
