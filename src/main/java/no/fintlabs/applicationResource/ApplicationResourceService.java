package no.fintlabs.applicationResource;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.OrgUnitType;
import no.fintlabs.applicationResourceLocation.ApplicationResourceLocation;
import no.fintlabs.applicationResourceLocation.ApplicationResourceLocationRepository;
import no.fintlabs.authorization.AuthorizationUtil;
import no.fintlabs.cache.FintCache;
import no.fintlabs.kodeverk.applikasjonskategori.Applikasjonskategori;
import no.fintlabs.kodeverk.applikasjonskategori.ApplikasjonskategoriService;
import no.fintlabs.kodeverk.handhevingstype.HandhevingstypeLabels;
import no.fintlabs.opa.OpaService;
import no.fintlabs.resourceGroup.EntraGroup;
import no.fintlabs.resourceGroup.EntraStatus;
import no.fintlabs.resourceGroup.ResourceGroup;
import no.fintlabs.resourceGroup.ResourceGroupOperation;
import no.fintlabs.resourceGroup.ResourceGroupProducerService;
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
    private final FintCache<Long, EntraGroup> entraGroupCache;
    private final AuthorizationUtil authorizationUtil;
    private final OpaService opaService;
    private final ResourceGroupProducerService resourceGroupProducerService;
    private final ApplikasjonskategoriService applikasjonskategoriService;

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
                    resolveApplicationCategories(applicationResource);
                    setPendingActiveIfIdentityProviderGroupIsMissing(applicationResource);
                    ApplicationResource newResource = applicationResourceRepository.save(applicationResource);
                    resourceGroupProducerService.publish(newResource, shouldPublishMsGraph(newResource, true));
                });
    }

    public Optional<ApplicationResource> getApplicationResourceByResourceId(String resourceId) {
        return applicationResourceRepository.findApplicationResourceByResourceIdEqualsIgnoreCase(resourceId);
    }

    private void saveExistingApplicationResource(ApplicationResource incoming) {
        ApplicationResource existingApplicationResource = applicationResourceRepository
                .findApplicationResourceByResourceIdEqualsIgnoreCase(incoming.getResourceId()).orElseThrow(()
                        -> new ApplicationResourceNotFoundException(incoming.getId()));

        ResourceGroup previousMsGraphCommand = toMsGraphCommand(existingApplicationResource);
        String previousStatus = existingApplicationResource.getStatus();
        boolean reactivatingDeletedResource = isReactivatingDeletedResource(previousStatus, incoming.getStatus());
        mapApplicationResource(incoming, existingApplicationResource);
        if (reactivatingDeletedResource) {
            prepareDeletedResourceForReactivation(existingApplicationResource);
        }
        ResourceGroup updatedMsGraphCommand = toMsGraphCommand(existingApplicationResource);
        boolean publishMsGraph = shouldPublishMsGraph(
                existingApplicationResource,
                hasMsGraphCommandChanged(previousMsGraphCommand, updatedMsGraphCommand)
                        || changedToPendingActive(previousStatus, existingApplicationResource.getStatus())
        );

        applicationResourceRepository.save(existingApplicationResource);
        resourceGroupProducerService.publish(existingApplicationResource, publishMsGraph);
    }

    private boolean shouldPublishMsGraph(ApplicationResource applicationResource, boolean publishMsGraph) {
        return publishMsGraph && !ApplicationResourceStatus.DELETED.value().equalsIgnoreCase(applicationResource.getStatus());
    }

    private boolean hasMsGraphCommandChanged(ResourceGroup previous, ResourceGroup updated) {
        return !Objects.equals(previous.getOperation(), updated.getOperation())
                || !Objects.equals(previous.getResourceId(), updated.getResourceId())
                || !Objects.equals(previous.getIdpGroupObjectId(), updated.getIdpGroupObjectId())
                || !Objects.equals(previous.getResourceName(), updated.getResourceName());
    }

    private boolean changedToPendingActive(String previousStatus, String updatedStatus) {
        return !Objects.equals(previousStatus, updatedStatus)
                && ApplicationResourceStatus.PENDING_ACTIVE.value().equals(updatedStatus);
    }

    private boolean isReactivatingDeletedResource(String previousStatus, String incomingStatus) {
        return ApplicationResourceStatus.DELETED.value().equalsIgnoreCase(previousStatus)
                && ApplicationResourceStatus.ACTIVE.value().equals(incomingStatus);
    }

    private void prepareDeletedResourceForReactivation(ApplicationResource applicationResource) {
        applicationResource.setIdentityProviderGroupObjectId(null);
        applicationResource.setIdentityProviderGroupName(null);
        applicationResource.setStatus(ApplicationResourceStatus.PENDING_ACTIVE.value());
        applicationResource.setStatusChanged(Date.from(Instant.now()));
    }

    private ResourceGroup toMsGraphCommand(ApplicationResource applicationResource) {
        return ResourceGroup.builder()
                .operation(resolveMsGraphOperation(applicationResource))
                .resourceId(applicationResource.getId().toString())
                .idpGroupObjectId(applicationResource.getIdentityProviderGroupObjectId() == null
                        ? null
                        : applicationResource.getIdentityProviderGroupObjectId().toString())
                .resourceName(applicationResource.getResourceName())
                .build();
    }

    private ResourceGroupOperation resolveMsGraphOperation(ApplicationResource applicationResource) {
        if ("DELETED".equalsIgnoreCase(applicationResource.getStatus())) {
            return ResourceGroupOperation.DELETE;
        }

        return applicationResource.getIdentityProviderGroupObjectId() == null
                ? ResourceGroupOperation.CREATE
                : ResourceGroupOperation.UPDATE;
    }

    private void mapApplicationResource(
            ApplicationResource incoming,
            ApplicationResource existingApplicationResource
    ) {
        resolveApplicationCategories(incoming);

        existingApplicationResource.setApplicationAccessType(incoming.getApplicationAccessType());
        existingApplicationResource.setApplicationAccessRole(incoming.getApplicationAccessRole());
        existingApplicationResource.setPlatform(toMutableSet(incoming.getPlatform()));
        existingApplicationResource.setAccessType(incoming.getAccessType());
        existingApplicationResource.setResourceLimit(incoming.getResourceLimit());
        existingApplicationResource.setResourceOwnerOrgUnitId(incoming.getResourceOwnerOrgUnitId());
        existingApplicationResource.setResourceOwnerOrgUnitName(incoming.getResourceOwnerOrgUnitName());
        existingApplicationResource.setLicenseEnforcement(incoming.getLicenseEnforcement());
        existingApplicationResource.setHasCost(incoming.isHasCost());
        existingApplicationResource.setUnitCost(incoming.getUnitCost());
        existingApplicationResource.setStatus(incoming.getStatus());
        existingApplicationResource.setStatusChanged(incoming.getStatusChanged());
        setPendingActiveIfIdentityProviderGroupIsMissing(existingApplicationResource);
        existingApplicationResource.setNeedApproval(incoming.isNeedApproval());
        existingApplicationResource.setValidForRoles(toMutableSet(incoming.getValidForRoles()));
        existingApplicationResource.setApplicationCategory(toMutableSet(incoming.getApplicationCategory()));
        existingApplicationResource.setResourceName(incoming.getResourceName());
        existingApplicationResource.setResourceType(incoming.getResourceType());
        updateApplicationResourceLocations(existingApplicationResource, incoming);
    }

    private void resolveApplicationCategories(ApplicationResource applicationResource) {
        Set<Applikasjonskategori> applicationCategories = applicationResource.getApplicationCategory();
        if (applicationCategories == null || applicationCategories.isEmpty()) {
            applicationResource.setApplicationCategory(new HashSet<>());
            return;
        }

        List<String> categoryNames = applicationCategories.stream()
                .map(Applikasjonskategori::getName)
                .filter(Objects::nonNull)
                .toList();

        applicationResource.setApplicationCategory(
                toMutableSet(applikasjonskategoriService.getOrCreateApplikasjonskategoriByNames(categoryNames))
        );
    }

    private static <T> Set<T> toMutableSet(Collection<T> values) {
        return values == null ? new HashSet<>() : new HashSet<>(values);
    }

    public ApplicationResourceDTOFrontendDetail getApplicationResourceDTOFrontendDetailById(Long id) {
        List<String> validOrgUnits = authorizationUtil.getAllAuthorizedOrgUnitIDs();
        ModelMapper modelMapper = new ModelMapper();

        ApplicationResource applicationResource = applicationResourceRepository.findById(id).orElseThrow(() -> new ApplicationResourceNotFoundException(id));

        ApplicationResourceDTOFrontendDetail applicationResourceDTOFrontendDetail =
                modelMapper.map(applicationResource, ApplicationResourceDTOFrontendDetail.class);
        applicationResourceDTOFrontendDetail.setApplicationCategory(ApplicationResourceMapper.toApplicationCategoryNames(applicationResource));

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

    public void saveEntraGroup(EntraGroup entraGroup) {
        if (entraGroup == null) {
            log.warn("Ignoring graph-group response without payload");
            return;
        }

        log.debug(
                "Handling graph-group response for resourceGroupId {}, objectId {}, status {}, traceId {}",
                entraGroup.getResourceGroupId(),
                entraGroup.getObjectId(),
                entraGroup.getStatus(),
                entraGroup.getTraceId()
        );

        findApplicationResourceForEntraGroup(entraGroup)
                .ifPresent(applicationResource -> {
                    EntraStatus status = resolveEntraStatus(entraGroup, applicationResource);
                    applicationResource.setEntraState(status == null ? null : status.name());

                    if (status == EntraStatus.NO_CHANGES) {
                        log.info(
                                "Received graph-group NO_CHANGES response for resourceGroupId {}, objectId {}. Keeping current catalog state unchanged. traceId={}",
                                entraGroup.getResourceGroupId(),
                                entraGroup.getObjectId(),
                                entraGroup.getTraceId()
                        );
                        return;
                    }

                    if (status == EntraStatus.ERROR || status == EntraStatus.FAILED) {
                        applicationResourceRepository.save(applicationResource);
                        log.warn(
                                "Updated application resource {} entraState to {} after graph-group response. traceId={}",
                                applicationResource.getId(),
                                status,
                                entraGroup.getTraceId()
                        );
                        return;
                    }

                    if (status == EntraStatus.DELETED) {
                        Long cacheKey = applicationResource.getId();
                        applicationResource.setIdentityProviderGroupObjectId(null);
                        applicationResource.setIdentityProviderGroupName(null);
                        applicationResourceRepository.save(applicationResource);
                        entraGroupCache.remove(cacheKey);
                        log.debug(
                                "Cleared Entra group info for application resource {} after delete response",
                                applicationResource.getId()
                        );
                        return;
                    }

                    if (entraGroup.getObjectId() == null || entraGroup.getObjectId().isBlank()) {
                        log.warn(
                                "Ignoring graph-group response for resourceGroupId {} without objectId. status={}, traceId={}",
                                entraGroup.getResourceGroupId(),
                                status,
                                entraGroup.getTraceId()
                        );
                        applicationResourceRepository.save(applicationResource);
                        return;
                    }

                    Optional<UUID> objectId = parseObjectId(entraGroup.getObjectId(), entraGroup.getTraceId());
                    if (objectId.isEmpty()) {
                        log.warn(
                                "Ignoring graph-group response for resourceGroupId {} with invalid objectId {}. status={}, traceId={}",
                                entraGroup.getResourceGroupId(),
                                entraGroup.getObjectId(),
                                status,
                                entraGroup.getTraceId()
                        );
                        applicationResourceRepository.save(applicationResource);
                        return;
                    }

                    applicationResource.setIdentityProviderGroupObjectId(objectId.get());
                    applicationResource.setIdentityProviderGroupName(entraGroup.getDisplayName());
                    activatePendingActiveStatus(applicationResource, status);
                    applicationResourceRepository.save(applicationResource);
                    Long cacheKey = applicationResource.getId();
                    if (entraGroup.getResourceGroupId() == null) {
                        entraGroup.setResourceGroupId(cacheKey);
                    }
                    entraGroupCache.put(cacheKey, entraGroup);
                });
    }

    private EntraStatus resolveEntraStatus(EntraGroup entraGroup, ApplicationResource applicationResource) {
        EntraStatus status = entraGroup.getStatus();
        if (status != EntraStatus.NO_CHANGES) {
            return status;
        }

        if (hasEntraGroupChanged(entraGroup, applicationResource)) {
            log.warn(
                    "Received graph-group NO_CHANGES response for application resource {}, but returned objectId/name differs from catalog. Treating response as UPDATED. currentObjectId={}, returnedObjectId={}, currentName={}, returnedName={}, traceId={}",
                    applicationResource.getId(),
                    applicationResource.getIdentityProviderGroupObjectId(),
                    entraGroup.getObjectId(),
                    applicationResource.getIdentityProviderGroupName(),
                    entraGroup.getDisplayName(),
                    entraGroup.getTraceId()
            );
            return EntraStatus.UPDATED;
        }

        return status;
    }

    private boolean hasEntraGroupChanged(EntraGroup entraGroup, ApplicationResource applicationResource) {
        boolean objectIdChanged = parseObjectId(entraGroup.getObjectId(), entraGroup.getTraceId())
                .map(objectId -> !Objects.equals(applicationResource.getIdentityProviderGroupObjectId(), objectId))
                .orElse(false);

        boolean groupNameChanged = entraGroup.getDisplayName() != null
                && !Objects.equals(applicationResource.getIdentityProviderGroupName(), entraGroup.getDisplayName());

        return objectIdChanged || groupNameChanged;
    }

    private Optional<ApplicationResource> findApplicationResourceForEntraGroup(EntraGroup entraGroup) {
        if (entraGroup.getResourceGroupId() != null) {
            return findApplicationResourceById(entraGroup.getResourceGroupId());
        }

        log.info(
                "graph-group response for objectId {} has no resourceGroupId. Treating it as an Entra-side reconciliation event and looking up application resource by Entra object id. traceId={}",
                entraGroup.getObjectId(),
                entraGroup.getTraceId()
        );

        Optional<UUID> objectId = parseObjectId(entraGroup.getObjectId(), entraGroup.getTraceId());
        if (objectId.isEmpty()) {
            log.warn(
                    "Ignoring graph-group response without resourceGroupId because objectId is missing or invalid. objectId={}, status={}, traceId={}",
                    entraGroup.getObjectId(),
                    entraGroup.getStatus(),
                    entraGroup.getTraceId()
            );
            return Optional.empty();
        }

        return applicationResourceRepository.findApplicationResourceByIdentityProviderGroupObjectId(objectId.get());
    }

    private Optional<UUID> parseObjectId(String objectId, String traceId) {
        if (objectId == null || objectId.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(UUID.fromString(objectId));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid graph-group objectId {}. traceId={}", objectId, traceId);
            return Optional.empty();
        }
    }

    public List<ApplicationResource> getAllApplicationResources() {
        return applicationResourceRepository.findAll();
    }

    public List<ApplicationResource> getApplicationResourcesWithFailedEntraState() {
        return applicationResourceRepository.findAllByEntraState(EntraStatus.FAILED.name());
    }

    public ApplicationResource createApplicationResource(ApplicationResource applicationResource) {
        resolveApplicationCategories(applicationResource);
        setPendingActiveIfIdentityProviderGroupIsMissing(applicationResource);
        ApplicationResource newApplicationResource = applicationResourceRepository.saveAndFlush(applicationResource);
        log.info("Created new application resource: {}", newApplicationResource.getResourceId());
        resourceGroupProducerService.publish(newApplicationResource, shouldPublishMsGraph(newApplicationResource, true));

        return newApplicationResource;
    }


    public ApplicationResource updateApplicationResource(ApplicationResource applicationResource) throws ApplicationResourceNotFoundException {
        ApplicationResource applicationResourceToUpdate = applicationResourceRepository
                .findById(applicationResource.getId())
                .orElseThrow(() -> new ApplicationResourceNotFoundException(applicationResource.getId()));

        String currentStatus = applicationResourceToUpdate.getStatus();
        String updatedStatus = ApplicationResourceStatus.pendingActiveUntilIdentityProviderGroupExists(
                applicationResource.getStatus(),
                applicationResourceToUpdate.getIdentityProviderGroupObjectId() != null
        );
        applicationResource.setStatus(updatedStatus);
        applicationResource.setStatusChanged(Objects.equals(currentStatus, updatedStatus)
                ? applicationResourceToUpdate.getStatusChanged()
                : Date.from(Instant.now()));

        saveExistingApplicationResource(applicationResource);
        ApplicationResource updatedApplicationResource = applicationResourceRepository
                .findApplicationResourceByResourceIdEqualsIgnoreCase(applicationResource.getResourceId()).orElseThrow(() -> new ApplicationResourceNotFoundException(applicationResource.getId()));

        log.info("Updated application resource: {}", updatedApplicationResource.getResourceId());


        return updatedApplicationResource;
    }

    private void setPendingActiveIfIdentityProviderGroupIsMissing(ApplicationResource applicationResource) {
        applicationResource.setStatus(ApplicationResourceStatus.pendingActiveUntilIdentityProviderGroupExists(
                applicationResource.getStatus(),
                applicationResource.getIdentityProviderGroupObjectId() != null
        ));
    }

    private void activatePendingActiveStatus(ApplicationResource applicationResource, EntraStatus entraStatus) {
        if ((entraStatus == EntraStatus.CREATED || entraStatus == EntraStatus.UPDATED)
                && ApplicationResourceStatus.PENDING_ACTIVE.value().equals(applicationResource.getStatus())) {
            applicationResource.setStatus(ApplicationResourceStatus.ACTIVE.value());
            applicationResource.setStatusChanged(Date.from(Instant.now()));
        }
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
                existing.setTopOrgunit(updated.isTopOrgunit());

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
            newLocation.setTopOrgunit(location.isTopOrgunit());
            newLocation.setApplicationResource(applicationResourceToUpdate);

            existingLocations.add(newLocation);
        }
    }


    public void deleteApplicationResource(Long id) throws ApplicationResourceNotFoundException {
        ApplicationResource applicationResource = applicationResourceRepository.findById(id)
                .orElseThrow(() -> new ApplicationResourceNotFoundException(id));

        applicationResource.setStatus("DELETED");
        applicationResource.setStatusChanged(Date.from(Instant.now()));
        ApplicationResource deletedApplicationResource = applicationResourceRepository.saveAndFlush(applicationResource);
        resourceGroupProducerService.publish(deletedApplicationResource, false);
    }

    public Page<ApplicationResource> getAllApplicationResourcesForAdmins(
            FintJwtEndUserPrincipal jwtEndUserPrincipal,
            String search,
            List<String> orgunits,
            List<String> validForOrgUnits,
            String resourceType,
            List<String> userTypes,
            String accessType,
            List<String> applicationCategories,
            List<String> statusList,
            Pageable pageable
    ) {

        return searchApplicationResources(
                jwtEndUserPrincipal,
                search,
                orgunits,
                validForOrgUnits,
                resourceType,
                userTypes,
                accessType,
                applicationCategories,
                statusList,
                pageable,
                true
        );
    }

    public Page<ApplicationResource> searchApplicationResources(
            FintJwtEndUserPrincipal principal,
            String searchString,
            List<String> orgUnits,
            List<String> validOrgUnits,
            String resourceType,
            List<String> userType,
            String accessType,
            List<String> applicationCategory,
            List<String> statusList,
            Pageable pageable,
            boolean forAdmins
    ) {
        List<String> orgUnitsInScope = opaService.getOrgUnitsInScope("resource");
        log.info("Org units returned from scope: {}", orgUnitsInScope);

        List<String> validOrgUnitsInScope = getOrgUnitsValidAndInScope(orgUnitsInScope, validOrgUnits);

        Set<Long> accessableRestrictedResourceIds = new HashSet<>();

        if (!orgUnitsInScope.contains(OrgUnitType.ALLORGUNITS.name())) {
            Optional<Set<Long>> optionalestrictedResourcesForOrgUnitsInScope = getRestrictedResourcesForOrgUnitsInScope(validOrgUnitsInScope);

            if (optionalestrictedResourcesForOrgUnitsInScope.isPresent()) {
                accessableRestrictedResourceIds = optionalestrictedResourcesForOrgUnitsInScope.get();
                log.info("Restricted resources accessable for {} found: {}", principal.getMail(), accessableRestrictedResourceIds);
            }
        }
        boolean hasAccessAllToAppResources = orgUnitsInScope.contains(OrgUnitType.ALLORGUNITS.name());
        Specification<ApplicationResource>  applicationResourceSpecification =
                Specification.where(ApplicationResourceSpecification.hasNameLike(searchString))
                        .and(ApplicationResourceSpecification.isAccessable(hasAccessAllToAppResources, accessableRestrictedResourceIds))
                        .and(ApplicationResourceSpecification.userTypeLike(userType))
                        .and(ApplicationResourceSpecification.accessTypeLike(accessType))
                        .and(ApplicationResourceSpecification.applicationCategoryLike(applicationCategory))
                        .and(ApplicationResourceSpecification.statuslike(statusList));

        if (orgUnits != null && !orgUnits.isEmpty()) {
            applicationResourceSpecification = applicationResourceSpecification.and(ApplicationResourceSpecification.isInFilteredOrgUnits(orgUnits));
        } else if (!forAdmins) {
            applicationResourceSpecification = applicationResourceSpecification.and(ApplicationResourceSpecification.hasApplicationResourceLocation());
        }

        return applicationResourceRepository.findAll(applicationResourceSpecification, pageable);
    }

    public Optional<Set<Long>> getRestrictedResourcesForOrgUnitsInScope(List<String> orgUnitsInScope) {
        return Optional.of(applicationResourceLocationRepository.getDistinctByOrgUnitIdIsIn(orgUnitsInScope)
                .stream()
                .map(location -> location.getApplicationResource().getId())
                .collect(Collectors.toSet())
        );
    }

    public static List<String> getOrgUnitsValidAndInScope(List<String> orgUnitsInScope, List<String> validOrgUnits) {
        log.debug("Getting intersection of {} and {}", orgUnitsInScope,  validOrgUnits);
        if (validOrgUnits ==null || validOrgUnits.isEmpty()) {
            log.debug("No valid orgUnits found, returning org units in scope");
            return orgUnitsInScope;
        }
        if (orgUnitsInScope.contains(OrgUnitType.ALLORGUNITS.name())) {
            log.debug("org unit scope contains ALLORGUNITS, returning valid orgUnits");
            return validOrgUnits;
        }
        List<String> intersection = new ArrayList<>(orgUnitsInScope);
        intersection.retainAll(validOrgUnits);
        log.debug("Both orgUnitsInScope and validOrgUnits are non empty subsets. Returning the actual intersection");
        return intersection;
    }
}
