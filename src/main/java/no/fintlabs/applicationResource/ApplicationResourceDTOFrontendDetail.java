package no.fintlabs.applicationResource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import no.fintlabs.applicationResourceLocation.ApplicationResourceLocation;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationResourceDTOFrontendDetail {
    private Long id;
    private String resourceId;
    private String resourceName;
    private String resourceType;
    private String identityProviderGroupName;
    private String applicationAccessType;
    private String applicationAccessRole;
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
    private Date statusChanged;
    private String createdBy;
    private Date dateCreated;

    @JsonIgnore
    public boolean isValid(){
        return this.id!=null;
    }

}
