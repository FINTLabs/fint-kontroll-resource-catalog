package no.fintlabs.resource;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.ResponseFactory;
import no.fintlabs.applicationResource.*;
import no.fintlabs.opa.model.OrgUnitType;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/api/resources")
public class ResourceController {
    private final ApplicationResourceService applicationResourceService;
    private final ResponseFactory responseFactory;
    private final ApplicationCategoryService applicationCategoryService;
    private final AccessTypeService accessTypeService;



    public ResourceController(ApplicationResourceService applicationResourceService, ResponseFactory responseFactory, ApplicationCategoryService applicationCategoryService, AccessTypeService accessTypeService) {
        this.applicationResourceService = applicationResourceService;
        this.responseFactory = responseFactory;
        this.applicationCategoryService = applicationCategoryService;
        this.accessTypeService = accessTypeService;
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

    @GetMapping("/accesstypes")
    public ResponseEntity<List<String>> getAccessTypes(){
        List<String> accessTypes = accessTypeService.getAllAccessTypes();

        if (!accessTypes.isEmpty()){
            return new ResponseEntity<>(accessTypes, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/v1")
    public ResponseEntity<Map<String, Object>> getAllResourcesUsingSpesification(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "search",required = false) String search,
            @RequestParam(value = "orgunits",required = false) List<String> orgUnits,
            @RequestParam(value = "resourceType",required = false) String resourceType,
            @RequestParam(value = "usertype",required = false) List<String> userType,
            @RequestParam(value = "accesstype",required = false) String accessType,
            @RequestParam(value = "applicationcategory", required = false) List<String> applicationCategory,
            @RequestParam(value ="page",defaultValue ="0") int page,
            @RequestParam(defaultValue = "${fint.kontroll.resource-catalog.pagesize:20}") int size
    ){
        if (orgUnits==null){
            List<String> allAuthorizedOrgUnitIds = applicationResourceService.getAllAuthorizedOrgUnitIDs();
            if (allAuthorizedOrgUnitIds.contains(OrgUnitType.ALLORGUNITS.name())){
                return responseFactory.toResponsEntity(FintJwtEndUserPrincipal.from(jwt),search,orgUnits, resourceType,userType,accessType,applicationCategory,page,size);
            }

            return responseFactory.toResponsEntity(FintJwtEndUserPrincipal.from(jwt),search, allAuthorizedOrgUnitIds, resourceType,userType,accessType,applicationCategory,page,size);
        }
        else {
            List<String> authorizedOrgUnitIds = applicationResourceService.compareRequestedOrgUnitIDsWithOPA(orgUnits);
            if (authorizedOrgUnitIds.contains(OrgUnitType.ALLORGUNITS.name())){
                return responseFactory.toResponsEntity(FintJwtEndUserPrincipal.from(jwt),search,orgUnits, resourceType,userType,accessType,applicationCategory,page,size);
            }

            return responseFactory.toResponsEntity(FintJwtEndUserPrincipal.from(jwt),search,authorizedOrgUnitIds, resourceType,userType,accessType,applicationCategory,page,size);
        }
    }


    @PostMapping("v1")
    public ResponseEntity<HttpStatus> createApplicationResource( @AuthenticationPrincipal Jwt jwt, @RequestBody ApplicationResource request){
        FintJwtEndUserPrincipal principal = FintJwtEndUserPrincipal.from(jwt);

        ApplicationResource applicationResource = ApplicationResource.builder()
                .resourceId(request.resourceId)
                .resourceName(request.resourceName)
                .resourceType(request.resourceType)
                .platform(request.getPlatform())
                .accessType(request.getAccessType())
                .resourceLimit(request.getResourceLimit())
                .resourceOwnerOrgUnitId(request.getResourceOwnerOrgUnitId())
                .resourceOwnerOrgUnitName(request.getResourceOwnerOrgUnitName())
                .validForRoles(request.getValidForRoles())
                .applicationCategory(request.getApplicationCategory())
                .licenseEnforcement(request.getLicenseEnforcement())
                .unitCost(request.getUnitCost())
                .status(request.getStatus())
                .statusChanged(Date.from(Instant.now()))
                .dateCreated(Date.from(Instant.now()))
                .createdBy(principal.getMail())
                .hasCost(request.isHasCost())
                .validForOrgUnits(request.getValidForOrgUnits())
                .build();

        ApplicationResource newApplicationResource = applicationResourceService.createApplicationResource(applicationResource);
        if (newApplicationResource != null){
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
        else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
