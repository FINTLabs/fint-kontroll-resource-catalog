package no.fintlabs.applicationResource;

import lombok.*;
import no.fintlabs.applicationResourceLocation.ApplicationResourceLocation;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name="application_resource",schema = "public")
public class ApplicationResource {
    @Id()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String resourceId;
    private String resourceName;
    private String resourceType;
    private String applicationAccessType;
    private String applicationAccessRole;
    private String platform;
    private String accessType;
    private Long resourceLimit;
    private String resourceOwnerOrgUnitId;
    private String resourceOwnerOrgUnitName;


    @ElementCollection
    @CollectionTable(name = "application_resource_valid_for_roles",joinColumns = @JoinColumn(name = "application_resource_id"))
    private List<String> validForRoles= new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "application_resource_id")
    @CollectionTable(name="application_resource_valid_for_org_units")
    private List<ApplicationResourceLocation> validForOrgUnits = new ArrayList<>();


    public ApplicationResourceDTO toApplicationResourceDTO() {
        return ApplicationResourceDTO
                .builder()
                .id(id)
                .resourceId(resourceId)
                .resourceName(resourceName)
                .resourceType(resourceType)
                .applicationAccessType(applicationAccessType)
                .applicationAccessRole(applicationAccessRole)
                .platform(platform)
                .accessType(accessType)
                .resourceLimit(resourceLimit)
                .resourceOwnerOrgUnitId(resourceOwnerOrgUnitId)
                .resourceOwnerOrgUnitName(resourceOwnerOrgUnitName)
                .validForRoles(validForRoles)
                .validForOrgUnits(validForOrgUnits)
                .build();

    }

}

