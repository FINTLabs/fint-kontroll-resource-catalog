package no.fintlabs.applicationResource;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.ResponseFactory;
import no.fintlabs.applicationResourceLocation.ApplicationResourceLocation;
import no.fintlabs.authorization.AuthorizationUtil;
import no.fintlabs.cache.FintCache;
import no.fintlabs.opa.model.OrgUnitType;
import no.fintlabs.resourceGroup.AzureGroup;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;

import static no.fintlabs.opa.model.OrgUnitType.ALLORGUNITS;


@Slf4j
@Service
public class ApplicationResourceService {

    private final ApplicationResourceRepository applicationResourceRepository;
    private final FintCache<Long, AzureGroup> azureGroupCache;
    private final AuthorizationUtil authorizationUtil;
    private final ResponseFactory responseFactory;

    public ApplicationResourceService(ApplicationResourceRepository applicationResourceRepository, FintCache<Long, AzureGroup> azureGroupCache,
                                      AuthorizationUtil authorizationUtil,
                                      ResponseFactory responseFactory) {
        this.applicationResourceRepository = applicationResourceRepository;
        this.azureGroupCache = azureGroupCache;
        this.authorizationUtil = authorizationUtil;
        this.responseFactory = responseFactory;
    }


    public void save(ApplicationResource applicationResource) {
        applicationResourceRepository
                .findApplicationResourceByResourceIdEqualsIgnoreCase(applicationResource.getResourceId())
                .ifPresentOrElse(onSaveExistingApplicationResource(applicationResource),
                        onSaveNewApplicationResource(applicationResource));
    }

    private Runnable onSaveNewApplicationResource(ApplicationResource applicationResource) {
        return () -> applicationResourceRepository.save(applicationResource);
    }

    private Consumer<ApplicationResource> onSaveExistingApplicationResource(ApplicationResource applicationResource) {
        return existingApplicationResource -> {
            Long applicationResourceId = existingApplicationResource.getId();
            applicationResource.setId(applicationResourceId);

            Optional<AzureGroup> azureGroup = azureGroupCache.getOptional(applicationResourceId);

            if (!azureGroup.isEmpty()) {
                applicationResource.setIdentityProviderGroupObjectId(azureGroup.get().getId());
                applicationResource.setIdentityProviderGroupName(azureGroup.get().getDisplayName());
            }
            applicationResourceRepository.save(applicationResource);
        };
    }

    @Transactional
    public ApplicationResourceDTOFrontendDetail getApplicationResourceDTOFrontendDetailById(FintJwtEndUserPrincipal principal, Long id) {
        List<String> validOrgUnits = authorizationUtil.getAllAuthorizedOrgUnitIDs();
        ModelMapper modelMapper = new ModelMapper();

        Optional<ApplicationResource> applicationResourceOptional = applicationResourceRepository.findById(id);

        ApplicationResourceDTOFrontendDetail applicationResourceDTOFrontendDetail = applicationResourceOptional
                .map(applicationResource -> modelMapper.map(applicationResource, ApplicationResourceDTOFrontendDetail.class))
                .orElse(new ApplicationResourceDTOFrontendDetail());

        List<ApplicationResourceLocation> applicationResourceLocations = applicationResourceDTOFrontendDetail.getValidForOrgUnits();
        List<String> orgunitsInApplicationResourceLocations = new ArrayList<>();
        applicationResourceLocations.forEach(applicationResourceLocation -> {
            orgunitsInApplicationResourceLocations.add(applicationResourceLocation.getOrgUnitId());
        });

        if (validOrgUnits.contains(ALLORGUNITS.name()) || validOrgUnits.contains(applicationResourceDTOFrontendDetail.getResourceOwnerOrgUnitId())){
            return applicationResourceDTOFrontendDetail;
        }

        List<String> validatedOrgUnits = orgunitsInApplicationResourceLocations.stream()
                .filter(orgUnit -> validOrgUnits.contains(orgUnit))
                .toList();

        if (validatedOrgUnits.isEmpty()) {
            return new ApplicationResourceDTOFrontendDetail();
        } else {
            return applicationResourceDTOFrontendDetail;
        }

    }
    //validOrgUnits.contains(applicationResourceDTOFrontendDetail.getResourceOwnerOrgUnitId())

    public List<ApplicationResourceDTOFrontendList> getApplicationResourceDTOFrontendList(
            String search,
            List<String> filteredOrgUnitIds,
            String type,
            List<String> userType,
            String accessType,
            List<String> applicationCategory,
            List<String> status
    ) {
        List<String> allAuthorizedOrgUnitIds = getAllAuthorizedOrgUnitIDs();
        List<String> scopedOrgUnitIds =
                allAuthorizedOrgUnitIds.contains(OrgUnitType.ALLORGUNITS.name()) ? null : allAuthorizedOrgUnitIds;

        AppicationResourceSpesificationBuilder appicationResourceSpesification = new AppicationResourceSpesificationBuilder(
                search, scopedOrgUnitIds, filteredOrgUnitIds, type, userType, accessType, applicationCategory, status
        );

        List<ApplicationResource> applicationResourseList = applicationResourceRepository.findAll(appicationResourceSpesification.build());

        return applicationResourseList
                .stream()
                .map(ApplicationResource::toApplicationResourceDTOFrontendList)
                .toList();
    }


