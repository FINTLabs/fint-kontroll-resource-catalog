package no.fintlabs.applicationResource;

import no.fintlabs.kodeverk.applikasjonskategori.Applikasjonskategori;
import no.fintlabs.resourceAvailability.ResourceAvailabilityService;
import org.springframework.data.domain.Page;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ApplicationResourceMapper {

    public static ApplicationResource toApplicationResource(
            ApplicationResourceFrontendRequest request,
            Set<Applikasjonskategori> applicationCategories
    ) {
        return ApplicationResource.builder()
                .id(request.getId())
                .resourceId(request.getResourceId())
                .resourceName(request.getResourceName())
                .resourceType(request.getResourceType())
                .platform(toMutableSet(request.getPlatform()))
                .accessType(request.getAccessType())
                .resourceLimit(request.getResourceLimit())
                .resourceOwnerOrgUnitId(request.getResourceOwnerOrgUnitId())
                .resourceOwnerOrgUnitName(request.getResourceOwnerOrgUnitName())
                .validForRoles(toMutableSet(request.getValidForRoles()))
                .applicationCategory(toMutableSet(applicationCategories))
                .licenseEnforcement(request.getLicenseEnforcement())
                .unitCost(request.getUnitCost())
                .status(request.getStatus())
                .hasCost(request.isHasCost())
                .needApproval(request.isNeedApproval())
                .validForOrgUnits(toMutableSet(request.getValidForOrgUnits()))
                .build();
    }

    private static <T> Set<T> toMutableSet(Collection<T> values) {
        return values == null ? new HashSet<>() : new HashSet<>(values);
    }

    public static Map<String, Object> toApplicationResourceDtoPage(
            Page<ApplicationResource> applicationResourcePage,
            ResourceAvailabilityService resourceAvailabilityService
    ) {
        return Map.of(
                "resources",
                    applicationResourcePage.getContent()
                        .stream()
                        .map(applicationResource -> toApplicationResourceDto(applicationResource, resourceAvailabilityService))
                        .collect(Collectors.toList()),
                "currentPage", applicationResourcePage.getNumber(),
                "totalPages", applicationResourcePage.getTotalPages(),
                "size", applicationResourcePage.getSize(),
                "totalItems", applicationResourcePage.getTotalElements()
        );
    }
    public static ApplicationResourceDTOFrontendList toApplicationResourceDto(
            ApplicationResource applicationResource,
            ResourceAvailabilityService resourceAvailabilityService
    ) {
        return ApplicationResourceDTOFrontendList.builder()
                .id(applicationResource.getId())
                .resourceId(applicationResource.getResourceId())
                .resourceName(applicationResource.getResourceName())
                .resourceType(applicationResource.getResourceType())
                .resourceLimit(applicationResource.getResourceLimit())
                .identityProviderGroupObjectId(applicationResource.getIdentityProviderGroupObjectId())
                .applicationCategory(toApplicationCategoryNames(applicationResource))
                .status(applicationResource.getStatus())
                .resourceUsage(resourceAvailabilityService.getUsage(applicationResource.getResourceId()))
                .build();
    }
    public static Map<String, Object> toApplicationResourceAdminDtoPage(
            Page<ApplicationResource> applicationResourcePage,
            ResourceAvailabilityService resourceAvailabilityService
    ) {
        return Map.of(
                "resources",
                applicationResourcePage.getContent()
                        .stream()
                        .map(applicationResource -> toApplicationResourceAdminDto(applicationResource, resourceAvailabilityService))
                        .collect(Collectors.toList()),
                "currentPage", applicationResourcePage.getNumber(),
                "totalPages", applicationResourcePage.getTotalPages(),
                "size", applicationResourcePage.getSize(),
                "totalItems", applicationResourcePage.getTotalElements()
        );
    }
    public static ApplicationResourceDTOFrontendListForAdmin toApplicationResourceAdminDto(
            ApplicationResource applicationResource,
            ResourceAvailabilityService resourceAvailabilityService
    ) {

        return ApplicationResourceDTOFrontendListForAdmin.builder()
                .id(applicationResource.getId())
                .resourceId(applicationResource.getResourceId())
                .resourceName(applicationResource.getResourceName())
                .resourceType(applicationResource.getResourceType())
                .resourceLimit(applicationResource.getResourceLimit())
                .status(applicationResource.getStatus())
                .entraState(applicationResource.getEntraState())
                .identityProviderGroupObjectId(applicationResource.getIdentityProviderGroupObjectId())
                .needApproval(applicationResource.isNeedApproval())
                .applicationCategory(toApplicationCategoryNames(applicationResource))
                .resourceUsage(resourceAvailabilityService.getUsage(applicationResource.getResourceId()))
                .build();
    }

    public static List<String> toApplicationCategoryNames(ApplicationResource applicationResource) {
        return applicationResource.getApplicationCategory()
                .stream()
                .map(Applikasjonskategori::getName)
                .toList();
    }
}
