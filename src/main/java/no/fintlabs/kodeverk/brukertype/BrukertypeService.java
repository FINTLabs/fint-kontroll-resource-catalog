package no.fintlabs.kodeverk.brukertype;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class BrukertypeService {
    private final BrukertypeRepository brukertypeRepository;

    public BrukertypeService(BrukertypeRepository brukertypeRepository) {
        this.brukertypeRepository = brukertypeRepository;
    }


    public List<Brukertype> getAllBrukertypes() {
        return brukertypeRepository.findAll();
    }


    public Brukertype getBrukertypeById(Long id) {
        return brukertypeRepository.findById(id).orElse(null);
    }


    public Brukertype updateBrukertype(Brukertype brukertype) {
        Brukertype currentBrukertype = brukertypeRepository.findById(brukertype.getId()).orElse(null);

        if (currentBrukertype != null) {
            log.info("Brukertype updated: {} - {} - {}", brukertype.getId(),brukertype.getFkLabel(),brukertype.getLabel());
            currentBrukertype.setFkLabel(brukertype.getFkLabel());
            return brukertypeRepository.saveAndFlush(currentBrukertype);
        }
        return null;
    }
}
