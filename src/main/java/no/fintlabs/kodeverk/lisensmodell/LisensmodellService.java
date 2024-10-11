package no.fintlabs.kodeverk.lisensmodell;


import lombok.extern.slf4j.Slf4j;
import no.fintlabs.applicationResource.ApplicationResourceNotFoundExeption;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class LisensmodellService {
    private final LisensmodellRepository lisensmodellRepository;

    public LisensmodellService(LisensmodellRepository lisensmodellRepository) {
        this.lisensmodellRepository = lisensmodellRepository;
    }

    public List<Lisensmodell> getAllLisensmodell() {

        return lisensmodellRepository.findAll();
    }

    public Lisensmodell getlisensmodellById(Long id) {

        return lisensmodellRepository.findById(id).orElse(null);
    }

    public Lisensmodell saveLisensmodell(Lisensmodell newLisensmodell) {
        boolean toBeCreated = newLisensmodell.getId() == null;
        Lisensmodell newOrUpdatedLisensmodell = lisensmodellRepository.saveAndFlush(newLisensmodell);
        if (toBeCreated) {
            log.info("Created Lisensmodell: {} - {}", newOrUpdatedLisensmodell.getId(),newOrUpdatedLisensmodell.getName());
        } else {
            log.info("Updated Lisensmodell: {} - {}", newOrUpdatedLisensmodell.getId(),newOrUpdatedLisensmodell.getName());
        }
        return newOrUpdatedLisensmodell;
    }


    public void deleteLisensmodell(Long id) throws ApplicationResourceNotFoundExeption {
        lisensmodellRepository.deleteById(id);
        log.info("Deleted Lisensmodell: {}", id);
    }

}
