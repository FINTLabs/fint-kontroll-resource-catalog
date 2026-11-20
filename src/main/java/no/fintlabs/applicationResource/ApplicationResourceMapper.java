package no.fintlabs.applicationResource;

import lombok.RequiredArgsConstructor;
import no.fintlabs.kodeverk.applikasjonskategori.Applikasjonskategori;
import no.fintlabs.resourceAvailability.ResourceAvailabilityService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Component
public class ApplicationResourceMapper {
    private final ResourceAvailabilityService resourceAvailabilityService;

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

    public Map<String, Object> toApplicationResourceDtoPage(Page<ApplicationResource> applicationResourcePage) {
        return Map.of(
                "resources",
                    applicationResourcePage.getContent()
                        .stream()
                        .map(this::toApplicationResourceDto)
                        .collect(Collectors.toList()),
                "currentPage", applicationResourcePage.getNumber(),
                "totalPages", applicationResourcePage.getTotalPages(),
                "size", applicationResourcePage.getSize(),
                "totalItems", applicationResourcePage.getTotalElements()
        );
    }
    public ApplicationResourceDTOFrontendList toApplicationResourceDto(ApplicationResource applicationResource) {
        return new ApplicationResourceDTOFrontendList (
                applicationResource.getId(),
                applicationResource.getResourceId(),
                applicationResource.getResourceName(),
                applicationResource.getResourceType(),
                applicationResource.getResourceLimit(),
                applicationResource.getIdentityProviderGroupObjectId(),
                toApplicationCategoryNames(applicationResource),
                applicationResource.getStatus(),
                resourceAvailabilityService.getUsage(applicationResource.getResourceId())
        );
    }
    public Map<String, Object> toApplicationResourceAdminDtoPage(Page<ApplicationResource> applicationResourcePage) {
        return Map.of(
                "resources",
                applicationResourcePage.getContent()
                        .stream()
                        .map(this::toApplicationResourceAdminDto)
                        .collect(Collectors.toList()),
                "currentPage", applicationResourcePage.getNumber(),
                "totalPages", applicationResourcePage.getTotalPages(),
                "size", applicationResourcePage.getSize(),
                "totalItems", applicationResourcePage.getTotalElements()
        );
    }
    public ApplicationResourceDTOFrontendListForAdmin toApplicationResourceAdminDto(ApplicationResource applicationResource) {
        return new ApplicationResourceDTOFrontendListForAdmin (
                applicationResource.getId(),
                applicationResource.getResourceId(),
                applicationResource.getResourceName(),
                applicationResource.getResourceType(),
                applicationResource.getResourceLimit(),
                applicationResource.getStatus(),
                applicationResource.getEntraState(),
                applicationResource.getIdentityProviderGroupObjectId(),
                applicationResource.isNeedApproval(),
                toApplicationCategoryNames(applicationResource),
                resourceAvailabilityService.getUsage(applicationResource.getResourceId())
        );
    }

    public static List<String> toApplicationCategoryNames(ApplicationResource applicationResource) {
        return applicationResource.getApplicationCategory()
                .stream()
                .map(Applikasjonskategori::getName)
                .toList();
    }
}
