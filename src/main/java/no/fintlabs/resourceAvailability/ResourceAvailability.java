package no.fintlabs.resourceAvailability;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "resource_availability")
public class ResourceAvailability {
    @Id()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "resource_id",unique = true)
    private String resourceId;
    @Column(name = "assigned_resources")
    private Long assignedResources;

    @OneToMany(cascade = CascadeType.ALL,orphanRemoval = true)
    @JoinColumn(name = "id")
    @CollectionTable(name = "resource_availability_resource_consumer_assignments")
    private List<ResourceConsumerAssignment> resourceConsumerAssignments = new ArrayList<>();


    @Override
    public String toString() {
        return "ResourceAvailability{" +
                "resourceId='" + resourceId + '\'' +
                ", assignedResources=" + assignedResources +
                ", resourceConsumerAssignments=" + resourceConsumerAssignments +
                '}';
    }
}


