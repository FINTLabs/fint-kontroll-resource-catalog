package no.fintlabs.resourceGroup;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResourceGroup {
    private ResourceGroupOperation operation;
    private String resourceId;
    private String idpGroupObjectId;
    private String resourceName;
}
