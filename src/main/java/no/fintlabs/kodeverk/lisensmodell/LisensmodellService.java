package no.fintlabs.kodeverk.lisensmodell;


import lombok.extern.slf4j.Slf4j;
import no.fintlabs.applicationResource.ApplicationResourceNotFoundExeption;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class LisensmodellService {
    public List<Lisensmodell> getAllLisensmodell() {
        return null;
    }

    public Lisensmodell getlisensmodellById(Long id) {
        return null;
    }

    public Lisensmodell saveLisensmodell(Lisensmodell newLisensmodell) {
        return null;
    }

    public void deleteLisensmodell(Long id) throws ApplicationResourceNotFoundExeption {

    }
}
