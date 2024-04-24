package no.fintlabs.resource;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.ResponseFactory;
import no.fintlabs.applicationResource.ApplicationResourceDTOFrontendDetail;
import no.fintlabs.applicationResource.ApplicationResourceService;
import no.fintlabs.applicationResource.ApplicationCategoryService;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/api/resources")
public class ResourceController {
    private final ApplicationResourceService applicationResourceService;
    private final ResponseFactory responseFactory;
    private final ApplicationCategoryService applicationCategoryService;



    public ResourceController(ApplicationResourceService applicationResourceService, ResponseFactory responseFactory, ApplicationCategoryService applicationCategoryService) {
        this.applicationResourceService = applicationResourceService;
        this.responseFactory = responseFactory;
        this.applicationCategoryService = applicationCategoryService;
    }

    @GetMapping()
    public ResponseEntity<Map<String, Object>> getAllResources(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "search",defaultValue = "%") String search,
            @RequestParam(value = "orgUnits",required = false) List<String> orgUnits,
            @RequestParam(value="type",defaultValue = "ALLTYPES") String type,
            @RequestParam(value ="page",defaultValue ="0") int page,
            @RequestParam(defaultValue = "${fint.kontroll.resource-catalog.pagesize:20}") int size
    ){
        if (orgUnits==null){
            return responseFactory.toResponsEntity(FintJwtEndUserPrincipal.from(jwt),search,type,page,size);
        }
        else {
            return responseFactory.toResponsEntity(FintJwtEndUserPrincipal.from(jwt),search,orgUnits,type,page,size);
        }


    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResourceDTOFrontendDetail> getApplicationResourceById(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id){
        log.info("Fetching applicationResourse by id: " + id);
        ApplicationResourceDTOFrontendDetail applicationResourceDTOFrontendDetail = applicationResourceService
                .getApplicationResourceDTOFrontendDetailById(FintJwtEndUserPrincipal.from(jwt), id);
        if (applicationResourceDTOFrontendDetail.isValid()){
            return new ResponseEntity<>(applicationResourceDTOFrontendDetail, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/applicationcategories")
    public ResponseEntity<List<String>> getApplicationCategories(){
        List<String> applicationCategories = applicationCategoryService.getAllApplicationCategories();

        if (!applicationCategories.isEmpty()){
            return new ResponseEntity<>(applicationCategories, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }


    }
}
