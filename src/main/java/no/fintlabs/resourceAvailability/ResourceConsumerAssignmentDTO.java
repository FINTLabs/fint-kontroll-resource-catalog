package no.fintlabs.resourceAvailability;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceConsumerAssignmentDTO {
    private String orgUnitId;
    private Long assignedResources;
}