package no.fintlabs.kodeverk.plattform;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/resources/kodeverk/plattform")
public class PlattformController {
    private final PlattformService plattformService;

    public PlattformController(PlattformService plattformService) {
        this.plattformService = plattformService;
    }

    @GetMapping
    public Set<Plattform> getAllPlattform(){

        return plattformService.getAllPlattform();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Plattform> getPlattformById(@PathVariable Long id){
        Plattform plattform = plattformService.getPlattformById(id);
        if (plattform != null){

            return new ResponseEntity<>(plattform, HttpStatus.OK);
        } else {

            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @PostMapping
    public ResponseEntity<Plattform> createPlattform(@RequestBody Plattform plattform){
        Plattform newPlattform = Plattform
                .builder()
                .name(plattform.getName())
                .build();
        Plattform createdPlattform = plattformService.savePlattform(newPlattform);
        if (createdPlattform != null){

            return new ResponseEntity<>(createdPlattform, HttpStatus.ACCEPTED);
        } else {

            return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}
