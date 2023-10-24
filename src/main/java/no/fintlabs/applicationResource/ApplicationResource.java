package no.fintlabs.applicationResource;

import lombok.*;
import no.fintlabs.applicationResourceLocation.ApplicationResourceLocation;
import no.fintlabs.resource.Resource;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
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


    @ElementCollection
    @CollectionTable(name = "application_resource_valid_for_roles", joinColumns = @JoinColumn(name = "id"))
    private List<String> validForRoles= new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "id")
    @CollectionTable(name="application_resource_valid_for_org_units")
    private List<ApplicationResourceLocation> validForOrgUnits = new ArrayList<>();


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



}

