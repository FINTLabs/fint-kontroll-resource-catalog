package no.fintlabs.applicationResource;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import no.fintlabs.OrgUnitType;
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
import java.util.*;
import java.util.stream.Collectors;

import static no.fintlabs.OrgUnitType.ALLORGUNITS;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ApplicationResourceService {

    private final ApplicationResourceRepository applicationResourceRepository;
    private final ApplicationResourceLocationRepository applicationResourceLocationRepository;
    private final FintCache<Long, AzureGroup> azureGroupCache;
    private final AuthorizationUtil authorizationUtil;
    private final OpaService opaService;

    public void save(ApplicationResource applicationResource) {
        String resourceId = applicationResource.getResourceId();
        log.info("Trying to save application resource {} with resourceId {}",
                applicationResource.getResourceName(), resourceId);

        getApplicationResourceByResourceId(resourceId)
                .ifPresentOrElse(existing -> {
                    log.info("Application resource with resourceId {} already exists. Updating existing resource", resourceId);
                    saveExistingApplicationResource(applicationResource);
                }, () -> {
                    log.info("Application resource with resourceId {} does not exist. Saving new resource", resourceId);
                    applicationResourceRepository.save(applicationResource);
                });
    }

    public Optional<ApplicationResource> getApplicationResourceByResourceId(String resourceId) {
        return applicationResourceRepository.findApplicationResourceByResourceIdEqualsIgnoreCase(resourceId);
    }

    private void saveExistingApplicationResource(ApplicationResource applicationResource) {

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

    public ApplicationResourceDTOFrontendDetail getApplicationResourceDTOFrontendDetailById(Long id) {
        List<String> validOrgUnits = authorizationUtil.getAllAuthorizedOrgUnitIDs();
        ModelMapper modelMapper = new ModelMapper();

        ApplicationResource applicationResource = applicationResourceRepository.findById(id).orElseThrow(() -> new ApplicationResourceNotFoundException(id));

        ApplicationResourceDTOFrontendDetail applicationResourceDTOFrontendDetail =
                modelMapper.map(applicationResource, ApplicationResourceDTOFrontendDetail.class);

        List<ApplicationResourceLocation> applicationResourceLocations = applicationResourceDTOFrontendDetail.getValidForOrgUnits();
        List<String> orgunitsInApplicationResourceLocations = new ArrayList<>();
        applicationResourceLocations.forEach(applicationResourceLocation -> {
            orgunitsInApplicationResourceLocations.add(applicationResourceLocation.getOrgUnitId());
        });

        String licenseEnforcement = applicationResourceDTOFrontendDetail.getLicenseEnforcement();
        if (validOrgUnits.contains(ALLORGUNITS.name())
                || validOrgUnits.contains(applicationResourceDTOFrontendDetail.getResourceOwnerOrgUnitId())
                || licenseEnforcement != null && isLicenseEnforcementUnrestricted(licenseEnforcement)
        ) {
            return applicationResourceDTOFrontendDetail;
        }

        List<String> validatedOrgUnits = orgunitsInApplicationResourceLocations.stream()
                .filter(validOrgUnits::contains)
                .toList();

        if (validatedOrgUnits.isEmpty()) {
            return new ApplicationResourceDTOFrontendDetail();
        } else {
            return applicationResourceDTOFrontendDetail;
        }
    }

    private boolean isLicenseEnforcementUnrestricted(String licenseEnforcementType) {
        Set<String> unlimitedLicenceEnforcementTypes = Set.of(
                HandhevingstypeLabels.NOTSET.name(),
                HandhevingstypeLabels.FREEALL.name(),
                HandhevingstypeLabels.FREEEDU.name(),
                HandhevingstypeLabels.FREESTUDENT.name());

        return unlimitedLicenceEnforcementTypes.contains(licenseEnforcementType);
    }

    public Optional<ApplicationResource> findApplicationResourceById(Long applicationResourceId) {
        return applicationResourceRepository.findById(applicationResourceId);
    }

    public List<ApplicationResource> getAllApplicationResources() {
        return applicationResourceRepository.findAll();
    }

    public ApplicationResource createApplicationResource(ApplicationResource applicationResource) {
        ApplicationResource newApplicationResource = applicationResourceRepository.saveAndFlush(applicationResource);
        log.info("Created new application resource: {}", newApplicationResource.getResourceId());

        return newApplicationResource;
    }


    public ApplicationResource updateApplicationResource(ApplicationResource applicationResource) throws ApplicationResourceNotFoundException {
        ApplicationResource applicationResourceToUpdate = applicationResourceRepository
                .findById(applicationResource.getId())
                .orElseThrow(() -> new ApplicationResourceNotFoundException(applicationResource.getId()));

        applicationResourceToUpdate.setApplicationAccessType(applicationResource.getApplicationAccessType());
        applicationResourceToUpdate.setApplicationAccessRole(applicationResource.getApplicationAccessRole());
        applicationResourceToUpdate.setPlatform(applicationResource.getPlatform());
        applicationResourceToUpdate.setAccessType(applicationResource.getAccessType());
        applicationResourceToUpdate.setResourceLimit(applicationResource.getResourceLimit());
        applicationResourceToUpdate.setResourceOwnerOrgUnitId(applicationResource.getResourceOwnerOrgUnitId());
        applicationResourceToUpdate.setResourceOwnerOrgUnitName(applicationResource.getResourceOwnerOrgUnitName());
        applicationResourceToUpdate.setLicenseEnforcement(applicationResource.getLicenseEnforcement());
        applicationResourceToUpdate.setHasCost(applicationResource.isHasCost());
        applicationResourceToUpdate.setUnitCost(applicationResource.getUnitCost());
        applicationResourceToUpdate.setStatus(applicationResource.getStatus());
        applicationResourceToUpdate.setStatusChanged(applicationResource.getStatusChanged());
        applicationResourceToUpdate.setNeedApproval(applicationResource.isNeedApproval());
        applicationResourceToUpdate.setValidForRoles(applicationResource.getValidForRoles());
        applicationResourceToUpdate.setApplicationCategory(applicationResource.getApplicationCategory());

        updateApplicationResourceLocations(applicationResourceToUpdate, applicationResource);

        ApplicationResource updatedApplicationResource = applicationResourceRepository.saveAndFlush(applicationResourceToUpdate);

        log.info("Updated application resource: {}", updatedApplicationResource.getResourceId());

        return updatedApplicationResource;
    }

    private void updateApplicationResourceLocations(ApplicationResource applicationResourceToUpdate, ApplicationResource applicationResource) {
        Set<ApplicationResourceLocation> existingLocations = applicationResourceToUpdate.getValidForOrgUnits();
        Set<ApplicationResourceLocation> newLocations = applicationResource.getValidForOrgUnits();

        Map<String, ApplicationResourceLocation> newLocationsByOrgUnitId = newLocations.stream()
                .collect(Collectors.toMap(ApplicationResourceLocation::getOrgUnitId, location -> location));

        Iterator<ApplicationResourceLocation> iterator = existingLocations.iterator();
        while (iterator.hasNext()) {
            ApplicationResourceLocation existing = iterator.next();
            ApplicationResourceLocation updated = newLocationsByOrgUnitId.get(existing.getOrgUnitId());
            if (updated != null) {
                existing.setResourceLimit(updated.getResourceLimit());
                existing.setResourceName(updated.getResourceName());
                existing.setOrgUnitName(updated.getOrgUnitName());

                newLocationsByOrgUnitId.remove(existing.getOrgUnitId());
            } else {
                iterator.remove();
            }
        }

        for (ApplicationResourceLocation location : newLocationsByOrgUnitId.values()) {
            ApplicationResourceLocation newLocation = new ApplicationResourceLocation();
            newLocation.setOrgUnitId(location.getOrgUnitId());
            newLocation.setResourceId(location.getResourceId());
            newLocation.setResourceLimit(location.getResourceLimit());
            newLocation.setResourceName(location.getResourceName());
            newLocation.setOrgUnitName(location.getOrgUnitName());
            newLocation.setApplicationResource(applicationResourceToUpdate);

            existingLocations.add(newLocation);
        }
    }




    public void deleteApplicationResource(Long id) throws ApplicationResourceNotFoundException {
        ApplicationResource applicationResource = applicationResourceRepository.findById(id)
                .orElseThrow(() -> new ApplicationResourceNotFoundException(id));

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
                        .and(ApplicationResourceSpecification.isAccessable(hasAccessAllToAppResources, accessableRestrictedResourceIds))
                        .and(ApplicationResourceSpecification.isInFilteredOrgUnits(orgUnits))
                        .and(ApplicationResourceSpecification.userTypeLike(userType))
                        .and(ApplicationResourceSpecification.accessTypeLike(accessType))
                        .and(ApplicationResourceSpecification.applicationCategoryLike(applicationCategory))
                        .and(ApplicationResourceSpecification.statuslike(statusList))
                );

        return applicationResourceRepository.findAll(applicationResourceSpecification, pageable);
    }

    public Optional<Set<Long>> getRestrictedResourcesForOrgUnitsInScope(List<String> orgUnitsInScope) {
        return Optional.of(applicationResourceLocationRepository.getDistinctByOrgUnitIdIsIn(orgUnitsInScope)
                .stream()
                .map(location -> location.getApplicationResource().getId())
                .collect(Collectors.toSet())
        );
    }
}
