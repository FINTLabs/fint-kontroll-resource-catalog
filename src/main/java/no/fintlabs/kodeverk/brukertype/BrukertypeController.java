package no.fintlabs.kodeverk.brukertype;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/resources/kodeverk/brukertype")
public class BrukertypeController {
    private final BrukertypeService brukertypeService;

    public BrukertypeController(BrukertypeService brukertypeService) {
        this.brukertypeService = brukertypeService;
    }


    @GetMapping("/v1")
    public List<Brukertype> getAllBrukerType() {
        return brukertypeService.getAllBrukertypes();
    }


    @GetMapping("/v1/{id}")
    public ResponseEntity<Brukertype> getBrukertypeById(@PathVariable Long id) {
        Brukertype brukertype = brukertypeService.getBrukertypeById(id);
        if (brukertype != null) {

            return new ResponseEntity<>(brukertype, HttpStatus.OK);
        } else {

            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }


    @PatchMapping("/v1/{id}")
    public ResponseEntity<Brukertype> updateBrukertype(@PathVariable Long id, @RequestBody BrukertypePatchDTO brukertypePatchDTO) {
        Brukertype updatedBrukertype = brukertypeService.updateBrukertype(id, brukertypePatchDTO.getFkLabel());
        if (updatedBrukertype != null) {

            return new ResponseEntity<>(updatedBrukertype, HttpStatus.OK);
        } else {

            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }
}
