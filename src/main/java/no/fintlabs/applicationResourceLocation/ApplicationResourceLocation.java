package no.fintlabs.applicationResourceLocation;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;


import jakarta.persistence.*;
import no.fintlabs.applicationResource.ApplicationResource;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "application_resource_location")
@EntityListeners(ApplicationResourceLocationListener.class)
public class ApplicationResourceLocation {
    @Id()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String resourceId;
    private String resourceName;
    @Column(name = "orgunit_id")
    private String orgUnitId;
    private String orgUnitName;
    private Long resourceLimit;

    @ManyToOne(fetch = FetchType.LAZY,
            cascade = {CascadeType.MERGE}
    )
    @JoinColumn(name="resource_ref")
    @JsonBackReference(value = "resource-location")
    private ApplicationResource applicationResource;

}
