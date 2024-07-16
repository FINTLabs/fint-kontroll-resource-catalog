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
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

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

//    public void saveApplicationResource(ApplicationResource applicationResource) {
//        System.out.println("resourceId" + applicationResource.getResourceId());
//        System.out.println("resourceName: " + applicationResource.getResourceName());
//
//    }
//
//    public void saveApplicationResources(List<ApplicationResource> applicationResources) {
//        applicationResources
//                .stream()
//                .peek(this::save);
//    }

    public void save(ApplicationResource applicationResource) {
        applicationResourceRepository
                .findApplicationResourceByResourceIdEqualsIgnoreCase(applicationResource.getResourceId())
                .ifPresentOrElse(onSaveExistingApplicationResource(applicationResource),
                                 onSaveNewApplicationResource(applicationResource));
    }

    private Runnable onSaveNewApplicationResource(ApplicationResource applicationResource) {
        return () -> {

            //resourceGroupProducerService.publish(applicationResourceRepository.save(applicationResource));
            applicationResourceRepository.save(applicationResource);

        };
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
            //resourceGroupProducerService.publish(applicationResource);
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
                search,orgUnits,type,userType,accessType,applicationCategory
        );

        //TODO: refactor to use native sql og jpl instead of findAll->stream
        List<ApplicationResource> applicationResourseList = applicationResourceRepository.findAll(appicationResourceSpesification.build());

        List<ApplicationResourceDTOFrontendList> applicationResourceDTOFrontendList = applicationResourseList
                .stream()
                .filter(applicationResource -> applicationResource.getStatus() != null)
                .filter(applicationResource -> applicationResource.getStatus().equals("ACTIVE"))
                .map(ApplicationResource::toApplicationResourceDTOFrontendList)
                .toList();
        return applicationResourceDTOFrontendList;
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
        if (orgUnitsFromOPA.contains(OrgUnitType.ALLORGUNITS.name())){
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


}
