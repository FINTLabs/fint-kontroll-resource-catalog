package no.fintlabs.kodeverk.applikasjonskategori;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.applicationResource.ApplicationResourceNotFoundExeption;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ApplikasjonskategoriService {
    private final ApplikasjonskategoriRepository applikasjonskategoriRepository;

    public ApplikasjonskategoriService(ApplikasjonskategoriRepository repository) {
        this.applikasjonskategoriRepository = repository;
    }

    public List<Applikasjonskategori> getAllApplikasjonskategori() {
        return applikasjonskategoriRepository.findAll();
    }

    public Applikasjonskategori getApplikasjonskategori(Long id) {
        return applikasjonskategoriRepository.findById(id).orElse(null);
    }

    public Applikasjonskategori saveApplikasjonskategori(Applikasjonskategori applikasjonskategori) {
        boolean toBeCreated = applikasjonskategori.getId() != null;
        Applikasjonskategori newOrUpdatedApplikasjonskategori = applikasjonskategoriRepository.saveAndFlush(applikasjonskategori);

        if (toBeCreated) {
            log.info("Updated applikasjonskategori: {} - {}", newOrUpdatedApplikasjonskategori.getId(),newOrUpdatedApplikasjonskategori.getName());
        }
        else {
            log.info("Created applikasjonskategori: {} - {}", newOrUpdatedApplikasjonskategori.getId(),newOrUpdatedApplikasjonskategori.getName());
        }

        return newOrUpdatedApplikasjonskategori;
    }

    public void deleteApplikasjonskategori(Long id) throws ApplicationResourceNotFoundExeption {

        applikasjonskategoriRepository.deleteById(id);
        log.info("deleted applikasjonskategori: {}", id);
    }
}
