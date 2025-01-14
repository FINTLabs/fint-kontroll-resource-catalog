package no.fintlabs.resourceAvailability;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "resource_consumer_assignments")
public class ResourceConsumerAssignments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "orgunit_id")
    private String orgUnitId;
    @Column(name = "assigned_resources")
    private Long assignedResources;

    @Override
    public String toString() {
        return "{" +
                "orgunitId='" + orgUnitId + '\'' +
                ", assignedResources=" + assignedResources +
                '}';
    }
}