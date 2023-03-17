package no.fintlabs.resource;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class Resource {
    private Long id;
    private String resourceId;
    private String resourceName;
    private String resourceType;
    private Long resourceLimit;
    private String resourceOwnerOrgUnitId;
    private String resourceOwnerName;
    private String resourceConsumerOrgUnitId;
    private String resourceConsumerName;
    private String parentResourceId;
    private String childrenResourceId;
    private List<String> validForRoles;
}
