package no.fintlabs.applicationResource;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.OrgUnitType;
import no.fintlabs.ResponseFactory;
import no.fintlabs.applicationResourceLocation.ApplicationResourceLocation;
import no.fintlabs.applicationResourceLocation.ApplicationResourceLocationRepository;
import no.fintlabs.authorization.AuthorizationUtil;
import no.fintlabs.cache.FintCache;
import no.fintlabs.kodeverk.handhevingstype.HandhevingstypeLabels;
import no.fintlabs.opa.OpaService;
import no.fintlabs.resourceGroup.AzureGroup;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static no.fintlabs.OrgUnitType.ALLORGUNITS;


@Slf4j
@Service
public class ApplicationResourceService {

    private final ApplicationResourceRepository applicationResourceRepository;
    private final ApplicationResourceLocationRepository applicationResourceLocationRepository;
    private final FintCache<Long, AzureGroup> azureGroupCache;
    private final AuthorizationUtil authorizationUtil;
    private final ResponseFactory responseFactory;
    private final OpaService opaService;
    //private final ApplicationResourceLocationService applicationResourceLocationService;

    public ApplicationResourceService(
        ApplicationResourceRepository applicationResourceRepository,
        ApplicationResourceLocationRepository applicationResourceLocationRepository,
        FintCache<Long, AzureGroup> azureGroupCache,
        AuthorizationUtil authorizationUtil,
        ResponseFactory responseFactory,
        OpaService opaService
    ) {
        this.applicationResourceRepository = applicationResourceRepository;
        this.applicationResourceLocationRepository = applicationResourceLocationRepository;
        this.azureGroupCache = azureGroupCache;
        this.authorizationUtil = authorizationUtil;
        this.responseFactory = responseFactory;
        this.opaService = opaService;
        //this.applicationResourceLocationService = applicationResourceLocationService;
    }
    public void save(ApplicationResource applicationResource) {
        log.info("Trying to save application resource {} with resourceId {}", applicationResource.getResourceName(), applicationResource.getResourceId());

        Optional<ApplicationResource> returnedApplicationResource = getApplicationResource(applicationResource);

        if (returnedApplicationResource.isPresent()) {
            log.info("Application resource with resourceId {} already exists. Updating existing resource", applicationResource.getResourceId());
            onSaveExistingApplicationResource(applicationResource);
            return;
        }
        log.info("Application resource with resourceId {} does not exist. Saving new resource", applicationResource.getResourceId());
        ApplicationResource newApplicationResource = onSaveNewApplicationResource(applicationResource);
    }

    public Optional<ApplicationResource> getApplicationResourceByResourceId(String resourceId) {
        return applicationResourceRepository.getApplicationResourceByResourceId(resourceId);
    }

    private Optional<ApplicationResource> getApplicationResource(ApplicationResource applicationResource) {
        Optional<ApplicationResource> returnedApplicationResource = applicationResourceRepository
                .findApplicationResourceByResourceIdEqualsIgnoreCase(applicationResource.getResourceId());
        return returnedApplicationResource;
    }

    private ApplicationResource onSaveNewApplicationResource(ApplicationResource applicationResource) {
        return applicationResourceRepository.save(applicationResource);
    }

    private void onSaveExistingApplicationResource(ApplicationResource applicationResource) {

            ApplicationResource existingApplicationResource = applicationResourceRepository
                    .findApplicationResourceByResourceIdEqualsIgnoreCase(applicationResource.getResourceId()).get();

            Long applicationResourceId = existingApplicationResource.getId();

            applicationResource.setId(applicationResourceId);

            if (existingApplicationResource.getIdentityProviderGroupObjectId() != null) {
                applicationResource.setIdentityProviderGroupObjectId(existingApplicationResource.getIdentityProviderGroupObjectId());
            }
            if (existingApplicationResource.getIdentityProviderGroupName() != null) {
                applicationResource.setIdentityProviderGroupName(existingApplicationResource.getIdentityProviderGroupName());
            }
            Optional<AzureGroup> azureGroup = azureGroupCache.getOptional(applicationResourceId);

            if (azureGroup.isPresent()) {
                applicationResource.setIdentityProviderGroupObjectId(azureGroup.get().getId());
                applicationResource.setIdentityProviderGroupName(azureGroup.get().getDisplayName());
            }
            applicationResourceRepository.save(applicationResource);
    }

