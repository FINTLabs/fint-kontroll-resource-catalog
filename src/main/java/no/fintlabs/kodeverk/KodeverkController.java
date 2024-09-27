package no.fintlabs.kodeverk;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.kodeverk.brukertype.Brukertype;
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

    public KodeverkController(BrukertypeService brukertypeService) {
        this.brukertypeService = brukertypeService;
    }


    @GetMapping("/brukertype/v1")
    public List<Brukertype> getAllBrukerType(){
        return brukertypeService.getAllBrukertypes();
    }


    @GetMapping("/brukertype/v1/{id}")
    public ResponseEntity<Brukertype> getBrukertypeById(@PathVariable Long id) {
        Brukertype brukertype = brukertypeService.getBrukertypeById(id);
        if (brukertype != null) {
            return new ResponseEntity<>(brukertype, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }


    //TODO: vurdere om kun oppdatering av fkLabel
    @PutMapping("/brukertype/v1")
    public ResponseEntity<HttpStatus> updateBrukerType(@RequestBody Brukertype brukertype) {
        Brukertype updatedBrukertype = brukertypeService.updateBrukertype(brukertype);
        if (updatedBrukertype != null) {
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        }
        else {
            return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}
