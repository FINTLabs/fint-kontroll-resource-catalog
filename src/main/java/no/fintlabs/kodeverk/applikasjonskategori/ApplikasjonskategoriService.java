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

    //TODO: sjekk om create eller update og logge (sjekk på id)
    public Applikasjonskategori saveApplikasjonskategori(Applikasjonskategori applikasjonskategori) {
        //Applikasjonskategori currentApplikasjonskategori = applikasjonskategoriRepository.findById(applikasjonskategori.getId()).orElse(null);

        Applikasjonskategori newOrUpdatedApplikasjonskategori = applikasjonskategoriRepository.saveAndFlush(applikasjonskategori);
//
//        if (currentApplikasjonskategori != null) {
//            log.info("updated applikasjonskategori: {}", currentApplikasjonskategori.getName());
//        }
//        else {
            log.info("created applikasjonskategori: {}", newOrUpdatedApplikasjonskategori.getName());
        //}

        return newOrUpdatedApplikasjonskategori;
    }

    public void deleteApplikasjonskategori(Long id) throws ApplicationResourceNotFoundExeption {

        applikasjonskategoriRepository.deleteById(id);
        log.info("deleted applikasjonskategori: {}", id);
    }



}