    @Transactional
    public ApplicationResourceDTOFrontendDetail getApplicationResourceDTOFrontendDetailById(FintJwtEndUserPrincipal principal, Long id) {
        List<String> validOrgUnits = authorizationUtil.getAllAuthorizedOrgUnitIDs();
        ModelMapper modelMapper = new ModelMapper();

        Optional<ApplicationResource> applicationResourceOptional = applicationResourceRepository.findById(id);

//        ApplicationResourceDTOFrontendDetail applicationResourceDTOFrontendDetail = applicationResourceOptional
//                .map(applicationResource -> modelMapper.map(applicationResource, ApplicationResourceDTOFrontendDetail.class))
//                .orElse(new ApplicationResourceDTOFrontendDetail());

        if (applicationResourceOptional.isEmpty()) {
            return null;
        }
        ApplicationResourceDTOFrontendDetail applicationResourceDTOFrontendDetail =
                modelMapper.map(applicationResourceOptional.get(),ApplicationResourceDTOFrontendDetail.class);

        List<ApplicationResourceLocation> applicationResourceLocations = applicationResourceDTOFrontendDetail.getValidForOrgUnits();
        List<String> orgunitsInApplicationResourceLocations = new ArrayList<>();
        applicationResourceLocations.forEach(applicationResourceLocation -> {
            orgunitsInApplicationResourceLocations.add(applicationResourceLocation.getOrgUnitId());
        });

        String licenseEnforcement = applicationResourceDTOFrontendDetail.getLicenseEnforcement();
        if (validOrgUnits.contains(ALLORGUNITS.name())
                || validOrgUnits.contains(applicationResourceDTOFrontendDetail.getResourceOwnerOrgUnitId())
                || licenseEnforcement != null && isLicenseEnforcementIsUnRestricted(licenseEnforcement)
        ){
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

    private boolean isLicenseEnforcementIsUnRestricted(String licenseEnforcementType) {
        Set<String > unlimitedLicenceEnforcementTypes = Set.of(
                HandhevingstypeLabels.NOTSET.name(),
                HandhevingstypeLabels.FREEALL.name(),
                HandhevingstypeLabels.FREEEDU.name(),
                HandhevingstypeLabels.FREESTUDENT.name());

        return unlimitedLicenceEnforcementTypes.contains(licenseEnforcementType);
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

    public Page<ApplicationResource> getAllApplicationResourcesForAdmins(
            FintJwtEndUserPrincipal jwtEndUserPrincipal,
            String search,
            List<String> orgunits,
            String resourceType,
            List<String> userTypes,
            String accessType,
            List<String> applicationCategories,
            List<String> statusList,
            Pageable pageable
    ) {

        Page<ApplicationResource> applicationResourcePage = searchApplicationResources(
                jwtEndUserPrincipal,
                search,
                orgunits,
                resourceType,
                userTypes,
                accessType,
                applicationCategories,
                statusList,
                pageable
        );
        return applicationResourcePage;
    }

    public Page<ApplicationResource> searchApplicationResources(
            FintJwtEndUserPrincipal principal,
            String searchString,
            List<String> orgUnits,
            String resourceType,
            List<String> userType,
            String accessType,
            List<String> applicationCategory,
            List<String> statusList,
            Pageable pageable
    ) {
        List<String> orgUnitsInScope = opaService.getOrgUnitsInScope("resource");
        log.info("Org units returned from scope: {}", orgUnitsInScope);

        Set<Long> accessableRestrictedResourceIds = new HashSet<>();

        if (!orgUnitsInScope.contains(OrgUnitType.ALLORGUNITS.name())) {
            Optional<Set<Long>> optionalestrictedResourcesForOrgUnitsInScope = getRestrictedResourcesForOrgUnitsInScope(orgUnitsInScope);

            if (optionalestrictedResourcesForOrgUnitsInScope.isPresent()) {
                accessableRestrictedResourceIds = optionalestrictedResourcesForOrgUnitsInScope.get();
                log.info("Restricted resources accessable for {} found: {}", principal.getMail(), accessableRestrictedResourceIds);
            }
        }
        boolean hasAccessAllToAppResources = orgUnitsInScope.contains(OrgUnitType.ALLORGUNITS.name());

        Specification<ApplicationResource> applicationResourceSpecification =
                Specification.where(ApplicationResourceSpecification.hasNameLike(searchString)
                        .and(ApplicationResourceSpecification.isAccessable(hasAccessAllToAppResources,accessableRestrictedResourceIds))
                        .and(ApplicationResourceSpecification.isInFilteredOrgUnits(orgUnits))
                        .and(ApplicationResourceSpecification.userTypeLike(userType))
                        .and(ApplicationResourceSpecification.accessTypeLike(accessType))
                        .and(ApplicationResourceSpecification.applicationCategoryLike(applicationCategory))
                        .and(ApplicationResourceSpecification.statuslike(statusList))
                );

        return applicationResourceRepository.findAll(applicationResourceSpecification, pageable);
    }

    public Optional<Set<Long>> getRestrictedResourcesForOrgUnitsInScope(List<String> orgUnitsInScope) {
        return Optional.of(applicationResourceLocationRepository.getDistinctByOrOrgUnitIdIsIn(orgUnitsInScope)
                .stream()
                .map(ApplicationResourceLocation::getResourceRef)
                .collect(Collectors.toSet())
        );
    }
}
