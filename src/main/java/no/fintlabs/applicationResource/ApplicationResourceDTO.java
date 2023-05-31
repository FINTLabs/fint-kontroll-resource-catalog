package no.fintlabs.applicationResource;

import lombok.*;
import no.fintlabs.applicationResourceLocation.ApplicationResourceLocation;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationResourceDTO {
    private Long id;
    private String resourceId;
    private String resourceName;
    private String resourceType;
    private String applicationAccessType;
    private String applicationAccessRole;
    private List<String> platform;
    private String accessType;
    private Long resourceLimit;
    private String resourceOwnerOrgUnitId;
    private String resourceOwnerOrgUnitName;
    private List<ApplicationResourceLocation> validForOrgUnits;
    private List<String> validForRoles;

}
