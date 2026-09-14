package no.fintlabs.applicationResource;

import no.fintlabs.kodeverk.applikasjonskategori.Applikasjonskategori;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ApplicationResourceMapper {

    public static Map<String, Object> toApplicationResourceDtoPage(Page<ApplicationResource> applicationResourcePage) {
        return Map.of(
                "resources",
                    applicationResourcePage.getContent()
                        .stream()
                        .map(ApplicationResourceMapper::toApplicationResourceDto)
                        .collect(Collectors.toList()),
                "currentPage", applicationResourcePage.getNumber(),
                "totalPages", applicationResourcePage.getTotalPages(),
                "size", applicationResourcePage.getSize(),
                "totalItems", applicationResourcePage.getTotalElements()
        );
    }
    public static ApplicationResourceDTOFrontendList toApplicationResourceDto(ApplicationResource applicationResource) {
        return new ApplicationResourceDTOFrontendList (
                applicationResource.getId(),
                applicationResource.getResourceId(),
                applicationResource.getResourceName(),
                applicationResource.getResourceType(),
                applicationResource.getResourceLimit(),
                applicationResource.getIdentityProviderGroupObjectId(),
                toApplicationCategoryNames(applicationResource),
                applicationResource.getStatus()
        );
    }
    public static Map<String, Object> toApplicationResourceAdminDtoPage(Page<ApplicationResource> applicationResourcePage) {
        return Map.of(
                "resources",
                applicationResourcePage.getContent()
                        .stream()
                        .map(ApplicationResourceMapper::toApplicationResourceAdminDto)
                        .collect(Collectors.toList()),
                "currentPage", applicationResourcePage.getNumber(),
                "totalPages", applicationResourcePage.getTotalPages(),
                "size", applicationResourcePage.getSize(),
                "totalItems", applicationResourcePage.getTotalElements()
        );
    }
    public static ApplicationResourceDTOFrontendListForAdmin toApplicationResourceAdminDto(ApplicationResource applicationResource) {
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
                toApplicationCategoryNames(applicationResource)
        );
    }

    public static List<String> toApplicationCategoryNames(ApplicationResource applicationResource) {
        return applicationResource.getApplicationCategory()
                .stream()
                .map(Applikasjonskategori::getName)
                .toList();
    }
}
