package no.fintlabs.resourceGroup;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EntraGroup {
    protected String objectId;
    protected String displayName;
    protected Long resourceGroupId;
    protected String traceId;
    protected EntraStatus status;
}
