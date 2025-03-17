package no.fintlabs.applicationResourceLocation;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;


import jakarta.persistence.*;
import no.fintlabs.applicationResource.ApplicationResource;

import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "application_resource_location")
public class ApplicationResourceLocation {
    @Id()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "resource_ref")
    private Long resourceRef;
    private String resourceId;
    private String resourceName;
    @Column(name = "orgunit_id")
    private String orgUnitId;
    private String orgUnitName;
    private Long resourceLimit;

    @ManyToOne(fetch = FetchType.LAZY,
            cascade = {CascadeType.MERGE}
    )
    @JoinColumn(name="resource_ref",
            insertable = false,
            updatable = false
    )
    @JsonBackReference(value = "resource-location")
    private ApplicationResource applicationResource;

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        ApplicationResourceLocation that = (ApplicationResourceLocation) o;
//        return Objects.equals(id, that.id)
//                && Objects.equals(resourceRef, that.resourceRef)
//                && Objects.equals(resourceId, that.resourceId)
//                && Objects.equals(resourceName, that.resourceName)
//                && Objects.equals(orgUnitId, that.orgUnitId)
//                && Objects.equals(orgUnitName, that.orgUnitName)
//                && Objects.equals(resourceLimit, that.resourceLimit);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash( id, resourceRef, resourceId, resourceName, orgUnitId, orgUnitName, resourceLimit);
//    }
}
