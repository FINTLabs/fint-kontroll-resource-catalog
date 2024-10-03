package no.fintlabs.kodeverk;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.applicationResource.ApplicationResourceNotFoundExeption;
import no.fintlabs.kodeverk.applikasjonskategori.Applikasjonskategori;
import no.fintlabs.kodeverk.applikasjonskategori.ApplikasjonskategoriService;
import no.fintlabs.kodeverk.brukertype.Brukertype;
import no.fintlabs.kodeverk.brukertype.BrukertypePatchDTO;
import no.fintlabs.kodeverk.brukertype.BrukertypeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/resources/kodeverk")
public class KodeverkController {
    private final BrukertypeService brukertypeService;
    private final ApplikasjonskategoriService applikasjonskategoriService;

    public KodeverkController(BrukertypeService brukertypeService, ApplikasjonskategoriService applikasjonskategoriService) {
        this.brukertypeService = brukertypeService;
        this.applikasjonskategoriService = applikasjonskategoriService;
    }


    @GetMapping("/brukertype/v1")
    public List<Brukertype> getAllBrukerType() {
        return brukertypeService.getAllBrukertypes();
    }


    @GetMapping("/brukertype/v1/{id}")
    public ResponseEntity<Brukertype> getBrukertypeById(@PathVariable Long id) {
        Brukertype brukertype = brukertypeService.getBrukertypeById(id);
        if (brukertype != null) {
            return new ResponseEntity<>(brukertype, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }


    @PatchMapping("/brukertype/v1/{id}")
    public ResponseEntity<Brukertype> updateBrukertype(@PathVariable Long id, @RequestBody BrukertypePatchDTO brukertypePatchDTO) {
        Brukertype updatedBrukertype = brukertypeService.updateBrukertype(id, brukertypePatchDTO.getFkLabel());
        if (updatedBrukertype != null) {
            return new ResponseEntity<>(updatedBrukertype, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @PutMapping("/brukertype/v1")
    public ResponseEntity<HttpStatus> updateBrukerType(@RequestBody Brukertype brukertype) {
        Brukertype updatedBrukertype = brukertypeService.updateBrukertype(brukertype);
        if (updatedBrukertype != null) {
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        } else {
            return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        }
    }


    @GetMapping("/applikasjonskategori/v1")
    public List<Applikasjonskategori> getAllApplikasjonskategori() {
        return applikasjonskategoriService.getAllApplikasjonskategori();
    }

    @GetMapping("/applikasjonskategori/v1/{id}")
    public ResponseEntity<Applikasjonskategori> getApplikasjonskategoriById(@PathVariable Long id) {
        Applikasjonskategori applikasjonskategori =applikasjonskategoriService.getApplikasjonskategori(id);
        if (applikasjonskategori != null) {
            return new ResponseEntity<>(applikasjonskategori, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }


    }


    @PostMapping("/applikasjonskategori/v1")
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

    @PutMapping("/applikasjonskategori/v1")
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


    @DeleteMapping("/applikasjonskategori/v1/{id}")
    public ResponseEntity<HttpStatus> deleteApplikasjonskategori(@PathVariable Long id) {
        try {
            applikasjonskategoriService.deleteApplikasjonskategori(id);
        } catch (ApplicationResourceNotFoundExeption applicationResourceNotFoundExeption) {
            log.error("Application resource not found", applicationResourceNotFoundExeption);

            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


}
