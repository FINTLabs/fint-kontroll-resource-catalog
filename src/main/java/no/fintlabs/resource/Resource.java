package no.fintlabs.resource;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(schema = "public")
public abstract class Resource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    @ElementCollection
    @CollectionTable(name="resource_children_resource_id")
    private List<String> childrenResourceId = new ArrayList<>();
    @ElementCollection
    @CollectionTable(name = "resource_valid_for_roles")
    private List<String> validForRoles= new ArrayList<>();
}
