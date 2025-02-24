package no.fintlabs.resourceAvailability;

import java.util.List;

public class ResourceAvailabilityMapper {
    public static ResourceAvailability toResourceAvailability(ResourceAvailabilityDTO resourceAvailabilityDTO) {

        return ResourceAvailability.builder()
                .resourceId(resourceAvailabilityDTO.getResourceId())
                .assignedResources(resourceAvailabilityDTO.getAssignedResources())
                .resourceConsumerAssignments(toResourceConsumerAssignment(resourceAvailabilityDTO.getResourceConsumerAssignmentDTO()))
                .build();
    }

    private static List<ResourceConsumerAssignment> toResourceConsumerAssignment(ResourceConsumerAssignmentDTO resourceConsumerAssignmentDTO) {
        ResourceConsumerAssignment resourceConsumerAssignment = ResourceConsumerAssignment.builder()
                .orgUnitId(resourceConsumerAssignmentDTO.getOrgUnitId())
                .assignedResources(resourceConsumerAssignmentDTO.getAssignedResources())
                .build();

        return List.of(resourceConsumerAssignment);

    }
}