    public Optional<ApplicationResource> getApplicationResourceFromId(Long applicationResourceId) {

        return applicationResourceRepository.findById(applicationResourceId);
    }

    public List<ApplicationResource> getAllApplicationResources() {

        return applicationResourceRepository.findAll();
    }


    public List<String> getAllAuthorizedOrgUnitIDs() {

        return authorizationUtil.getAllAuthorizedOrgUnitIDs();
    }


    public List<String> compareRequestedOrgUnitIDsWithOPA(List<String> requestedOrgUnitIDs) {
        List<String> orgUnitsFromOPA = getAllAuthorizedOrgUnitIDs();
        if (orgUnitsFromOPA.contains(OrgUnitType.ALLORGUNITS.name())) {

            return requestedOrgUnitIDs;
        }

        return orgUnitsFromOPA.stream()
                .filter(requestedOrgUnitIDs::contains)
                .toList();
    }


    public ApplicationResource createApplicationResource(ApplicationResource applicationResource) {
        ApplicationResource newApplicationResource = applicationResourceRepository.saveAndFlush(applicationResource);
        log.info("Created new application resource: {}", newApplicationResource.getResourceId());

        return newApplicationResource;
    }


    public ApplicationResource updateApplicationResource(ApplicationResource applicationResource) {
        Optional<ApplicationResource> applicationResourceSaved = applicationResourceRepository.findById(applicationResource.getId());

        if (applicationResourceSaved.isPresent()) {
            applicationResource.setCreatedBy(applicationResourceSaved.get().getCreatedBy());
            applicationResource.setDateCreated(applicationResourceSaved.get().getDateCreated());
        }

        ApplicationResource updatedApplicationResource = applicationResourceRepository.saveAndFlush(applicationResource);

        log.info("Updated application resource: {}", updatedApplicationResource.getResourceId());

        return updatedApplicationResource;
    }

    public void deleteApplicationResource(Long id) throws ApplicationResourceNotFoundExeption {
        ApplicationResource applicationResource = applicationResourceRepository.findById(id)
                .orElseThrow(() -> new ApplicationResourceNotFoundExeption(id));

        applicationResource.setStatus("DELETED");
        applicationResource.setStatusChanged(Date.from(Instant.now()));
        applicationResourceRepository.saveAndFlush(applicationResource);


    }

    public ResponseEntity<Map<String, Object>> getAllApplicationResourcesForAdmins(
            String search,
            List<String> filteredOrgUnitIds,
            String resourceType,
            List<String> userType,
            String accessType,
            List<String> applicationCategory,
            List<String> status,
            int page,
            int size
    ) {
        List<String> scopedOrgUnitIds = authorizationUtil.getAllAuthorizedOrgUnitIDs();

        AppicationResourceSpesificationBuilder appicationResourceSpesification = new AppicationResourceSpesificationBuilder(
                search, scopedOrgUnitIds, filteredOrgUnitIds, resourceType, userType, accessType, applicationCategory, status);

        List<ApplicationResource> applicationResourceList = applicationResourceRepository.findAll(appicationResourceSpesification.build());

        List<ApplicationResourceDTOFrontendListForAdmin> applicationResourceDTOFrontendListForAdmins = applicationResourceList
                .stream()
                .map(ApplicationResource::toApplicationResourceDTOFrontendListForAdmin)
                .toList();

        ResponseEntity<Map<String,Object>> responseEntity = responseFactory.createResponsAndPaging(applicationResourceDTOFrontendListForAdmins,page,size);


        return responseEntity;
    }

    public ResponseEntity<Map<String, Object>> getAllActiveAndValidApplicationResources(
            String search,
            List<String> orgUnits,
            String resourceType,
            List<String> userType,
            String accessType,
            List<String> applicationCategory,
            int page,
            int size
    ) {
        //TODO finne rolle til bruker. Hvis bruker er tildeler, hente kun aktive ressurser. Ellers hente alle ressurser.
        // Da vil admin-endepunktene også kunne benytte getApplicationResourceDTOFrontendList

        List<String> status = List.of("ACTIVE");
        List<ApplicationResourceDTOFrontendList> applicationResourceDTOFrontendLists =
                this.getApplicationResourceDTOFrontendList(
                        search,
                        orgUnits,
                        resourceType,
                        userType,
                        accessType,
                        applicationCategory,
                        status
                );

        List<ApplicationResourceDTOFrontendList> applicationResourceDTOFrontendListFiltered = applicationResourceDTOFrontendLists
                .stream().filter(ent -> ent.getIdentityProviderGroupObjectId()!=null)
                .toList();

        ResponseEntity<Map<String,Object>> responseEntity = responseFactory.createResponsAndPaging(applicationResourceDTOFrontendListFiltered,page,size);

        return responseEntity;
    }
}
