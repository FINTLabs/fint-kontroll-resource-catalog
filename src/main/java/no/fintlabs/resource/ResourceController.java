package no.fintlabs.resource;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.ServiceConfiguration;
import no.fintlabs.applicationResource.*;
import no.fintlabs.kodeverk.brukertype.BrukertypeService;
import no.fintlabs.resourceAvailability.ResourceAvailabilityService;
import no.fintlabs.resourceGroup.ResourceGroupPublishComponent;
import no.fintlabs.util.OnlyDevelopers;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/api/resources")
public class ResourceController {
    private final ApplicationResourceService applicationResourceService;
    private final ApplicationResourceMapper applicationResourceMapper;
    private final ApplicationCategoryService applicationCategoryService;
    private final AccessTypeService accessTypeService;
    private final ServiceConfiguration serviceConfiguration;
    private final ResourceGroupPublishComponent resourceGroupPublishComponent;
    private final ResourceAvailabilityService resourceAvailabilityService;


    public ResourceController(
            ApplicationResourceService applicationResourceService,
            ApplicationResourceMapper applicationResourceMapper,
            ApplicationCategoryService applicationCategoryService,
            AccessTypeService accessTypeService,
            BrukertypeService brukertypeService,
            ServiceConfiguration serviceConfiguration, ResourceGroupPublishComponent resourceGroupPublishComponent, ResourceAvailabilityService resourceAvailabilityService) {
        this.applicationResourceService = applicationResourceService;
        this.applicationResourceMapper = applicationResourceMapper;
        this.applicationCategoryService = applicationCategoryService;
        this.accessTypeService = accessTypeService;
        this.serviceConfiguration = serviceConfiguration;
        this.resourceGroupPublishComponent = resourceGroupPublishComponent;
        this.resourceAvailabilityService = resourceAvailabilityService;
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResourceDTOFrontendDetail> getApplicationResourceById(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        log.info("Fetching applicationResourse by id: {}", id);
        ApplicationResourceDTOFrontendDetail applicationResourceDTOFrontendDetail = applicationResourceService
                .getApplicationResourceDTOFrontendDetailById(id);
        return new ResponseEntity<>(applicationResourceDTOFrontendDetail, HttpStatus.OK);
    }

    @GetMapping("/applicationcategories")
    public ResponseEntity<List<String>> getApplicationCategories() {
        List<String> applicationCategories = applicationCategoryService.getAllApplicationCategories();
        return new ResponseEntity<>(applicationCategories, HttpStatus.OK);

    }

    @GetMapping("/accesstypes")
    public ResponseEntity<List<String>> getAccessTypes() {
        List<String> accessTypes = accessTypeService.getAllAccessTypes();

        if (!accessTypes.isEmpty()) {
            return new ResponseEntity<>(accessTypes, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/v1")
    public ResponseEntity<Map<String, Object>> getAllActiveResources(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "orgunits", required = false) List<String> orgUnits,
            @RequestParam(value = "validorgunits", required = false) List<String> validOrgUnits,
            @RequestParam(value = "resourceType", required = false) String resourceType,
            @RequestParam(value = "usertype", required = false) List<String> userType,
            @RequestParam(value = "accesstype", required = false) String accessType,
            @RequestParam(value = "applicationcategory", required = false) List<String> applicationCategory,
            @SortDefault(sort = {"resourceName"}, direction = Sort.Direction.ASC)
            @ParameterObject @PageableDefault(size = 100) Pageable pageable

    ) {
        try {
            Page<ApplicationResource> allApplicationResources = applicationResourceService
                .searchApplicationResources(
                    FintJwtEndUserPrincipal.from(jwt),
                    search,
                    orgUnits,
                    validOrgUnits,
                    resourceType,
                    userType,
                    accessType,
                    applicationCategory,
                    List.of("ACTIVE","PENDING_ACTIVE"),
                    pageable,
                        false
            );
            if (allApplicationResources == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fetching application resources returned no resources");
            }
            return ResponseEntity.ok(applicationResourceMapper.toApplicationResourceDtoPage(allApplicationResources));
        }
        catch (Exception e) {
            log.error("Error fetching application resources", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Something went wrong when fetching application resources");
        }
    }

    @GetMapping("/usage/{resourceid}")
    public ResponseEntity<Long> getUsageForResourceId(@PathVariable String resourceid){

        return ResponseEntity.ok(resourceAvailabilityService.getUsage(resourceid));
    }

    @GetMapping("/admin/v1")
    public ResponseEntity<Map<String, Object>> getAllResourcesForAdmins(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "orgunits", required = false) List<String> orgUnits,
            @RequestParam(value = "validorgunits", required = false) List<String> validOrgUnits,
            @RequestParam(value = "resourceType", required = false) String resourceType,
            @RequestParam(value = "usertype", required = false) List<String> userType,
            @RequestParam(value = "accesstype", required = false) String accessType,
            @RequestParam(value = "applicationcategory", required = false) List<String> applicationCategory,
            @RequestParam(value = "status", required = false) List<String> status,
            @SortDefault(sort = {"resourceName"}, direction = Sort.Direction.ASC)
            @ParameterObject @PageableDefault(size = 100) Pageable pageable
    ) {
        try {
            Page<ApplicationResource> allApplicationResources = applicationResourceService.getAllApplicationResourcesForAdmins(
                    FintJwtEndUserPrincipal.from(jwt),
                    search,
                    orgUnits,
                    validOrgUnits,
                    resourceType,
                    userType,
                    accessType,
                    applicationCategory,
                    status,
                    pageable
            );
            if (allApplicationResources == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fetching application resources returned no resources");
            }
            return ResponseEntity.ok(applicationResourceMapper.toApplicationResourceAdminDtoPage(allApplicationResources));
        }
        catch (Exception e) {
            log.error("Error fetching application resources", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Something went wrong when fetching application resources");
        }
    }

    @PostMapping("v1")
    public ResponseEntity<HttpStatus> createApplicationResource(
            @RequestBody ApplicationResourceFrontendRequest request
    ) {
        applicationResourceService.createApplicationResource(request);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("v1")
    public ResponseEntity<HttpStatus> updateApplicationResource(
            @RequestBody ApplicationResourceFrontendRequest request
    ) throws ApplicationResourceNotFoundException {
        applicationResourceService.updateApplicationResource(request);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @DeleteMapping("v1/{id}")
    public ResponseEntity<HttpStatus> deleteApplicationResource(@PathVariable Long id) {
        try {
            applicationResourceService.deleteApplicationResource(id);
        } catch (ApplicationResourceNotFoundException applicationResourceNotFoundException) {
            log.error("Application resource not found", applicationResourceNotFoundException);

            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("admin/source/v1")
    public ResponseEntity<String> getSourceConfig() {
        String source = serviceConfiguration.getSource();

        return new ResponseEntity<>(source, HttpStatus.OK);
    }

    @OnlyDevelopers
    @PostMapping("admin/publishall")
    public ResponseEntity<HttpStatus> publishAll(
            @RequestParam(defaultValue = "false") boolean publishAll,
            @RequestParam(defaultValue = "false") boolean publishToMsGraph
    ) {

        resourceGroupPublishComponent.publishResourceGroups(publishAll, publishToMsGraph);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @OnlyDevelopers
    @PostMapping("admin/publishall-ms-graph")
    public ResponseEntity<HttpStatus> publishAllMsGraph() {
        resourceGroupPublishComponent.publishAllResourceGroupsMsGraph();

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @OnlyDevelopers
    @PostMapping("admin/publishall-ms-graph-create")
    public ResponseEntity<HttpStatus> publishAllMsGraphAsCreate() {
        resourceGroupPublishComponent.publishAllResourceGroupsMsGraphAsCreate();

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @OnlyDevelopers
    @PostMapping("admin/publishfailed-ms-graph")
    public ResponseEntity<HttpStatus> publishFailedMsGraph() {
        resourceGroupPublishComponent.publishFailedResourceGroupsMsGraph();

        return new ResponseEntity<>(HttpStatus.OK);
    }


    @GetMapping("/source/v1")
    public ResponseEntity<String> getSourceConfigPublic() {
        String source = serviceConfiguration.getSource();

        return new ResponseEntity<>(source, HttpStatus.OK);
    }

}
