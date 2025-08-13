package no.fintlabs.kodeverk.applikasjonskategori;


import lombok.extern.slf4j.Slf4j;
import no.fintlabs.applicationResource.ApplicationResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/resources/kodeverk/applikasjonskategori")
public class ApplikasjonskategoriController {
    private final ApplikasjonskategoriService applikasjonskategoriService;

    public ApplikasjonskategoriController(ApplikasjonskategoriService applikasjonskategoriService) {
        this.applikasjonskategoriService = applikasjonskategoriService;
    }


    @GetMapping("/v1")
    public List<Applikasjonskategori> getAllApplikasjonskategori() {

        return applikasjonskategoriService.getAllApplikasjonskategori();
    }


    @GetMapping("/v1/{id}")
    public ResponseEntity<Applikasjonskategori> getApplikasjonskategoriById(@PathVariable Long id) {
        Applikasjonskategori applikasjonskategori =applikasjonskategoriService.getApplikasjonskategori(id);
        if (applikasjonskategori != null) {

            return new ResponseEntity<>(applikasjonskategori, HttpStatus.OK);
        } else {

            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }


    @PostMapping("/v1")
    public ResponseEntity<Applikasjonskategori> createApplikasjonskategori(@RequestBody Applikasjonskategori applikasjonskategori) {
        Applikasjonskategori newApplikasjonskategori = Applikasjonskategori
                .builder()
                .name(applikasjonskategori.getName())
                .description(applikasjonskategori.getDescription())
                .category(applikasjonskategori.getCategory())
                .build();
        Applikasjonskategori createdApplikasjonskategori = applikasjonskategoriService.saveApplikasjonskategori(newApplikasjonskategori);

        if (createdApplikasjonskategori != null) {

            return new ResponseEntity<>(createdApplikasjonskategori, HttpStatus.CREATED);
        } else {

            return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        }
    }


    @PutMapping("/v1")
    public ResponseEntity<Applikasjonskategori> updateApplikasjonskategori(@RequestBody Applikasjonskategori applikasjonskategori) {
        Applikasjonskategori newApplikasjonskategori = Applikasjonskategori
                .builder()
                .id(applikasjonskategori.getId())
                .name(applikasjonskategori.getName())
                .description(applikasjonskategori.getDescription())
                .category(applikasjonskategori.getCategory())
                .build();

        Applikasjonskategori updatedApplikasjonskategori = applikasjonskategoriService.saveApplikasjonskategori(newApplikasjonskategori);

        if (updatedApplikasjonskategori != null) {

            return new ResponseEntity<>(updatedApplikasjonskategori, HttpStatus.ACCEPTED);
        } else {

            return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        }
    }


    @DeleteMapping("/v1/{id}")
    public ResponseEntity<HttpStatus> deleteApplikasjonskategori(@PathVariable Long id) {
        try {
            applikasjonskategoriService.deleteApplikasjonskategori(id);
        } catch (ApplicationResourceNotFoundException applicationResourceNotFoundException) {
            log.error("Application resource not found", applicationResourceNotFoundException);

            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
