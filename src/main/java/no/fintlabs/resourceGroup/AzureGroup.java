package no.fintlabs.resourceGroup;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
public class AzureGroup {
    protected UUID id;
    protected String displayName;
    protected List<String> members;

    protected String stringresourceGroupID;

}
