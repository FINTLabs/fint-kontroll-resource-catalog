package no.fintlabs.resource;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.ServiceConfiguration;
import no.fintlabs.applicationResource.*;
import no.fintlabs.kodeverk.brukertype.BrukertypeService;
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

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/api/resources")
public class ResourceController {
    private final ApplicationResourceService applicationResourceService;
    private final ApplicationCategoryService applicationCategoryService;
    private final AccessTypeService accessTypeService;
    private final ServiceConfiguration serviceConfiguration;


    public ResourceController(
            ApplicationResourceService applicationResourceService,
            ApplicationCategoryService applicationCategoryService,
            AccessTypeService accessTypeService,
            BrukertypeService brukertypeService,
            ServiceConfiguration serviceConfiguration) {
        this.applicationResourceService = applicationResourceService;
        this.applicationCategoryService = applicationCategoryService;
        this.accessTypeService = accessTypeService;
        this.serviceConfiguration = serviceConfiguration;
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResourceDTOFrontendDetail> getApplicationResourceById(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        log.info("Fetching applicationResourse by id: {}", id);
        ApplicationResourceDTOFrontendDetail applicationResourceDTOFrontendDetail = applicationResourceService
                .getApplicationResourceDTOFrontendDetailById(id);
        if (applicationResourceDTOFrontendDetail !=null) {
            return new ResponseEntity<>(applicationResourceDTOFrontendDetail, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/applicationcategories")
    public ResponseEntity<List<String>> getApplicationCategories() {
        List<String> applicationCategories = applicationCategoryService.getAllApplicationCategories();

        if (!applicationCategories.isEmpty()) {
            return new ResponseEntity<>(applicationCategories, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
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
                    resourceType,
                    userType,
                    accessType,
                    applicationCategory,
                    List.of("ACTIVE"),
                    pageable
            );
            if (allApplicationResources == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fetching application resources returned no resources");
            }
            return ResponseEntity.ok(ApplicationResourceMapper.toApplicationResourceDtoPage(allApplicationResources));
        }
        catch (Exception e) {
            log.error("Error fetching application resources", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Something went wrong when fetching application resources");
        }
    }

    @GetMapping("/admin/v1")
    public ResponseEntity<Map<String, Object>> getAllResourcesForAdmins(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "orgunits", required = false) List<String> orgUnits,
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
            return ResponseEntity.ok(ApplicationResourceMapper.toApplicationResourceAdminDtoPage(allApplicationResources));
        }
        catch (Exception e) {
            log.error("Error fetching application resources", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Something went wrong when fetching application resources");
        }
    }

    @PostMapping("v1")
    public ResponseEntity<HttpStatus> createApplicationResource(@AuthenticationPrincipal Jwt jwt, @RequestBody ApplicationResource request) {
        FintJwtEndUserPrincipal principal = FintJwtEndUserPrincipal.from(jwt);

        ApplicationResource applicationResource = ApplicationResource.builder()
                .resourceId(UUID.randomUUID().toString())
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
                .hasCost(request.isHasCost())
                .validForOrgUnits(request.getValidForOrgUnits())
                .build();

        ApplicationResource newApplicationResource = applicationResourceService.createApplicationResource(applicationResource);
        if (newApplicationResource != null) {
            return new ResponseEntity<>(HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @PutMapping("v1")
    public ResponseEntity<HttpStatus> updateApplicationResource(@RequestBody ApplicationResource request) throws ApplicationResourceNotFoundException {
        ApplicationResource applicationResource = ApplicationResource.builder()
                .id(request.id)
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
                .hasCost(request.isHasCost())
                .validForOrgUnits(request.getValidForOrgUnits())
                .build();

        ApplicationResource updateApplicationResource = applicationResourceService.updateApplicationResource(applicationResource);

        if (updateApplicationResource != null) {
            log.info("Resource updated: {}", updateApplicationResource.getResourceId());
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        } else {
            return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        }
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

    @GetMapping("/source/v1")
    public ResponseEntity<String> getSourceConfigPublic() {
        String source = serviceConfiguration.getSource();

        return new ResponseEntity<>(source, HttpStatus.OK);
    }

}
