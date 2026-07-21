package no.fintlabs.applicationResource;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.fintlabs.applicationResourceLocation.ApplicationResourceLocation;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationResourceFrontendRequest {
    private Long id;
    private String resourceId;
    private String resourceName;
    private String resourceType;
    private List<String> platform;
    private String accessType;
    private Long resourceLimit;
    private String resourceOwnerOrgUnitId;
    private String resourceOwnerOrgUnitName;
    private List<ApplicationResourceLocation> validForOrgUnits;
    private List<String> validForRoles;
    private List<String> applicationCategory;
    private String licenseEnforcement;
    private boolean hasCost;
    private Long unitCost;
    private String status;
    private boolean needApproval;
}
