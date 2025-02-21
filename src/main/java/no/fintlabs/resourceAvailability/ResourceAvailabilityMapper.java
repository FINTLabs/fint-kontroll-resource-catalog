package no.fintlabs.resourceAvailability;

import java.util.List;

public class ResourceAvailabilityMapper {
    public static ResourceAvailability toResourceAvailability(ResourceAvailabilityDTO resourceAvailabilityDTO) {

        return ResourceAvailability.builder()
                .resourceId(resourceAvailabilityDTO.getResourceId())
                .assignedResources(resourceAvailabilityDTO.getAssignedResources())
                .resourceConsumerAssignments(toResourceConsumerAssignment(resourceAvailabilityDTO.getResourceConsumerAssignment()))
                .build();
    }

    private static List<ResourceConsumerAssignments> toResourceConsumerAssignment(ResourceConsumerAssignment resourceConsumerAssignment) {
        ResourceConsumerAssignments resourceConsumerAssignments = ResourceConsumerAssignments.builder()
                .orgUnitId(resourceConsumerAssignment.getOrgUnitId())
                .assignedResources(resourceConsumerAssignment.getAssignedResources())
                .build();

        return List.of(resourceConsumerAssignments);

    }
}
