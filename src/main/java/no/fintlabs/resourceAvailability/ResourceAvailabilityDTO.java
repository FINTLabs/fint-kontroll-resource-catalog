package no.fintlabs.resourceAvailability;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResourceAvailabilityDTO {
    private String resourceId;
    private Long assignedResources;
    private ResourceConsumerAssignment resourceConsumerAssignment;
}
