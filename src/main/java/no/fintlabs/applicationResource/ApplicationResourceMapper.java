package no.fintlabs.applicationResource;

import org.springframework.data.domain.Page;

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
                applicationResource.getApplicationCategory()
        );
    }
}
