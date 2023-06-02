package no.fintlabs.resource;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.ResponseFactory;
import no.fintlabs.applicationResource.ApplicationResourceDTO;
import no.fintlabs.applicationResource.ApplicationResourceRepository;
import no.fintlabs.applicationResource.ApplicationResourceService;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@Slf4j
@RequestMapping("/api/resources")
public class ResourceController {
    private final ApplicationResourceService applicationResourceService;
    private final ApplicationResourceRepository applicationResourceRepository;
    private final ResponseFactory responseFactory;


    public ResourceController(ApplicationResourceService applicationResourceService, ApplicationResourceRepository applicationResourceRepository, ResponseFactory responseFactory) {
        this.applicationResourceService = applicationResourceService;
        this.applicationResourceRepository = applicationResourceRepository;
        this.responseFactory = responseFactory;
    }

    @GetMapping()
    public ResponseEntity<Map<String, Object>> getAllResources(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "search",defaultValue = "%") String search,
            @RequestParam(value="type",defaultValue = "ALLTYPES") String type,
            @RequestParam(value ="page",defaultValue ="0") int page,
            @RequestParam(defaultValue = "${fint.kontroll.resource-catalog.pagesize:20}") int size
    ){
        return responseFactory.toResponsEntity(FintJwtEndUserPrincipal.from(jwt),search,type,page,size);
    }

    @GetMapping("/{id}")
    public Optional<ApplicationResourceDTO> getApplicationResourceById(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id){
        log.info("Fetching applicationResourse by id: " + id);
        return applicationResourceService.getApplicationResourceById(FintJwtEndUserPrincipal.from(jwt), id);
        //endre til responsEntity 200/404
    }
}
