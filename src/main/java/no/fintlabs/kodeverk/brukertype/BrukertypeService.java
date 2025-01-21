package no.fintlabs.kodeverk.brukertype;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.applicationResource.ApplicationResourceUserType;
import no.fintlabs.applicationResource.ApplicationResourceUserTypeToBrukerMapping;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public void save(ApplicationResourceUserType applicationResourceUserType) {
        Optional<Brukertype> mappedBrukertypeOptional =
                Optional.ofNullable(ApplicationResourceUserTypeToBrukerMapping.mapResourceUserTypeToBrukerType(applicationResourceUserType));

        if (mappedBrukertypeOptional.isEmpty()) {
            log.warn("Could not map ApplicationResourceUserType to Brukertype");
            return;
        }
        Brukertype mappedBrukertype = mappedBrukertypeOptional.get();
        Optional<Brukertype> existingBrukertype = brukertypeRepository.findBrukertypeByLabel(mappedBrukertype.getLabel());

        if (existingBrukertype.isEmpty()) {
            log.info("Brukertype {} not found. Saving new brukertype with displayname {}", mappedBrukertype.getLabel(), mappedBrukertype.getFkLabel());
            brukertypeRepository.save(mappedBrukertype);
            return;
        }
        log.info("Brukertype {} already exists", mappedBrukertype.getLabel());
        mappedBrukertype.setId(existingBrukertype.get().getId());
        log.info("Updating existing brukertype {} with displayname {}", mappedBrukertype.getLabel(), mappedBrukertype.getFkLabel());
        brukertypeRepository.save(mappedBrukertype);
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

    public Brukertype updateBrukertype(Long id, String fkLabel) {
        Brukertype currentBrukertype = brukertypeRepository.findById(id).orElse(null);

        if (currentBrukertype != null) {
            currentBrukertype.setFkLabel(fkLabel);
            log.info("Brukertype updated: {} - {} - {}", currentBrukertype.getId(),currentBrukertype.getFkLabel(),currentBrukertype.getLabel());

            return brukertypeRepository.saveAndFlush(currentBrukertype);
        }

        return null;
    }
}
