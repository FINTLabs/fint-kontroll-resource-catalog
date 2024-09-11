package no.fintlabs.applicationResource;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.applicationResourceLocation.ApplicationResourceLocation;
import no.fintlabs.authorization.AuthorizationUtil;
import no.fintlabs.cache.FintCache;
import no.fintlabs.opa.model.OrgUnitType;
import no.fintlabs.resourceGroup.AzureGroup;
import no.fintlabs.resourceGroup.ResourceGroupProducerService;
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
    private final ResourceGroupProducerService resourceGroupProducerService;
    private final FintCache<Long, AzureGroup> azureGroupCache;
    private final AuthorizationUtil authorizationUtil;

    public ApplicationResourceService(ApplicationResourceRepository applicationResourceRepository,
                                      ResourceGroupProducerService resourceGroupProducerService,
                                      FintCache<Long, AzureGroup> azureGroupCache, AuthorizationUtil authorizationUtil) {
        this.applicationResourceRepository = applicationResourceRepository;
        this.resourceGroupProducerService = resourceGroupProducerService;
        this.azureGroupCache = azureGroupCache;
        this.authorizationUtil = authorizationUtil;
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

        List<String> validatedOrgUnits = orgunitsInApplicationResourceLocations.stream()
                .filter(orgUnit -> validOrgUnits.contains(ALLORGUNITS.name()) || validOrgUnits.contains(orgUnit))
                .toList();

        if (validatedOrgUnits.isEmpty()) {
            return new ApplicationResourceDTOFrontendDetail();
        } else {
            return applicationResourceDTOFrontendDetail;
        }
    }


    public List<ApplicationResourceDTOFrontendList> getApplicationResourceDTOFrontendList(FintJwtEndUserPrincipal principal,
                                                                                          String search,
                                                                                          List<String> orgUnits) {
        List<String> validOrgUnits = authorizationUtil.getAllAuthorizedOrgUnitIDs();

        List<String> orgUnitsToQuery = orgUnits.stream()
                .filter(orgUnit -> orgUnit.contains(ALLORGUNITS.name()) || validOrgUnits.contains(orgUnit))
                .toList();

        List<ApplicationResource> applicationResources =
                applicationResourceRepository.findApplicationResourceByOrgUnitIds(search, orgUnitsToQuery);

        return applicationResources
                .stream()
                .filter(applicationResource -> applicationResource.getStatus() != null)
                .filter(applicationResource -> applicationResource.getStatus().equals("ACTIVE"))
                .map(ApplicationResource::toApplicationResourceDTOFrontendList)
                .toList();
    }

    public List<ApplicationResourceDTOFrontendList> getApplicationResourceDTOFrontendList(FintJwtEndUserPrincipal principal,
                                                                                          String search) {
        List<String> validOrgUnits = authorizationUtil.getAllAuthorizedOrgUnitIDs();

        List<ApplicationResource> applicationResources;

        //TODO: refactor to use native sql og jpl instead of findAll->stream
        if (validOrgUnits.contains(ALLORGUNITS.name())) {
            applicationResources = applicationResourceRepository.findApplicationResourceByResourceName(search);
        } else {
            applicationResources = applicationResourceRepository.findApplicationResourceByOrgUnitIds(search, validOrgUnits);
        }

        log.info("Fetching applicationResources. Count: " + applicationResources.size());

        return applicationResources
                .stream()
                .filter(applicationResource -> applicationResource.getStatus() != null)
                .filter(applicationResource -> applicationResource.getStatus().equals("ACTIVE"))
                .map(ApplicationResource::toApplicationResourceDTOFrontendList)
                .toList();
    }

    // new for V1
    public List<ApplicationResourceDTOFrontendList> getApplicationResourceDTOFrontendList(
            FintJwtEndUserPrincipal from,
            String search,
            List<String> orgUnits,
            String type,
            List<String> userType,
            String accessType,
            List<String> applicationCategory) {
        AppicationResourceSpesificationBuilder appicationResourceSpesification = new AppicationResourceSpesificationBuilder(
                search, orgUnits, type, userType, accessType, applicationCategory
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

    public ResponseEntity<Map<String,Object>> getAllApplicationResourcesForAdmins(
            String search,
            List<String> orgUnits,
            String resourceType,
            List<String> userType,
            String accessType,
            List<String> applicationCategory,
            List<String> status) {

    return null;
    }
}
