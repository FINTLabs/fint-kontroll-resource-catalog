package no.fintlabs.kodeverk.lisensmodell;


import lombok.extern.slf4j.Slf4j;
import no.fintlabs.applicationResource.ApplicationResourceNotFoundExeption;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/resources/kodeverk/lisensmodell")
public class LisensmodellController {
    private final LisensmodellService lisensmodellService;

    public LisensmodellController(LisensmodellService lisensmodellService) {
        this.lisensmodellService = lisensmodellService;
    }

    @GetMapping("/v1")
    public List<Lisensmodell> getLisensmodell() {
        return lisensmodellService.getAllLisensmodell();
    }

    @GetMapping("v1/{id}")
    public ResponseEntity<Lisensmodell> getLisensmodell(@PathVariable Long id) {
        Lisensmodell lisensmodell = lisensmodellService.getlisensmodellById(id);

        if (lisensmodell == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(lisensmodell);
        }
    }


    @PostMapping("v1")
    public ResponseEntity<Lisensmodell> addLisensmodell(@RequestBody Lisensmodell lisensmodell) {
        Lisensmodell newLisensmodell = Lisensmodell
                .builder()
                .name(lisensmodell.getName())
                .description(lisensmodell.getDescription())
                .category(lisensmodell.getCategory())
                .build();

        Lisensmodell createdLisenmodell = lisensmodellService.saveLisensmodell(newLisensmodell);
        if (createdLisenmodell == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        } else {
            return new ResponseEntity<>(createdLisenmodell,HttpStatus.CREATED);
        }
    }

    @PutMapping("v1")
    public ResponseEntity<Lisensmodell> updateLisensmodell(@RequestBody Lisensmodell lisensmodell) {
        Lisensmodell updatedLisensmodell = Lisensmodell
                .builder()
                .id(lisensmodell.getId())
                .name(lisensmodell.getName())
                .description(lisensmodell.getDescription())
                .category(lisensmodell.getCategory())
                .build();

        Lisensmodell savedLisenmodell = lisensmodellService.saveLisensmodell(updatedLisensmodell);
        if (savedLisenmodell == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        } else {
            return new ResponseEntity<>(savedLisenmodell,HttpStatus.ACCEPTED);
        }
    }


    @DeleteMapping("/v1/{id}")
    public ResponseEntity<Lisensmodell> deleteLisensmodell(@PathVariable Long id) {
        try {
            lisensmodellService.deleteLisensmodell(id);
        } catch (ApplicationResourceNotFoundExeption applicationResourceNotFoundExeption) {
            log.error("Application resource not found", applicationResourceNotFoundExeption);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }


}
