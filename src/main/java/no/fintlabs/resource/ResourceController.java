package no.fintlabs.resource;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.applicationResource.ApplicationResourceDTO;
import no.fintlabs.applicationResource.ApplicationResourceRepository;
import no.fintlabs.applicationResource.ApplicationResourceService;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/resources")
public class ResourceController {
    private final ApplicationResourceService applicationResourceService;
    private final ApplicationResourceRepository applicationResourceRepository;


    public ResourceController(ApplicationResourceService applicationResourceService, ApplicationResourceRepository applicationResourceRepository) {
        this.applicationResourceService = applicationResourceService;
        this.applicationResourceRepository = applicationResourceRepository;
    }

    @GetMapping()
    public List<ApplicationResourceDTO> getAllResources(@AuthenticationPrincipal Jwt jwt){
        return applicationResourceService.getAllApplicationResources();
    }

    @GetMapping("/{id}")
    public ApplicationResourceDTO getApplicationResourceById(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id){
        log.info("Fetching applicationResourse by id: " + id);
        return applicationResourceService.getApplicationResourceById(FintJwtEndUserPrincipal.from(jwt), id);
    }
}
