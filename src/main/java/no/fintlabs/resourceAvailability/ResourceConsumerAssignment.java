package no.fintlabs.resourceAvailability;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceConsumerAssignment {
    private String orgUnitId;
    private Long assignedResources;
}