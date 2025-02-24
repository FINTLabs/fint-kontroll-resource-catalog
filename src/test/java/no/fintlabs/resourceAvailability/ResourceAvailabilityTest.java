package no.fintlabs.resourceAvailability;

import no.fintlabs.DatabaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;


@DataJpaTest
@Testcontainers
@ActiveProfiles("Test")
@Import(ResourceAvailabilityService.class)
class ResourceAvailabilityTest  extends DatabaseIntegrationTest {

    @Autowired
    private ResourceAvailabilityService resourceAvailabilityService;

    @Autowired
    private ResourceAvailabilityRepository resourceAvailabilityRepository;

    @Test
    public void shouldReturnNewResourceAvailabilityOnSave() {
        List<ResourceConsumerAssignment> resourceConsumerAssignmentNewList = new ArrayList<>();
        ResourceAvailability resourceAvailabilityNew = ResourceAvailability
                .builder()
                .resourceId("Kabal")
                .assignedResources(100L)
                .resourceConsumerAssignments(resourceConsumerAssignmentNewList)
                .build();

        ResourceConsumerAssignment resourceConsumerAssignment1 = ResourceConsumerAssignment
                .builder()
                .orgUnitId("org1")
                .assignedResources(20L)
                .build();
        resourceConsumerAssignmentNewList.add(resourceConsumerAssignment1);
        ResourceConsumerAssignment resourceConsumerAssignment2 = ResourceConsumerAssignment
                .builder()
                .orgUnitId("org1")
                .assignedResources(20L)
                .build();
        resourceConsumerAssignmentNewList.add(resourceConsumerAssignment2);
        resourceAvailabilityService.save(resourceAvailabilityNew);

        Optional<ResourceAvailability> resourceAvailabilityfromDB = resourceAvailabilityRepository
                .findByResourceId(resourceAvailabilityNew.getResourceId());

        assertEquals(resourceAvailabilityNew.getResourceId(), resourceAvailabilityfromDB.get().getResourceId());
    }

    @Test
    public void shouldNotReturnNewRecordOfResourceAvailabilityOnUpdate() {
        List<ResourceConsumerAssignment> resourceConsumerAssignmentNewList = new ArrayList<>();
        ResourceAvailability resourceAvailabilityNew = ResourceAvailability
                .builder()
                .resourceId("Kabal")
                .assignedResources(100L)
                .resourceConsumerAssignments(resourceConsumerAssignmentNewList)
                .build();

        ResourceConsumerAssignment resourceConsumerAssignment1 = ResourceConsumerAssignment
                .builder()
                .orgUnitId("org1")
                .assignedResources(20L)
                .build();
        resourceConsumerAssignmentNewList.add(resourceConsumerAssignment1);
        ResourceConsumerAssignment resourceConsumerAssignment2 = ResourceConsumerAssignment
                .builder()
                .orgUnitId("org2")
                .assignedResources(20L)
                .build();
        resourceConsumerAssignmentNewList.add(resourceConsumerAssignment2);
        resourceAvailabilityService.save(resourceAvailabilityNew);

        List<ResourceConsumerAssignment> resourceConsumerAssignmentUpdateList = new ArrayList<>();
        ResourceAvailability resourceAvailabilityUpdate = ResourceAvailability
                .builder()
                .resourceId("Kabal")
                .assignedResources(110L)
                .resourceConsumerAssignments(resourceConsumerAssignmentNewList)
                .build();

        ResourceConsumerAssignment resourceConsumerAssignmentUpdate1 = ResourceConsumerAssignment
                .builder()
                .orgUnitId("org1")
                .assignedResources(25L)
                .build();
        resourceConsumerAssignmentUpdateList.add(resourceConsumerAssignmentUpdate1);
        ResourceConsumerAssignment resourceConsumerAssignmentUpdate2 = ResourceConsumerAssignment
                .builder()
                .orgUnitId("org2")
                .assignedResources(25L)
                .build();
        resourceConsumerAssignmentUpdateList.add(resourceConsumerAssignmentUpdate2);
        resourceAvailabilityService.save(resourceAvailabilityUpdate);

        Optional<ResourceAvailability> resourceAvailabilityfromDB = resourceAvailabilityRepository
                .findByResourceId(resourceAvailabilityNew.getResourceId());

        assertEquals(resourceAvailabilityNew.getResourceId(), resourceAvailabilityfromDB.get().getResourceId());
        Long numberOfRecords = resourceAvailabilityRepository.count();
        assertEquals(1L, numberOfRecords);
    }

}