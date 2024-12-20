package no.fintlabs.applicationResource;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
public class ApplicationResourceDTOFrontendListForAdmin {
    private Long id;
    private String resourceId;
    private String resourceName;
    private String resourceType;
    private Long resourceLimit;
    private String status;
    private UUID identityProviderGroupObjectId;
    private boolean needApproval;
    private List<String> applicationCategory;

}
