package no.fintlabs.applicationResourceLocation;

import lombok.*;


import jakarta.persistence.*;

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
    private String resourceId;
    @Column(name = "orgunit_id")
    private String orgUnitId;
    private String orgUnitName;
    private Long resourceLimit;


    public boolean equalsIgnoringId(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationResourceLocation that = (ApplicationResourceLocation) o;
        return Objects.equals(resourceId, that.resourceId)
                && Objects.equals(orgUnitId, that.orgUnitId)
                && Objects.equals(orgUnitName, that.orgUnitName)
                && Objects.equals(resourceLimit, that.resourceLimit);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationResourceLocation that = (ApplicationResourceLocation) o;
        return Objects.equals(id, that.id)
                && Objects.equals(resourceId, that.resourceId)
                && Objects.equals(orgUnitId, that.orgUnitId)
                && Objects.equals(orgUnitName, that.orgUnitName)
                && Objects.equals(resourceLimit, that.resourceLimit);
    }

    @Override
    public int hashCode() {
        return Objects.hash( id, resourceId, orgUnitId, orgUnitName, resourceLimit);
    }
}
