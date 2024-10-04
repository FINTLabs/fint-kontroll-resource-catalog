package no.fintlabs.kodeverk.handhevingstype;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/resources/kodeverk/handhevingstype")
public class HandhevingstypeController {
    private final HandhevingstypeService handhevingstypeService;
    private final HandhevingstypeRepository handhevingstypeRepository;

    public HandhevingstypeController(HandhevingstypeService handhevingstypeService, HandhevingstypeRepository handhevingstypeRepository) {
        this.handhevingstypeService = handhevingstypeService;
        this.handhevingstypeRepository = handhevingstypeRepository;
    }

    @GetMapping("/v1")
    public List<Handhevingstype> getAllHandhevingstype() {
        return handhevingstypeRepository.findAll();
    }


    @GetMapping("/v1/{id}")
    public ResponseEntity<Handhevingstype> getHandhevingstype(Long id) {
        Handhevingstype handhevingstype = handhevingstypeService.getHandhevingstypeById(id);
        if (handhevingstype == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(handhevingstype);
        }
    }

    @PatchMapping("/v1/{id}")
    public ResponseEntity<HttpStatus> updateHandhevingstype(@PathVariable Long id, @RequestBody HandhevingstypePatchDTO handhevingstypePatchDTO) {
        Handhevingstype updatedHandhevingstype = handhevingstypeService.updateHandhevingstype(id,handhevingstypePatchDTO);
        if (updatedHandhevingstype == null) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } else {
            return ResponseEntity.ok(HttpStatus.ACCEPTED);
        }
    }

}
