package no.fintlabs.applicationResource;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import no.fintlabs.applicationResourceLocation.ApplicationResourceLocation;
import no.fintlabs.resource.Resource;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name="application_resource")
public class ApplicationResource extends Resource {
    private String applicationAccessType;
    private String applicationAccessRole;
    @ElementCollection
    @CollectionTable(name = "application_resource_platform",joinColumns = @JoinColumn(name="id"))
    private List<String> platform = new ArrayList<>();
    private String accessType;
    private Long resourceLimit;
    private String resourceOwnerOrgUnitId;
    private String resourceOwnerOrgUnitName;
    private String licenseEnforcement;
    private boolean hasCost;
    private Long unitCost;
    private String status;
    private Date statusChanged;
    private String createdBy;
    private Date dateCreated;


    @ElementCollection
    @CollectionTable(name = "application_resource_valid_for_roles", joinColumns = @JoinColumn(name = "id"))
    private List<String> validForRoles= new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "id")
    @CollectionTable(name="application_resource_valid_for_org_units")
    private List<ApplicationResourceLocation> validForOrgUnits = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "application_resource_application_category", joinColumns = @JoinColumn(name = "id"))
    private List<String> applicationCategory;


    public ApplicationResourceDTOFrontendList toApplicationResourceDTOFrontendList(){
        return ApplicationResourceDTOFrontendList
                .builder()
                .id(id)
                .resourceId(resourceId)
                .resourceName(resourceName)
                .resourceType(resourceType)
                .resourceLimit(resourceLimit)
                .build();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationResource that = (ApplicationResource) o;
        return hasCost == that.hasCost
                && Objects.equals(applicationAccessType, that.applicationAccessType)
                && Objects.equals(applicationAccessRole, that.applicationAccessRole)
                && Objects.equals(platform, that.platform)
                && Objects.equals(accessType, that.accessType)
                && Objects.equals(resourceLimit, that.resourceLimit)
                && Objects.equals(resourceOwnerOrgUnitId, that.resourceOwnerOrgUnitId)
                && Objects.equals(resourceOwnerOrgUnitName, that.resourceOwnerOrgUnitName)
                && Objects.equals(licenseEnforcement, that.licenseEnforcement)
                && Objects.equals(unitCost, that.unitCost)
                && Objects.equals(status, that.status)
                && Objects.equals(statusChanged, that.statusChanged)
                && Objects.equals(validForRoles, that.validForRoles)
                && Objects.equals(validForOrgUnits, that.validForOrgUnits)
                && Objects.equals(applicationCategory, that.applicationCategory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                applicationAccessType,
                applicationAccessRole,
                platform,
                accessType,
                resourceLimit,
                resourceOwnerOrgUnitId,
                resourceOwnerOrgUnitName,
                licenseEnforcement,
                hasCost,
                unitCost,
                status,
                statusChanged,
                validForRoles,
                validForOrgUnits,
                applicationCategory);
    }
}

