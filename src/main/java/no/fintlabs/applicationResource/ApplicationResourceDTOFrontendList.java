package no.fintlabs.applicationResource;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class ApplicationResourceDTOFrontendList {
    private Long id;
    private String resourceId;
    private String resourceName;
    private String resourceType;
    private Long resourceLimit;
    private UUID identityProviderGroupObjectId;
}
